package com.sa.clients;

import com.sa.models.Coordinates;

public interface WeatherClient {
    double getTemperature(Coordinates coordinates);
}
