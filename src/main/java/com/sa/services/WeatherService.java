package com.sa.services;

import com.sa.clients.GeocodingClient;
import com.sa.clients.WeatherClient;
import com.sa.configs.AppConfig;
import com.sa.enums.TemperatureCategory;
import com.sa.enums.TemperatureUnit;
import com.sa.models.Coordinates;
import com.sa.models.Temperature;

import java.util.List;

public class WeatherService {
    private final WeatherClient weatherClient;
    private final GeocodingClient geocodingClient;
    private final TemperatureUnit temperatureUnit;

    public WeatherService(WeatherClient weatherClient, GeocodingClient geocodingClient, AppConfig config) {
        this.weatherClient = weatherClient;
        this.geocodingClient = geocodingClient;
        this.temperatureUnit = config.getTemperatureUnit();
    }

    public Temperature getTemperatureForCity(String cityName) {
        List<Coordinates> coordinates = geocodingClient.getCoordinates(cityName);
        double temperature = weatherClient.getTemperature(coordinates.getFirst());
        return new Temperature(temperature, temperatureUnit, TemperatureCategory.from(temperature));
    }
}
