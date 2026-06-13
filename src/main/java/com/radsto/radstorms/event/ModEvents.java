package com.radsto.radstorms.event;

import com.radsto.radstorms.RadStormsMod;
import com.radsto.radstorms.capability.PlayerRadiation;
import com.radsto.radstorms.capability.PlayerRadiationProvider;
import com.radsto.radstorms.command.RadiationCommand;
import com.radsto.radstorms.word.RadStormData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
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
                if (RadStormData.get((ServerLevel) level).isActive() || level.isRaining() || level.isThundering()) {
                    int playerY = player.blockPosition().getY();
                    int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, player.blockPosition().getX(), player.blockPosition().getZ());

                    int maxPenetration = 15;
                    int safeZoneY = surfaceY - maxPenetration;

                    if (playerY > safeZoneY) {
                        player.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).ifPresent(radiation -> {
                            radiation.addRadiation(5f);
                            float radPercentage = radiation.getRadiationByPercentage();

                            applyRadiationStage(player, radPercentage, level);
                        });
                    }
                } else {
                    player.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).ifPresent(radiation -> {
                        if (radiation.getRadiation() != 0) {
                            radiation.subRadiation(0.06f);
                            RadStormsMod.LOGGER.info("The player is being subradiated: " + radiation.getRadiationByPercentage() + "%");
                        }
                    });
                }
            }
        }
    }

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        RadiationCommand.register(event.getDispatcher());
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
