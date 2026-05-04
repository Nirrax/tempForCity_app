package com.sa.clients.openMeteo;

import com.sa.clients.WeatherClient;
import com.sa.configs.AppConfig;
import com.sa.enums.TemperatureUnit;
import com.sa.exceptions.ExternalServiceException;
import com.sa.exceptions.InvalidResponseException;
import com.sa.exceptions.RequestInterruptedException;
import com.sa.models.Coordinates;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

public class OpenMeteoWeatherClient implements WeatherClient {
    private final HttpClient httpClient;
    private final String apiUrl;
    private final ObjectMapper objectMapper;
    private final int MAX_RETRIES;
    private final int RETRY_DELAY_MS;

    public OpenMeteoWeatherClient(AppConfig config, HttpClient httpClient, ObjectMapper objectMapper) {
        this.apiUrl = config.weatherApiUrl();
        this.MAX_RETRIES = config.weatherMaxRetries();
        this.RETRY_DELAY_MS = config.weatherRetryDelay();
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public double getTemperature(Coordinates coordinates, TemperatureUnit temperatureUnit) {
        String url = this.apiUrl
                + "?latitude=" + coordinates.latitude()
                + "&longitude=" + coordinates.longitude()
                + "&current=temperature_2m"
                + "&temperature_unit=" + temperatureUnit.getValue()
                + "&format=json";

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

        int attempts = 0;
        while (attempts < MAX_RETRIES) {
            try {
                HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    return parseTemperature(response.body());
                }

                if (response.statusCode() >= 400 && response.statusCode() < 500) {
                    throw new ExternalServiceException("Weather API failed: " + response.statusCode());
                }
                attempts++;
            } catch (ExternalServiceException e) {
                throw e;
            } catch (Exception e) {
                attempts++;
                if (attempts >= MAX_RETRIES) {
                    throw new ExternalServiceException("Weather API failed");
                }
            }

            try {
                Thread.sleep(RETRY_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RequestInterruptedException("Weather request interrupted");
            }
        }
        throw new ExternalServiceException("Weather API failed after " + attempts + " retries");
    }

    private double parseTemperature(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            return root.get("current").get("temperature_2m").asDouble();
        } catch (Exception e) {
            throw new InvalidResponseException("Failed to parse temperature response");
        }
    }
}
