package com.radsto.radstorms.weather;

import com.radsto.radstorms.RadStormsMod;
import com.radsto.radstorms.capability.PlayerRadiationProvider;
import com.radsto.radstorms.event.ModEvents;
import com.radsto.radstorms.world.RadStormData;
import com.radsto.radstorms.world.StormType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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

        // vanilla weather
        boolean isRaining = level.isRaining() || level.isThundering();
        boolean isDay = level.isDay();
        BlockPos pos = entity.blockPosition();

        if (entity instanceof Player player) {
            // Logic for player
            // Either there's an active storm rain right on the player (isRainingAt)
            // Or it's a clear day (storm is active) and the player is out in the open sun (canSeeSky)
            boolean underStorm = (currentStorm == StormType.RAD_RAIN || currentStorm == StormType.NUCLEAR_BLOWOUT) && isRaining && level.isRainingAt(pos);
            boolean underSun = (currentStorm == StormType.SOLAR_FLARE) && isDay && level.canSeeSky(pos) && !isRaining;

//            RadStormsMod.LOGGER.info("isRaining " + isRaining + " | isRainingAt: " + level.isRainingAt(pos));

            if (underStorm || underSun) {
                int entityY = pos.getY();
                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());

                if (entityY > surfaceY - 15) {
                    // If the sun is blazing — damage is less (0.3f), if there's a storm — it's more (0.5f)
                    float finalRadDamage = underSun ? 5f : 10f; // for realis 5f -> 0.3f, 10f -> 0.5f

                    player.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).ifPresent(radiation -> {
                        radiation.addRadiation(finalRadDamage);
                        ModEvents.applyRadiationStage(player, radiation.getRadiationByPercentage(), level);
                    });
                    return;
                }
            }
            // If player didn't get exposed (night, in the mine, under the roof) gradually cleaning
            ModEvents.subRadiationStage(player, level);
        } else {
            // Logic for mobs
            // get rained on during a storm if they’re not under a roof (isRainingAt)
            // Or get sunlight on a clear stormy day (canSeeSky)

            boolean mobUnderStorm = (currentStorm == StormType.RAD_RAIN || currentStorm == StormType.NUCLEAR_BLOWOUT) && isRaining && level.isRainingAt(pos);
            if (mobUnderStorm) {
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
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.level.isClientSide() || event.phase != TickEvent.Phase.END) return;

        ServerLevel level = (ServerLevel) event.level;
        if (level.dimension() != Level.OVERWORLD) return;

        StormType currentStorm = RadStormData.get(level).getCurrentStorm();

        if (currentStorm == StormType.NUCLEAR_BLOWOUT) {
            RandomSource random = level.getRandom();

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
    }
}

