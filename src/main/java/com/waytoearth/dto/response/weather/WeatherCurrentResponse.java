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

    @Schema(description = "기온(°C)", example = "22.7")
    private Double temperatureC;

    @Schema(description = "습도(%)", example = "63")
    private Integer humidity;

    @Schema(description = "OpenWeather 아이콘 코드(선택)", example = "10d")
    private String iconCode;

    @Schema(description = "조회 시각")
    private LocalDateTime fetchedAt;

    @Schema(description = "날씨별 러닝 추천 메시지")
    private String recommendation;

    public static WeatherCurrentResponse ofFallback(WeatherCondition c) {
        return WeatherCurrentResponse.builder()
                .condition(c)
                .temperatureC(null)
                .humidity(null)
                .iconCode(null)
                .fetchedAt(LocalDateTime.now())
                .recommendation(c.getRecommendation())
                .build();
    }
}