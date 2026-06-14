package com.radsto.radstorms.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.radsto.radstorms.capability.PlayerRadiation;
import com.radsto.radstorms.capability.PlayerRadiationProvider;
import com.radsto.radstorms.network.ModMessages;
import com.radsto.radstorms.network.PacketSyncWeather;
import com.radsto.radstorms.world.RadStormData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class RadiationCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("radstorms")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("clear")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();

                            player.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).ifPresent(PlayerRadiation::clearRadiation);

                            context.getSource().sendSuccess(() -> Component.literal("Your radiation has been successfully cleared!"), true);

                            return 1;
                        })
                )
                .then(Commands.literal("weather")
                        .then(Commands.argument("active", BoolArgumentType.bool())
                                .executes(context -> {
                                    boolean isActive = BoolArgumentType.getBool(context, "active");

                                    ServerLevel serverLevel = context.getSource().getLevel();
                                    RadStormData.get(serverLevel).setRadStormActive(isActive);

                                    RadStormData.get(serverLevel).setRadStormActive(isActive);
                                    ModMessages.sendToAllPlayers(new PacketSyncWeather(isActive));

                                    context.getSource().sendSuccess(() -> Component.literal("The radiation storm is set to active: " + isActive), true);

                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("rad_score")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();

                            player.getCapability(PlayerRadiationProvider.PLAYER_RADIATION).ifPresent(radiation -> {
                                context.getSource().sendSuccess(() -> Component.literal("Current radiation level: " + radiation.getRadiation() + " / " + radiation.getRadiationByPercentage() + "%"), true);
                            });

                            return 1;
                        })
                )
        );
    }
}
