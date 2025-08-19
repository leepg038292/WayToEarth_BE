package com.waytoearth.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
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

    private final String label;
    private final String emoji;

    // 응답(JSON)엔 한글로 나감
    @JsonValue
    public String getLabel() { return label; }

    // 요청(JSON)에서도 한글만 받음
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static WeatherCondition fromJson(String value) {
        if (value == null) return UNKNOWN;
        for (WeatherCondition c : values()) {
            if (c.label.equals(value)) return c;   // "맑음" 등 한글만 허용
        }
        return UNKNOWN; // 모르면 UNKNOWN 처리 (400 내리고 싶으면 예외 던져도 됨)
    }

    // OpenWeather 변환(내부용) - 그대로 유지
    public static WeatherCondition fromOpenWeatherMain(String main) {
        if (main == null) return UNKNOWN;
        switch (main.toLowerCase()) {
            case "clear":        return CLEAR;
            case "clouds":       return CLOUDY;
            case "rain":
            case "drizzle":      return RAINY;
            case "snow":         return SNOWY;
            case "mist":
            case "fog":
            case "haze":         return FOGGY;
            case "thunderstorm": return THUNDERSTORM;
            default:             return UNKNOWN;
        }
    }

    public String getRecommendation() {
        switch (this) {
            case CLEAR:        return "맑아요! 모자와 선크림 준비하고 가볍게 달려요.";
            case PARTLY_CLOUDY:return "구름 조금—달리기 딱 좋아요.";
            case CLOUDY:       return "흐려도 컨디션은 굿! 가벼운 바람막이 추천.";
            case RAINY:        return "비가 와요. 방수 재킷과 미끄럼 주의!";
            case SNOWY:        return "눈길 조심! 트랙션 좋은 신발을 신어주세요.";
            case FOGGY:        return "안개—가시성 주의, 밝은 색 착용 권장.";
            case THUNDERSTORM: return "뇌우—실내 러닝으로 대체하는 게 안전합니다.";
            default:           return "컨디션 파악 중—몸 상태에 맞춰 무리하지 마세요.";
        }
    }
}
