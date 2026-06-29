package com.radsto.radstorms.weather;

import com.radsto.radstorms.RadStormsMod;
import com.radsto.radstorms.capability.PlayerRadiationProvider;
import com.radsto.radstorms.event.ModEvents;
import com.radsto.radstorms.items.ModArmorMaterials;
import com.radsto.radstorms.items.ModItems;
import com.radsto.radstorms.items.custom.ModArmorItem;
import com.radsto.radstorms.sound.ModSounds;
import com.radsto.radstorms.world.RadStormData;
import com.radsto.radstorms.world.StormType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber(modid = RadStormsMod.MOD_ID)
public class WeatherEventHandler {
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();

        if(level.isClientSide() || entity.tickCount % 10 != 0) return;

        ServerLevel serverLevel = (ServerLevel) level;
        StormType currentStorm = RadStormData.get(serverLevel).getCurrentStorm();

        if (currentStorm == StormType.NONE) return;

        BlockPos pos = entity.blockPosition();
        int entityY = pos.getY();
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());

        boolean isInDangerZone = entityY > surfaceY - 15;

        if (entity instanceof Player player) {
            // Logic for player
            // Either there's an active storm rain right on the player (isRainingAt)
            // Or it's a clear day (storm is active) and the player is out in the open sun (canSeeSky)

            // vanilla weather
            boolean isRaining = level.isRaining() || level.isThundering();
            boolean isDay = level.isDay();

            boolean underStorm = (currentStorm == StormType.RAD_RAIN || currentStorm == StormType.NUCLEAR_BLOWOUT) && isRaining && level.isRainingAt(pos);
            boolean underSun = (currentStorm == StormType.SOLAR_FLARE || currentStorm == StormType.SUPER_SOLAR_APOCALYPSE) && isDay && level.canSeeSky(pos) && !isRaining;

            boolean underPassiveContamination = (currentStorm == StormType.RAD_CONTAMINATION) && isInDangerZone;

            ItemStack helmet = player.getInventory().getArmor(3);

            if (hasGeigerCounter(player)) {
                int tickInterval = 40;

                if (currentStorm == StormType.SOLAR_FLARE) tickInterval = 20;
                if (currentStorm == StormType.RAD_RAIN) tickInterval = 10;
                if (currentStorm == StormType.NUCLEAR_BLOWOUT) tickInterval = 5;
                if (currentStorm == StormType.SUPER_SOLAR_APOCALYPSE) tickInterval = 2;

                AtomicInteger finalTickInterval = new AtomicInteger(tickInterval);
                player.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).ifPresent(radiation -> {
                    float radPercent = radiation.getRadiationByPercentage();
                    if (radPercent >= 10.0f) finalTickInterval.set(30);
                    if (radPercent >= 35.0f) finalTickInterval.set(15);
                    if (radPercent >= 65.0f) finalTickInterval.set(6);
                    if (radPercent >= 90.0f) finalTickInterval.set(2);
                    if (radPercent >= 100.0f) finalTickInterval.set(1);
                });

                if (player.tickCount % finalTickInterval.get() == 0) {
                    float randomPitch = 1.4f + level.getRandom().nextFloat() * 0.5f;
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            ModSounds.GEIGER_CLICK.get(),
                            SoundSource.PLAYERS, 0.4f, randomPitch);
                }
            }

            if (underStorm || underSun || underPassiveContamination) {
                // If the sun is blazing — damage is less (0.3f), if there's a storm — it's more (0.5f)
                float RadDamage = 0.3f; // for realis 5f -> 0.3f, 10f -> 0.5f

                if (currentStorm == StormType.SOLAR_FLARE) RadDamage = 0.5f;
                if (currentStorm == StormType.RAD_RAIN) RadDamage = 0.8f;
                if (currentStorm == StormType.NUCLEAR_BLOWOUT) RadDamage = 1.3f;
                if (currentStorm == StormType.SUPER_SOLAR_APOCALYPSE) RadDamage = 2.6f;
                    
                final float finalRadDamage = RadDamage;

                if(!helmet.isEmpty() && helmet.getItem() instanceof ModArmorItem armorItem
                        && armorItem.getMaterial() == ModArmorMaterials.GAS_MASK) {

                        int filterLeft = helmet.getOrCreateTag().getInt("FilterLeft");


                        if (filterLeft > 0) {
                            ModEvents.subRadiationStage(player, level, 1.0f);

                            if (player.tickCount % 60 == 0) {
                                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                        SoundEvents.PLAYER_BREATH,
                                        SoundSource.PLAYERS, 0.4f, 0.8f);
                            }

                            filterLeft--;
                            helmet.getOrCreateTag().putInt("FilterLeft", filterLeft);

                            if (filterLeft <= 0) {
                                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                                        SoundEvents.PLAYER_BREATH,
                                        SoundSource.PLAYERS, 0.4f, 0.8f);
                                player.sendSystemMessage(Component.literal("§cФильтр противогаза полностью исчерпан!"));
                            }
                            return;
                        }
                } else {
                    player.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).ifPresent(radiation -> {
                        radiation.addRadiation(finalRadDamage);
                        ModEvents.applyRadiationStage(player, radiation.getRadiationByPercentage(), level);
                    });
                }
                return;
            }
            // If player didn't get exposed (night, in the mine, under the roof) gradually cleaning
            ModEvents.subRadiationStage(player, level, 0.06f);
        } else {
            // Logic for mobs
            // get rained on during a storm if they’re not under a roof (isRainingAt)
            // Or get sunlight on a clear stormy day (canSeeSky)
            float finalMobRadDamage = 0.3f;

            boolean mobUnderStorm = (
                    currentStorm == StormType.RAD_RAIN ||
                            currentStorm == StormType.NUCLEAR_BLOWOUT) && level.isRaining() && level.isRainingAt(pos);
            boolean mobUnderSun = (
                    currentStorm == StormType.SOLAR_FLARE ||
                            currentStorm == StormType.SUPER_SOLAR_APOCALYPSE) && level.isDay() && level.canSeeSky(pos) && !level.isRaining();

            if(isInDangerZone) {
                if (currentStorm == StormType.SOLAR_FLARE) finalMobRadDamage = 0.5f;
                if (currentStorm == StormType.RAD_RAIN) finalMobRadDamage = 0.8f;
                if (currentStorm == StormType.NUCLEAR_BLOWOUT) finalMobRadDamage = 1.3f;
                if (currentStorm == StormType.SUPER_SOLAR_APOCALYPSE) finalMobRadDamage = 2.6f;

                if (currentStorm == StormType.NUCLEAR_BLOWOUT && entity instanceof Enemy) {
                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, false, false));
                    entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 0, false, false));
                    entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 1, false, false));

                    if (serverLevel.getRandom().nextFloat() < 0.1f) {
                        serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                                entity.getX(), entity.getY() + 1.0D, entity.getZ(), 1, 0.2, 0.2, 0.2, 0.0);
                    }
                }

                if (mobUnderSun || (currentStorm == StormType.RAD_CONTAMINATION)) {
                    if (entity.getMobType() != MobType.UNDEAD && !(entity instanceof Enemy)) {
                        entity.hurt(level.damageSources().magic(), finalMobRadDamage);
                    }
                } else if (mobUnderStorm && level.isRainingAt(pos)) {
                    if (entity.getMobType() != MobType.UNDEAD && !(entity instanceof Enemy)) {
                        entity.hurt(level.damageSources().magic(), finalMobRadDamage);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.level.isClientSide() || event.phase != TickEvent.Phase.END) return;

        ServerLevel level = (ServerLevel) event.level;
        if (level.dimension() != Level.OVERWORLD) return;

        StormType currentStorm = RadStormData.get(level).getCurrentStorm();
        RandomSource random = level.getRandom();

        if (currentStorm == StormType.NUCLEAR_BLOWOUT) {
            if (level.getGameTime() % 20 == 0 && random.nextFloat() < 0.15f) {
                if (!level.players().isEmpty()) {
                    Player target = level.players().get(random.nextInt(level.players().size()));
                    BlockPos playerPos = target.blockPosition();

                    int offsetX = random.nextInt(60) - 30;
                    int offsetZ = random.nextInt(60) - 30;

                    BlockPos strikePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, playerPos.offset(offsetX, 0, offsetZ));

                    LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
                    if (lightning != null) {
                        lightning.moveTo(strikePos.getX(), strikePos.getY(), strikePos.getZ());
                        level.addFreshEntity(lightning);
                    }
                }
            }
        }

        if (currentStorm == StormType.SUPER_SOLAR_APOCALYPSE && level.isDay()) {
            if (!level.players().isEmpty()) {
                for (Player player : level.players()) {
                    BlockPos playerPos = player.blockPosition();

                    for (int i = 0; i < 5; i++) {
                        int offsetX = random.nextInt(80) - 40;
                        int offsetZ = random.nextInt(80) - 40;

                        BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, playerPos.offset(offsetX, 0, offsetZ));
                        BlockPos blockToBurn = surfacePos.below();

                        BlockState state = level.getBlockState(blockToBurn);

                        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MYCELIUM) || state.is(Blocks.PODZOL)) {
                            level.setBlockAndUpdate(blockToBurn, Blocks.DIRT.defaultBlockState());
                        } else if (state.getBlock() instanceof LeavesBlock) {
                            level.setBlockAndUpdate(blockToBurn, Blocks.AIR.defaultBlockState());
                        } else if (state.is(Blocks.TALL_GRASS) || state.is(Blocks.FERN) || state.is(Blocks.SUNFLOWER)) {
                            level.setBlockAndUpdate(blockToBurn, Blocks.AIR.defaultBlockState());
                        }

                        if (random.nextFloat() < 0.02f && level.isEmptyBlock(surfacePos)) {
                            if (state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS)) {
                                level.setBlockAndUpdate(surfacePos, Blocks.FIRE.defaultBlockState());
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean hasGeigerCounter(Player player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == ModItems.GEIGER_COUNTER.get()) {
                return true;
            }
        }
        return false;
    }
}

