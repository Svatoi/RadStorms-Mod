package com.radsto.radstorms.world;

import com.radsto.radstorms.RadStormsMod;
import com.radsto.radstorms.network.ModMessages;
import com.radsto.radstorms.network.PacketSyncWeather;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RadStormsMod.MOD_ID)
public class RadStormDirector {

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.level.isClientSide() || event.phase != TickEvent.Phase.END) return;

        ServerLevel level = (ServerLevel) event.level;

        if (level.dimension() != Level.OVERWORLD) return;

        if (level.getDayTime() % 24000 == 0) {
            tryTriggerRandomEvent(level);
        }
    }

    private static void tryTriggerRandomEvent(ServerLevel level) {
        RadStormData data = RadStormData.get(level);
        RandomSource random = level.getRandom();

        if (data.isActive()) {
            data.setStormType(StormType.RAD_CONTAMINATION);

            level.setWeatherParameters(0, 0, false, false);
            ModMessages.sendToAllPlayers(new PacketSyncWeather(StormType.RAD_CONTAMINATION.getId()));

            int cooldownDays = 1 + random.nextInt(7);
            data.setDaysUntilNextStorm(cooldownDays);

            level.players().forEach(player -> player.sendSystemMessage(
                    Component.literal("§aШторм утих, остатки радиации витают в воздухе...§r")
            ));
            return;
        }

        if (data.getDaysUntilNextStorm() > 0) {
            data.setDaysUntilNextStorm(data.getDaysUntilNextStorm() - 1);
            RadStormsMod.LOGGER.info("Дней до возможного шторма: " + data.getDaysUntilNextStorm());
            return;
        }


        if (random.nextFloat() < 0.40f) {
            float eventRoll = random.nextFloat();

            if (eventRoll < 0.50f) {
                data.setStormType(StormType.RAD_RAIN);

                level.setWeatherParameters(0, 24000, true, false);

                level.players().forEach(player -> player.sendSystemMessage(
                        Component.literal("§e[Внимание]: Небо затягивают странные зеленоватые тучи§r")
                ));
            } else if (eventRoll < 0.85f) {
                data.setStormType(StormType.SOLAR_FLARE);

                level.setWeatherParameters(24000, 0, false, false);

                level.players().forEach(player -> player.sendSystemMessage(
                        Component.literal("§c[Внимание]: Зафиксирована мощная вспышка на Солнце! Уровень УФ-излучения критический!§r")
                ));
            } else if (eventRoll < 0.95f) {
                data.setStormType(StormType.NUCLEAR_BLOWOUT);
                level.setWeatherParameters(0, 24000, true, true);
                level.players().forEach(player -> player.sendSystemMessage(
                        Component.literal("§4[КАТАСТРОФА]: Датчики зашкаливают! К миру приближается Ядерный Выброс! Срочно ищите глубокое укрытие!§r")
                ));
            } else if (eventRoll < 1f) {
                data.setStormType(StormType.SUPER_SOLAR_APOCALYPSE);
                level.setWeatherParameters(24000, 0, false, false);
                level.players().forEach(player -> player.sendSystemMessage(
                        Component.literal("§4§l[АПОКАЛИПСИС]: Уровень УФ-излучения достигла критического пика! CОЛНЦЕ ВЫЖЫГАЕТ ЗЕМЛЮ!§r")
                ));
            } else {
                data.setStormType(StormType.RAD_CONTAMINATION);
                level.setWeatherParameters(24000, 0, false, false);
                level.players().forEach(player -> player.sendSystemMessage(
                        Component.literal("§6Глобальное радиационное заражение!§r")
                ));
            }
            ModMessages.sendToAllPlayers(new PacketSyncWeather(data.getCurrentStorm().getId()));
        }
    }
}
