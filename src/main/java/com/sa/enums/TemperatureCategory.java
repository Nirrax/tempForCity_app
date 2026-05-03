package com.sa.enums;

public enum TemperatureCategory {
    FREEZING, COLD, MILD, WARM, HOT;

    public static TemperatureCategory from(Double temperature) {
        if (temperature < 0) return FREEZING;
        if (temperature < 10) return COLD;
        if (temperature < 20) return MILD;
        if (temperature < 30) return WARM;
        return HOT;
    }
}
