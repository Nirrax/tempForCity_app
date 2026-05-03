package com.sa.configs;

import com.sa.enums.TemperatureUnit;

public class AppConfig {
    private final String weatherApiUrl;
    private final int weatherMaxRetries;
    private final int weatherRetryDelay;
    private final TemperatureUnit temperatureUnit;

    private final String geocodingApiUrl;
    private final int geocodingMaxRetries;
    private final int geocodingRetryDelay;

    public AppConfig() {
        this.weatherApiUrl = getEnv("WEATHER_API_URL");
        this.weatherMaxRetries = getIntEnv("WEATHER_API_MAX_RETRIES");
        this.weatherRetryDelay = getIntEnv("WEATHER_API_RETRY_DELAY_MS");
        this.temperatureUnit = TemperatureUnit.from(getEnv("TEMPERATURE_UNIT"));

        this.geocodingApiUrl = getEnv("GEOCODING_API_URL");
        this.geocodingMaxRetries = getIntEnv("GEOCODING_API_MAX_RETRIES");
        this.geocodingRetryDelay = getIntEnv("GEOCODING_API_RETRY_DELAY_MS");
    }

    private String getEnv(String key) {
        String value = System.getenv(key);
        if (value == null) throw new RuntimeException("Environment variable " + key + " is missing");
        return value;
    }

    private int getIntEnv(String key) {
        try {
            return Integer.parseInt(getEnv(key));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Environment variable " + key + " must be a number");
        }
    }

    public String getWeatherApiUrl() {
        return weatherApiUrl;
    }

    public int getWeatherMaxRetries() {
        return weatherMaxRetries;
    }

    public int getWeatherRetryDelay() {
        return weatherRetryDelay;
    }

    public TemperatureUnit getTemperatureUnit() {
        return temperatureUnit;
    }

    public String getGeocodingApiUrl() {
        return geocodingApiUrl;
    }

    public int getGeocodingMaxRetries() {
        return geocodingMaxRetries;
    }

    public int getGeocodingRetryDelay() {
        return geocodingRetryDelay;
    }
}
