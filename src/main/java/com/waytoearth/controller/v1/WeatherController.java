package com.waytoearth.controller.v1;

import com.waytoearth.dto.weather.WeatherCurrentResponse;
import com.waytoearth.service.weather.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Weather", description = "날씨 조회 API")
@RestController
@RequestMapping("/v1/weather")
@RequiredArgsConstructor
@Validated
public class WeatherController {

    private final WeatherService weatherService;

    @Operation(summary = "현재 날씨 조회", description = "좌표(lat, lon) 기반 현재 날씨를 반환합니다.")
    @GetMapping("/current")
    public ResponseEntity<WeatherCurrentResponse> getCurrent(
            @RequestParam @Min(-90) @Max(90) double lat,
            @RequestParam @Min(-180) @Max(180) double lon
    ) {
        return ResponseEntity.ok(weatherService.getCurrent(lat, lon));
    }
}
