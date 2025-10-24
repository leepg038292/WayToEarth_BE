package com.waytoearth.dto.response.weather;

import com.waytoearth.entity.enums.WeatherCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "현재 날씨 응답")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherCurrentResponse {

    @Schema(description = "날씨 컨디션", example = "CLEAR")
    private WeatherCondition condition;

    @Schema(description = "OpenWeather 아이콘 코드(선택)", example = "10d")
    private String iconCode;

    @Schema(description = "날씨 이모지", example = "☁️")
    private String emoji;

    @Schema(description = "현재 온도(°C)", example = "23.5")
    private Double temperature;

    @Schema(description = "조회 시각")
    private LocalDateTime fetchedAt;

    @Schema(description = "날씨별 러닝 추천 메시지")
    private String recommendation;

    public static WeatherCurrentResponse ofFallback(WeatherCondition c) {
        return WeatherCurrentResponse.builder()
                .condition(c)
                .emoji(c.getEmoji())
                .iconCode(null)
                .temperature(null)
                .fetchedAt(LocalDateTime.now())
                .recommendation(c.getRecommendation())
                .build();
    }
}