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

    public String getRecommendation() {
        switch (this) {
            case CLEAR: return "맑아요! 모자와 선크림 준비하고 가볍게 달려요.";
            case PARTLY_CLOUDY: return "구름 조금—달리기 딱 좋아요.";
            case CLOUDY: return "흐려도 컨디션은 굿! 가벼운 바람막이 추천.";
            case RAINY: return "비가 와요. 방수 재킷과 미끄럼 주의!";
            case SNOWY: return "눈길 조심! 트랙션 좋은 신발을 신어주세요.";
            case FOGGY: return "안개—가시성 주의, 밝은 색 착용 권장.";
            case THUNDERSTORM: return "뇌우—실내 러닝으로 대체하는 게 안전합니다.";
            default: return "컨디션 파악 중—몸 상태에 맞춰 무리하지 마세요.";
        }
    }

}