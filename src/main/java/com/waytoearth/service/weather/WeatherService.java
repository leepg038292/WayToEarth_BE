package com.waytoearth.service.weather;

import com.waytoearth.dto.response.weather.WeatherCurrentResponse;

public interface WeatherService {
    WeatherCurrentResponse getCurrent(double lat, double lon);
}
