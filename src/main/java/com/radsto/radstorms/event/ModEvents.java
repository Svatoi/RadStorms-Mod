package com.radsto.radstorms.event;

import com.radsto.radstorms.RadStormsMod;
import com.radsto.radstorms.capability.PlayerRadiation;
import com.radsto.radstorms.capability.PlayerRadiationProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
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
    public static void onPlayerTick(PlayerTickEvent event) {
        if(event.side.isServer() && event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            Level level = player.level();

            if (player.tickCount % 10 == 0) {
                if (level.isRaining() || level.isThundering()) {
                    int playerY = player.blockPosition().getY();
                    int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, player.blockPosition().getX(), player.blockPosition().getZ());

                    int maxPenetration = 15;
                    int safeZoneY = surfaceY - maxPenetration;

                    if (playerY > safeZoneY) {
                        player.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).ifPresent(radiation -> {
                            radiation.addRadiation(0.5f);

                            RadStormsMod.LOGGER.info("The player is being irradiated! Current radiation level: " + radiation.getRadiation());
                            if (radiation.isMax()) {
                                player.hurt(level.damageSources().magic(), 1.0f);
                            }
                        });
                    }
                }
            }
        }
    }
}
