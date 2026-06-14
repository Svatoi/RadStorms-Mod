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

            if (playerY > surfaceY - 10) {
                event.setNearPlaneDistance(1.3f);
                event.setFarPlaneDistance(22f);
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

        if (isRaining) {
            int playerY = pos.getY();
            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());

            if (playerY > surfaceY - 10) {
                event.setRed(0.45f);
                event.setGreen(0.45f);
                event.setBlue(0.45f);
            }
        }
    }
}
