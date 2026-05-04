package com.sa.clients.openMeteo;

import com.sa.clients.GeocodingClient;
import com.sa.configs.AppConfig;
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
import java.util.List;

public class OpenMeteoGeocodingClient implements GeocodingClient {
    private final HttpClient httpClient;
    private final String apiUrl;
    private final ObjectMapper objectMapper;
    private final int MAX_RETRIES;
    private final int RETRY_DELAY_MS;

    public OpenMeteoGeocodingClient(AppConfig config, HttpClient httpClient, ObjectMapper objectMapper) {
        this.apiUrl = config.geocodingApiUrl();
        this.MAX_RETRIES = config.geocodingMaxRetries();
        this.RETRY_DELAY_MS = config.geocodingRetryDelay();
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Coordinates> getCoordinates(String cityName) {
        String url = this.apiUrl
                + "?name=" + cityName
                + "&count=5"
                + "&format=json";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        int attempts = 0;
        while (attempts < this.MAX_RETRIES) {
            try {
                HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    List<Coordinates> results = parseCoordinates(response.body());
                    if (results.isEmpty()) {
                        throw new IllegalArgumentException("Geocoding API found no such city: " + cityName);
                    }
                    return results;
                }

                if (response.statusCode() >= 400 && response.statusCode() < 500) {
                    throw new ExternalServiceException("Geocoding API failed: " + response.statusCode());
                }

                attempts++;
            } catch (ExternalServiceException e) {
                throw e;
            } catch (Exception e) {
                attempts++;
                if (attempts >= MAX_RETRIES) {
                    throw new ExternalServiceException("Geocoding API failed");
                }
            }

            try {
                Thread.sleep(RETRY_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RequestInterruptedException("Geocoding request interrupted");
            }
        }
        throw new ExternalServiceException("Geocoding API failed after " + attempts + " attempts");
    }

    private List<Coordinates> parseCoordinates(String json) {
        try {
            JsonNode results = objectMapper.readTree(json).get("results");
            if (results == null || results.isNull()) return List.of();

            return objectMapper.convertValue(results,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Coordinates.class));
        } catch (Exception e) {
            throw new InvalidResponseException("Failed to parse coordinates response");
        }
    }
}
