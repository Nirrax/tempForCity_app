package com.sa.services;

import com.sa.clients.GeocodingClient;
import com.sa.clients.WeatherClient;
import com.sa.enums.TemperatureCategory;
import com.sa.enums.TemperatureUnit;
import com.sa.models.Coordinates;
import com.sa.models.Temperature;

import java.util.List;

public class WeatherService {
    private final WeatherClient weatherClient;
    private final GeocodingClient geocodingClient;

    public WeatherService(WeatherClient weatherClient, GeocodingClient geocodingClient) {
        this.weatherClient = weatherClient;
        this.geocodingClient = geocodingClient;
    }

    public Temperature getTemperatureForCity(String cityName, TemperatureUnit temperatureUnit) {
        List<Coordinates> coordinates = geocodingClient.getCoordinates(cityName);
        double temperature = weatherClient.getTemperature(coordinates.getFirst(), temperatureUnit);
        return new Temperature(temperature, temperatureUnit, TemperatureCategory.from(temperature));
    }
}
