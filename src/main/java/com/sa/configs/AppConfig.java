package com.sa.configs;

public record AppConfig(
        String weatherApiUrl,
        int weatherMaxRetries,
        int weatherRetryDelay,
        String geocodingApiUrl,
        int geocodingMaxRetries,
        int geocodingRetryDelay
) {
    public AppConfig() {
        this(
                getEnv("WEATHER_API_URL"),
                getIntEnv("WEATHER_API_MAX_RETRIES"),
                getIntEnv("WEATHER_API_RETRY_DELAY_MS"),
                getEnv("GEOCODING_API_URL"),
                getIntEnv("GEOCODING_API_MAX_RETRIES"),
                getIntEnv("GEOCODING_API_RETRY_DELAY_MS")
        );
    }

    private static String getEnv(String key) {
        String value = System.getenv(key);
        if (value == null) throw new RuntimeException("Environment variable " + key + " is missing");
        return value;
    }

    private static int getIntEnv(String key) {
        try {
            return Integer.parseInt(getEnv(key));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Environment variable " + key + " must be a number");
        }
    }
}