package com.radsto.radstorms.radiation;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;

//public class RadiationIntegration {
//    public static final boolean RAD_LOADED =
//            ModList.get().isLoaded("radioactive");
//
//    public static void applyRadiation(Player player, float amount) {
//        if (!RAD_LOADED) return;
//
//        player.getCapability(RadiationProvider.RADIATION).ifPresent(cap -> {
//            float currentRad = cap.getRadiation();
//            cap.setRadiation(currentRad + amount);
//        });
//    }
//    public static float getRadiation(Player player) {
//        if (!RAD_LOADED) return 0.0f;
//
//        return player.getCapability(RadiationProvider.RADIATION)
//                .map(IRadiationCapability::getRadiation)
//                .orElse(0.0f);
//    }
//}
