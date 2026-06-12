package com.radsto.radstorms.event;

import com.radsto.radstorms.RadStormsMod;
import com.radsto.radstorms.capability.PlayerRadiation;
import com.radsto.radstorms.capability.PlayerRadiationProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
                            radiation.addRadiation(0.08f);
                            float radPercentage = radiation.getRadiationByPercentage();

                            RadStormsMod.LOGGER.info("The player is being irradiated! Current radiation level: " + radiation.getRadiation());
                            RadStormsMod.LOGGER.info("Current radiation level by percentage: " + radiation.getRadiationByPercentage() + "%");

                            if (radPercentage >= 5 && radPercentage <= 50) {
                                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 30, 0, false, true));
                            }
                            if (radPercentage >= 20) {
                                player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 30, 5, false, true));
                                player.hurt(level.damageSources().magic(), 1.0f);
                            }
                            if (radPercentage >= 50 && radPercentage <= 80) {
                                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30, 2, false, true));
                                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 30, 1, false, true));
                                player.hurt(level.damageSources().magic(), 2.5f);
                            }
                            if (radPercentage >= 80) {
                                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 1, false, true));
                                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 30, 2, false, true));
                                player.hurt(level.damageSources().magic(), 4.3f);
                            }
                        });
                    }
                } else {
                    player.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).ifPresent(radiation -> {
                        radiation.subRadiation(0.06f);

                        RadStormsMod.LOGGER.info("The player is being subradiated: " + radiation.getRadiationByPercentage() + "%");
                    });
                }
            }
        }
    }
}
