package com.radsto.radstorms.event;

import com.radsto.radstorms.RadStormsMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RadStormsMod.MOD_ID, value = Dist.CLIENT)
public class ModClientEvents {
    public static boolean isClientStormActive = false;

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        Player player = mc.player;

        if (level == null) return;

        BlockPos pos = player.blockPosition();

        boolean isRaining = level.isRaining() || level.isThundering();

        if (isRaining && isClientStormActive) {
            int playerY = pos.getY();
            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());

            int fadeStart = surfaceY;
            int fadeEnd = surfaceY - 15;

            if (playerY >= fadeStart) {
                event.setNearPlaneDistance(1.3f);
                event.setFarPlaneDistance(22f);
                event.setCanceled(true);
            } else if (playerY > fadeEnd) {
                float totalDistance = fadeStart - fadeEnd;
                float playerDistance = fadeStart - playerY;
                float depthFactor = playerDistance / totalDistance;

                float startFar = 22f;
                float endFar = 25f;
                float currentFar = startFar + (endFar - startFar) * depthFactor;

                float currentNear = 1.5f + (8f - 1.3f) * depthFactor;
                event.setNearPlaneDistance(currentNear);
                event.setFarPlaneDistance(currentFar);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onComputerFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        Player player = mc.player;

        if (level == null) return;

        BlockPos pos = player.blockPosition();

        boolean isRaining = level.isRaining() || level.isThundering();

        if (isRaining && isClientStormActive) {
            int playerY = pos.getY();
            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());

            int fadeStart = surfaceY;
            int fadeEnd = surfaceY - 15;

            if (playerY > surfaceY - 10) {
                event.setRed(0.45f);
                event.setGreen(0.45f);
                event.setBlue(0.45f);
            } else if (playerY > fadeEnd) {
                float totalDistance = fadeStart - fadeEnd;
                float playerDistance = fadeStart - playerY;
                float depthFactor = playerDistance / totalDistance;

                float vanillaR = event.getRed();
                float vanillaG = event.getGreen();
                float vanillaB = event.getBlue();

                event.setRed(0.45f + (vanillaR - 0.45f) * depthFactor);
                event.setGreen(0.45f + (vanillaG - 0.45f) * depthFactor);
                event.setBlue(0.45f + (vanillaB - 0.45f) * depthFactor);
            }
        }
    }
}
