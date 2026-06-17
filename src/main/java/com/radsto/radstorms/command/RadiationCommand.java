package com.radsto.radstorms.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.radsto.radstorms.capability.PlayerRadiation;
import com.radsto.radstorms.capability.PlayerRadiationProvider;
import com.radsto.radstorms.event.ModEvents;
import com.radsto.radstorms.network.ModMessages;
import com.radsto.radstorms.network.PacketSyncWeather;
import com.radsto.radstorms.world.RadStormData;
import com.radsto.radstorms.world.StormType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.Collection;

public class RadiationCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("radstorms")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("clear")
                        .then(Commands.argument("targets", EntityArgument.players())
                        .executes(context -> {
                            Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");

                            for (ServerPlayer target : targets) {
                                target.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).ifPresent(PlayerRadiation::clearRadiation);
                            }

                            context.getSource().sendSuccess(() -> Component.literal("Your radiation has been successfully cleared!"), true);

                            return 1;
                        }))
                )
                .then(Commands.literal("weather")
                        .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        Arrays.stream(StormType.values()).map(Enum::name).map(String::toLowerCase),
                                        builder
                                ))
                                .executes(RadiationCommand::changeWeather)
                        )
                )
                .then(Commands.literal("rad_score")
                        .then(Commands.argument("targets", EntityArgument.players())
                        .executes(context -> {
                            Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");

                            for (ServerPlayer target : targets) {
                                target.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).ifPresent(radiation -> {
                                    context.getSource().sendSuccess(() -> Component.literal("Current radiation level: " + radiation.getRadiation() + " / " + radiation.getRadiationByPercentage() + "%"), true);
                                });
                            }

                            return 1;
                        }))
                )
                .then(Commands.literal("add_rad")
                        .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                        .executes(context -> {
                            Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");

                            int amount = IntegerArgumentType.getInteger(context, "amount");

                            for (ServerPlayer target : targets) {
                                target.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).ifPresent(radiation -> {
                                    radiation.addRadiation(amount);
                                    ModEvents.applyRadiationStage(target, radiation.getRadiationByPercentage(), target.level());
                                });
                            }

                            context.getSource().sendSuccess(() -> Component.literal("Radiation has increased by: " + amount), true);

                            return 1;
                        })))
                )
        );
    }

    private static int changeWeather(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();

        String typeString = StringArgumentType.getString(context, "type").toUpperCase();

        try {
            StormType targetStorm = StormType.valueOf(typeString);
            RadStormData data = RadStormData.get(level);

            data.setStormType(targetStorm);

            if (targetStorm == StormType.NONE) {
                data.setStormType(StormType.NONE);

                data.setDaysUntilNextStorm(99);

                level.setWeatherParameters(0, 0, false, false);
                ModMessages.sendToAllPlayers(new PacketSyncWeather(StormType.NONE.getId()));

                source.sendSuccess(() -> Component.literal("§aПогода очищена. Все штормы выключены.§r"), true);
            } else if (targetStorm == StormType.RAD_RAIN) {
                level.setWeatherParameters(0, 24000, true, false);
                source.sendSuccess(() -> Component.literal("§eВключен радиационный ливень!§r"), true);
            } else if (targetStorm == StormType.SOLAR_FLARE) {
                level.setWeatherParameters(24000, 0, false, false);
                source.sendSuccess(() -> Component.literal("§cВключена солнечная вспышка!§r"), true);
            } else if (targetStorm == StormType.NUCLEAR_BLOWOUT) {
                level.setWeatherParameters(0, 0, true, true);
                source.sendSuccess(() -> Component.literal("§4Запущен ядерный выброс! Мобы мутируют!§r"), true);
            } else if (targetStorm == StormType.SUPER_SOLAR_APOCALYPSE) {
                level.setWeatherParameters(24000, 0, false, false);
                source.sendSuccess(() -> Component.literal("§4§lАПОКАЛИПСИС: Солнце выжигает этот мир!§r"), true);
            } else if (targetStorm == StormType.RAD_CONTAMINATION) {
                level.setWeatherParameters(24000, 0, false, false);
                source.sendSuccess(() -> Component.literal("§6Глобальное радиационное заражение!§r"), true);
            }

            ModMessages.sendToAllPlayers(new PacketSyncWeather(targetStorm.getId()));
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.literal("§cНеизвестный тип шторма! Используйте: none, rad_rain, solar_flare, nuclear_blowout§r"));
        }

        return 1;
    }
}
