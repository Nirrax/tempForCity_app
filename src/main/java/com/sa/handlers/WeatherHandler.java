package com.sa.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.sa.clients.GeocodingClient;
import com.sa.clients.WeatherClient;
import com.sa.clients.openMeteo.OpenMeteoGeocodingClient;
import com.sa.clients.openMeteo.OpenMeteoWeatherClient;
import com.sa.configs.AppConfig;
import com.sa.exceptions.ExternalServiceException;
import com.sa.exceptions.InvalidResponseException;
import com.sa.models.ErrorDTO;
import com.sa.models.Temperature;
import com.sa.services.WeatherService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.util.Map;

public class WeatherHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final WeatherService lambdaService;
    private final ObjectMapper objectMapper;

    public WeatherHandler() {
        AppConfig config = new AppConfig();
        HttpClient httpClient = HttpClient.newHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        GeocodingClient geocodingClient = new OpenMeteoGeocodingClient(config, httpClient, objectMapper);
        WeatherClient weatherClient = new OpenMeteoWeatherClient(config, httpClient, objectMapper);
        this.lambdaService = new WeatherService(weatherClient, geocodingClient, config);
        this.objectMapper = objectMapper;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        String cityName = getQueryParam(event, "cityName");
        if (cityName == null || cityName.isBlank()) {
            return sendResponse(400, new ErrorDTO(400, "Missing query parameter: cityName"));
        }

        try {
            Temperature temperature = lambdaService.getTemperatureForCity(cityName);
            return sendResponse(200, temperature);
        } catch (IllegalArgumentException e) {
            return sendResponse(400, new ErrorDTO(400, "Bad path parameter: cityName"));
        } catch (InvalidResponseException e) {
            return sendResponse(502, new ErrorDTO(502, "Bad External API response"));
        } catch (ExternalServiceException e) {
            return sendResponse(502, new ErrorDTO(502, "External API failure"));
        } catch (Exception e) {
            context.getLogger().log("Unexpected error: " + e.getMessage());
            return sendResponse(500, new ErrorDTO(500, "Internal server error"));
        }
    }

    private String getQueryParam(APIGatewayProxyRequestEvent event, String name) {
        Map<String, String> queryParams = event.getQueryStringParameters();
        return queryParams != null ? queryParams.get(name) : null;
    }

    private <T> APIGatewayProxyResponseEvent sendResponse(int statusCode, T body) {
        return new APIGatewayProxyResponseEvent().withStatusCode(statusCode).withBody(serialize(body));
    }

    private String serialize(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JacksonException e) {
            throw new RuntimeException("Serialization failed", e);
        }
    }
}
