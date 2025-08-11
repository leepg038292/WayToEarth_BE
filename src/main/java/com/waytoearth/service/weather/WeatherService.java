package com.waytoearth.service.weather;

import com.waytoearth.dto.response.WeatherCurrentResponse;

public interface WeatherService {
    WeatherCurrentResponse getCurrent(double lat, double lon);
}
