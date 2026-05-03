package com.sa.enums;

import java.util.Arrays;

public enum TemperatureUnit {
    CELSIUS("celsius"),
    FAHRENHEIT("fahrenheit");

    private final String value;

    TemperatureUnit(String value) {
        this.value = value;
    }

    public static TemperatureUnit from(String value) {
        return Arrays.stream(values())
                .filter(u -> u.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid temperature unit: " + value));
    }

    public String getValue() {
        return value;
    }
}
