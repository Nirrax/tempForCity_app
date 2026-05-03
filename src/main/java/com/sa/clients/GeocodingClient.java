package com.sa.clients;

import com.sa.models.Coordinates;

import java.util.List;

public interface GeocodingClient {
    List<Coordinates> getCoordinates(String cityName);
}
