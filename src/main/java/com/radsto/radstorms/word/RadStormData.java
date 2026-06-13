package com.radsto.radstorms.word;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class RadStormData extends SavedData {
    private boolean isRadStormActive = false;

    public boolean isActive() {
        return isRadStormActive;
    }

    public void setRadStormActive (boolean active) {
        this.isRadStormActive = active;
        this.setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("isRadStormActive", isRadStormActive);
        return tag;
    }

    public static RadStormData load(CompoundTag tag) {
        RadStormData data = new RadStormData();

        data.isRadStormActive = tag.getBoolean("isRadStormActive");

        return data;
    }

    public static RadStormData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                RadStormData::load,
                RadStormData::new,
                "radstorms_weather"
        );
    }
}
