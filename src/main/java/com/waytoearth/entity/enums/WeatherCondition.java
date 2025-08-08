package com.waytoearth.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WeatherCondition {
    CLEAR("맑음", "☀️"),
    PARTLY_CLOUDY("구름조금", "⛅"),
    CLOUDY("흐림", "☁️"),
    RAINY("비", "🌧️"),
    SNOWY("눈", "❄️"),
    FOGGY("안개", "🌫️"),
    THUNDERSTORM("천둥번개", "⛈️"),
    UNKNOWN("알수없음", "❓");

    private final String korean;
    private final String emoji;

    @JsonValue
    public String getKorean() {
        return korean;
    }

    public static WeatherCondition fromKorean(String korean) {
        for (WeatherCondition condition : WeatherCondition.values()) {
            if (condition.korean.equals(korean)) {
                return condition;
            }
        }
        return UNKNOWN;
    }

    // OpenWeatherMap API의 weather condition을 변환
    public static WeatherCondition fromOpenWeatherMain(String main) {
        if (main == null) return UNKNOWN;

        switch (main.toLowerCase()) {
            case "clear":
                return CLEAR;
            case "clouds":
                return CLOUDY;
            case "rain":
            case "drizzle":
                return RAINY;
            case "snow":
                return SNOWY;
            case "mist":
            case "fog":
            case "haze":
                return FOGGY;
            case "thunderstorm":
                return THUNDERSTORM;
            default:
                return UNKNOWN;
        }
    }
}