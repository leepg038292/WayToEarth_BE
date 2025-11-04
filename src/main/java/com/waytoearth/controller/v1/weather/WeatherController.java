package com.waytoearth.controller.v1.weather;

import com.waytoearth.dto.response.common.ApiResponse;
import com.waytoearth.dto.response.weather.WeatherCurrentResponse;
import com.waytoearth.security.AuthUser;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.weather.WeatherService;
import com.waytoearth.util.WeatherRateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@Tag(name = "Weather", description = "날씨 조회 API")
@RestController
@RequestMapping("/v1/weather")
@RequiredArgsConstructor
@Validated
public class WeatherController {

    private final WeatherService weatherService;
    private final WeatherRateLimiter weatherRateLimiter;

    @Operation(
        summary = "현재 날씨 조회",
        description = "좌표(lat, lon) 기반 현재 날씨를 반환합니다. Rate Limiting: 분당 10회, 초당 3회",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<WeatherCurrentResponse>> getCurrent(
            @AuthUser AuthenticatedUser user,
            @RequestParam @Min(-90) @Max(90) double lat,
            @RequestParam @Min(-180) @Max(180) double lon
    ) {
        // Rate Limiting 체크
        if (!weatherRateLimiter.canRequest(user.getUserId())) {
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(ApiResponse.error("요청이 너무 많습니다. 잠시 후 다시 시도해주세요."));
        }

        WeatherCurrentResponse response = weatherService.getCurrent(lat, lon);

        // Cache-Control 헤더 추가 (클라이언트 측 캐싱 유도)
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES).cachePublic())
                .body(ApiResponse.success(response, "현재 날씨 정보를 성공적으로 조회했습니다."));
    }
}
