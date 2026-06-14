package com.radsto.radstorms.event;

import com.radsto.radstorms.RadStormsMod;
import com.radsto.radstorms.capability.PlayerRadiation;
import com.radsto.radstorms.capability.PlayerRadiationProvider;
import com.radsto.radstorms.command.RadiationCommand;
import com.radsto.radstorms.world.RadStormData;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RadStormsMod.MOD_ID)
public class ModEvents {
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(PlayerRadiation.class);
    }

    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(PlayerRadiationProvider.PLAYER_RADIATION).isPresent()) {
                event.addCapability(new ResourceLocation(RadStormsMod.MOD_ID, "properties"), new PlayerRadiationProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();

        if(level.isClientSide() || entity.tickCount % 10 != 0) return;

        ServerLevel serverLevel = (ServerLevel) level;
        boolean isStormActive = RadStormData.get(serverLevel).isActive();

        // vanilla weather
        boolean isRaining = level.isRaining() || level.isThundering();
        boolean isDay = level.isDay();

        BlockPos pos = entity.blockPosition();

        if (entity instanceof Player player) {
            // Logic for player
            // Either there's an active storm rain right on the player (isRainingAt)
            // Or it's a clear day (storm is active) and the player is out in the open sun (canSeeSky)
            boolean underStorm = isStormActive && isRaining && level.isRainingAt(pos);
            boolean underSun = isStormActive && isDay && level.canSeeSky(pos) && !isRaining;

//            RadStormsMod.LOGGER.info("isRaining " + isRaining + " | isRainingAt: " + level.isRainingAt(pos));

            if (underStorm || underSun) {
                int entityY = pos.getY();
                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());

                if (entityY > surfaceY - 15) {
                    // If the sun is blazing — damage is less (0.3f), if there's a storm — it's more (0.5f)
                    float finalRadDamage = underSun ? 5f : 10f; // for realis 5f -> 0.3f, 10f -> 0.5f

                    player.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).ifPresent(radiation -> {
                        radiation.addRadiation(finalRadDamage);
                        applyRadiationStage(player, radiation.getRadiationByPercentage(), level);
                        });
                        return;
                    }
                }
                // If player didn't get exposed (night, in the mine, under the roof) gradually cleaning
                subRadiationStage(player, level);
            } else {
                // Logic for mobs
                // get rained on during a storm if they’re not under a roof (isRainingAt)
                // Or get sunlight on a clear stormy day (canSeeSky)

                boolean mobUnderStorm = isStormActive && isRaining && level.isRainingAt(pos);
                boolean mobUnderSun = isStormActive && isDay && level.canSeeSky(pos) && !isRaining;

                if (mobUnderStorm || mobUnderSun) {
                    int entityY = pos.getY();
                    int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());

                    if (entityY > surfaceY - 15) {
                        if (entity.getMobType() != MobType.UNDEAD && !(entity instanceof Enemy)) {
                            entity.hurt(level.damageSources().magic(), 0.5f);
                        }
                    }
                }
        }
    }

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        RadiationCommand.register(event.getDispatcher());
    }

    private static void subRadiationStage(Player player, Level level) {
        player.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).ifPresent(radiation -> {
            if (radiation.getRadiation() != 0) {
                radiation.subRadiation(0.06f);
                RadStormsMod.LOGGER.info("The player is being subradiated: " + radiation.getRadiationByPercentage() + "%");

                float radPercentage = radiation.getRadiationByPercentage();
                applyRadiationStage(player, radPercentage, level);
            }
        });
    }

    private static void applyRadiationStage(Player player, float radPercentage, Level level) {
        if (radPercentage >= 5 && radPercentage <= 50) {
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0, false, true));
        }
        if (radPercentage >= 20) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 2, false, true));
            if (radPercentage <= 50) {
                player.hurt(level.damageSources().magic(), 1.0f);
            }
        }
        if (radPercentage >= 50 && radPercentage <= 80) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 1, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 1, false, true));
            player.hurt(level.damageSources().magic(), 2.5f);
        }
        if (radPercentage >= 80) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 2, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 2, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 2, false, true));
            player.hurt(level.damageSources().magic(), 4.3f);
        }
    }
}
