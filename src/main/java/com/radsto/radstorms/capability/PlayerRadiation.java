package com.radsto.radstorms.capability;

import net.minecraft.nbt.CompoundTag;

public class PlayerRadiation {
    private float radiation = 0.0f;
    private final float MAX_RADIATION = 100.0f;

    public float getRadiation() {
        return this.radiation;
    }

    public float getRadiationByPercentage() {
        return (radiation / MAX_RADIATION) * 100;
    }

    public void setRadiation(float value) {
        this.radiation = Math.max(0.0f, Math.min(value, MAX_RADIATION));
    }

    public void addRadiation(float value) {
        setRadiation(this.radiation + value);
    }

    public void subRadiation(float value) {
        setRadiation(this.radiation - value);
    }

    public boolean isMax() {
        return this.radiation >= MAX_RADIATION;
    }

    public void saveNBTData(CompoundTag tag) {
        tag.putFloat("radiation", this.radiation);
    }

    public void loadNBTData(CompoundTag tag) {
        this.radiation = tag.getFloat("radiation");
    }
}
