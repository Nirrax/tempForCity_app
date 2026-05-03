package com.sa.models;

import com.sa.enums.TemperatureCategory;
import com.sa.enums.TemperatureUnit;

public record Temperature(Double temperature, TemperatureUnit unit, TemperatureCategory category) {
}
