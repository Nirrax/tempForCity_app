package com.sa.clients;

import com.sa.enums.TemperatureUnit;
import com.sa.models.Coordinates;
import com.sa.models.Temperature;

public interface WeatherClient {
    double getTemperature(Coordinates coordinates, TemperatureUnit temperatureUnit);
}
