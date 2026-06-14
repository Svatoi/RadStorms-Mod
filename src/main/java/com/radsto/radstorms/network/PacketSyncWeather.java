package com.radsto.radstorms.network;

import com.radsto.radstorms.event.ModClientEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSyncWeather {
    private final boolean isStormActive;

    public PacketSyncWeather(boolean isStormActive) {
        this.isStormActive = isStormActive;
    }

    public PacketSyncWeather(FriendlyByteBuf buf) {
        this.isStormActive = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(this.isStormActive);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
           if(context.getDirection().getReceptionSide() == LogicalSide.CLIENT) {
               ModClientEvents.isClientStormActive = this.isStormActive;
           }
        });
        return true;
    }
}
