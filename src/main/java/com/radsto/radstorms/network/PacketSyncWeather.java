package com.radsto.radstorms.network;

import com.radsto.radstorms.event.ModClientEvents;
import com.radsto.radstorms.world.StormType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSyncWeather {
    private final int stormId;

    public PacketSyncWeather(int stormId) {
        this.stormId = stormId;
    }

    public PacketSyncWeather(FriendlyByteBuf buf) {
        this.stormId = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.stormId);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
           if(context.getDirection().getReceptionSide() == LogicalSide.CLIENT) {
               ModClientEvents.clientStormType = StormType.fromId(this.stormId);
           }
        });
        return true;
    }
}
