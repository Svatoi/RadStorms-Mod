package com.radsto.radstorms.event;

import com.radsto.radstorms.RadStormsMod;
import com.radsto.radstorms.world.StormType;
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
    public static StormType clientStormType = StormType.NONE;

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        Player player = mc.player;

        if (level == null || player == null) return;

        BlockPos pos = player.blockPosition();
        boolean isRaining = level.isRaining() || level.isThundering();

        if (isRaining && (clientStormType == StormType.RAD_RAIN || clientStormType == StormType.NUCLEAR_BLOWOUT)) {
            int playerY = pos.getY();
            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());

            int fadeStart = surfaceY;
            int fadeEnd = surfaceY - 15;

            float nearDist = 1.3f;
            float farDist = 22f;

            if(clientStormType == StormType.NUCLEAR_BLOWOUT) {
                nearDist = 0.5f;
                farDist = 8f;
            }

            if (playerY >= fadeStart) {
                event.setNearPlaneDistance(nearDist);
                event.setFarPlaneDistance(farDist);
                event.setCanceled(true);
            }
            else if (playerY > fadeEnd) {
                float totalDistance = fadeStart - fadeEnd;
                float playerDistance = fadeStart - playerY;
                float depthFactor = playerDistance / totalDistance;

                float startFar = 22f;
                float endFar = 180f;
                float currentFar = startFar + (endFar - startFar) * depthFactor;

                float currentNear = nearDist + (8f - nearDist) * depthFactor;

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

        if (level == null || player == null) return;

        BlockPos pos = player.blockPosition();
        boolean isRaining = level.isRaining() || level.isThundering();

        if (isRaining && (clientStormType == StormType.RAD_RAIN || clientStormType == StormType.NUCLEAR_BLOWOUT)) {
            int playerY = pos.getY();
            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());

            int fadeStart = surfaceY;
            int fadeEnd = surfaceY - 15;

            float r = 0.45f, g = 0.45f, b = 0.45f;

            if (clientStormType == StormType.NUCLEAR_BLOWOUT) {
                r = 0.6f;
                g = 0.1f;
                b = 0.1f;
            }

            if (playerY > surfaceY - 10) {
                event.setRed(r);
                event.setGreen(g);
                event.setBlue(b);
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
