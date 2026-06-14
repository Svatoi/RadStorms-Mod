package com.radsto.radstorms.event;

import com.radsto.radstorms.RadStormsMod;
import com.radsto.radstorms.capability.PlayerRadiation;
import com.radsto.radstorms.capability.PlayerRadiationProvider;
import com.radsto.radstorms.command.RadiationCommand;
import com.radsto.radstorms.world.RadStormData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();

        if(!level.isClientSide() && entity.tickCount % 10 == 0) {
            ServerLevel serverLevel = (ServerLevel) level;
            boolean isStormActive = RadStormData.get(serverLevel).isActive();

            int entityY = entity.blockPosition().getY();
            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, entity.blockPosition().getX(), entity.blockPosition().getZ());

            float radDamage = 0.5f; // for realise 0.5f

            if (entity instanceof Player player) {
                if (isStormActive && (level.isRaining() || level.isThundering() ||
                                (level.isDay() && level.canSeeSky(player.blockPosition())))) {
                    if (entityY > surfaceY - 15) {
                        if (level.isDay() && level.canSeeSky(player.blockPosition())) {
                            radDamage = 0.3f;
                        }

                        float finalRadDamage = radDamage;
                        player.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).ifPresent(radiation -> {
                            radiation.addRadiation(finalRadDamage); // for realis 0.3
                            float radPercentage = radiation.getRadiationByPercentage();
                            applyRadiationStage(player, radPercentage, level);
                        });
                    } else if (entityY < surfaceY + 15) {
                        subRadiationStage(player, level);
                    }
                } else {
                    subRadiationStage(player, level);
                }
            } else {
                if (isStormActive && (level.isRaining() || level.isThundering() ||
                        (level.isDay() && level.canSeeSky(entity.blockPosition())))) {
                    if (entityY > surfaceY - 15) {
                        if (entity.getMobType() != MobType.UNDEAD && !(entity instanceof Enemy)) {
                            entity.hurt(level.damageSources().magic(), 0.5f);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        RadiationCommand.register(event.getDispatcher());
    }

    private static void subRadiationStage(Player player, Level level) {
        player.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).ifPresent(radiation -> {
            if (radiation.getRadiation() != 0) {
                radiation.subRadiation(0.06f);
                RadStormsMod.LOGGER.info("The player is being subradiated: " + radiation.getRadiationByPercentage() + "%");

                float radPercentage = radiation.getRadiationByPercentage();
                applyRadiationStage(player, radPercentage, level);
            }
        });
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
