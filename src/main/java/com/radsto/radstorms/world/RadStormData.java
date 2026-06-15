package com.radsto.radstorms.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class RadStormData extends SavedData {
    public StormType currentStorm = StormType.NONE;
    private int daysUntilNextStorm = 0;

    public StormType getCurrentStorm() {
        return currentStorm;
    }
    public boolean isActive() { return currentStorm != StormType.NONE; }

    public void setStormType(StormType type) {
        this.currentStorm = type;
        this.setDirty();
    }

    public int getDaysUntilNextStorm() {return daysUntilNextStorm;}
    public void setDaysUntilNextStorm(int days) {
        this.daysUntilNextStorm = days;
        this.setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("stormTypeId", currentStorm.getId());
        tag.putInt("daysUntilNextStorm", daysUntilNextStorm);
        return tag;
    }

    public static RadStormData load(CompoundTag tag) {
        RadStormData data = new RadStormData();
        data.currentStorm = StormType.fromId(tag.getInt("stormTypeId"));
        data.daysUntilNextStorm = tag.getInt("daysUntilNextStorm");
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
