package com.radsto.radstorms.world;

public enum StormType {
    NONE(0),
    RAD_RAIN(1),
    SOLAR_FLARE(2),
    NUCLEAR_BLOWOUT(3),
    SUPER_SOLAR_APOCALYPSE(4);

    private final int id;

    StormType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static StormType fromId(int id) {
        for (StormType type : StormType.values()) {
            if (type.id == id) return type;
        }
        return NONE;
    }
}

