package com.radsto.radstorms.network;

import com.radsto.radstorms.RadStormsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModMessages {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(RadStormsMod.MOD_ID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(PacketSyncWeather.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(PacketSyncWeather::new)
                .encoder(PacketSyncWeather::toBytes)
                .consumerMainThread(PacketSyncWeather::handle)
                .add();
    }

    public static <MSG> void sendToAllPlayers(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.sendTo(message, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}
