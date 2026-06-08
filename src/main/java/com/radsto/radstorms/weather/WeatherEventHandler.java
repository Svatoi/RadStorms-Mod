package com.radsto.radstorms.weather;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

//@Mod.EventBusSubscriber(modid = RadStorms.MODID)
//public class WeatherEventHandler {
//    public static boolean isRadRainActive = false;
//    private static boolean serverWasRaining = false;
//
//    @SubscribeEvent
//    public static void onWorldTick(TickEvent.LevelTickEvent event) {
//        if(event.phase != TickEvent.Phase.END && event.level.isClientSide()) return;
//
//        ServerLevel level = (ServerLevel) event.level;
//
//        if (level.getGameTime() % 20 == 0) {
//            boolean isRaining = level.isRaining();
//
//            if (isRaining && !serverWasRaining) {
//                float chance = RadStormsConfig.RAD_RAIN_CHANCE.get().floatValue();
//                isRadRainActive = level.random.nextFloat() < chance;
//
//                if (isRadRainActive) {
//                    RadStorms.LOGGER.info("Warning: RadRain is coming");
//                }
//            } else if (!isRaining && serverWasRaining) {
//                isRadRainActive = false;
//                RadStorms.LOGGER.info("RadRain end");
//            }
//
//            serverWasRaining = isRaining;
//        }
//    }
//
//    @SubscribeEvent
//    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
//        if (!(event.getEntity() instanceof Player player)) return;
//
//        Level level = player.level();
//        if (level.isClientSide) return;
//
//        if (player.tickCount % 20 == 0) {
//            if (isRadRainActive && level.isRaining()) {
//                BlockPos pos = player.blockPosition();
//
//                if (level.canSeeSky(pos)) {
//                    float damage = RadStormsConfig.RAD_RAIN_DAMAGE.get().floatValue();
//                    RadiationIntegration.applyRadiation(player, 0.25f);
//                }
//            }
//        }
//    }
//}
