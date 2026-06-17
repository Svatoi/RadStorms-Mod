package com.radsto.radstorms.event;

import com.radsto.radstorms.RadStormsMod;
import com.radsto.radstorms.capability.PlayerRadiation;
import com.radsto.radstorms.capability.PlayerRadiationProvider;
import com.radsto.radstorms.command.RadiationCommand;
import com.radsto.radstorms.network.ModMessages;
import com.radsto.radstorms.network.PacketSyncWeather;
import com.radsto.radstorms.world.RadStormData;
import com.radsto.radstorms.world.StormType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraftforge.event.entity.player.PlayerEvent;
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
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            ServerLevel level = player.serverLevel();

            StormType currentServerStorm = RadStormData.get(level).getCurrentStorm();
            ModMessages.sendToPlayer(
                    new PacketSyncWeather(currentServerStorm.getId()),
                    player
            );
        };
    }

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        RadiationCommand.register(event.getDispatcher());
    }

    public static void subRadiationStage(Player player, Level level, float subRad) {
        player.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).ifPresent(radiation -> {
            if (radiation.getRadiation() != 0) {
                radiation.subRadiation(subRad);
                RadStormsMod.LOGGER.info("The player is being subradiated: " + radiation.getRadiationByPercentage() + "%");

                float radPercentage = radiation.getRadiationByPercentage();
                applyRadiationStage(player, radPercentage, level);
            }
        });
    }

    public static void applyRadiationStage(Player player, float radPercentage, Level level) {
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
