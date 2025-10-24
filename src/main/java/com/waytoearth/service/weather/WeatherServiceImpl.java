package com.waytoearth.service.weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waytoearth.dto.response.weather.WeatherCurrentResponse;
import com.waytoearth.entity.enums.WeatherCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${weather.openweather.api-key:}")
    private String apiKey;

    @Override
    public WeatherCurrentResponse getCurrent(double lat, double lon) {
        // API 키가 없으면 기본값 반환 (요구사항)
        if (apiKey == null || apiKey.isBlank()) {
            return WeatherCurrentResponse.ofFallback(WeatherCondition.UNKNOWN);
        }

        String url = String.format(
                "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s&units=metric",
                lat, lon, apiKey
        );

        try {
            ResponseEntity<String> res = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(res.getBody());

            // OpenWeather main: "Clear", "Clouds", "Rain", "Snow", "Drizzle", "Thunderstorm", "Mist", ...
            String main = root.path("weather").get(0).path("main").asText("");
            String icon = root.path("weather").get(0).path("icon").asText(""); // e.g. "10d"

            // 온도 정보 파싱 (units=metric이므로 섭씨 온도)
            Double temperature = null;
            if (root.has("main") && root.path("main").has("temp")) {
                temperature = root.path("main").path("temp").asDouble();
            }

            WeatherCondition condition = WeatherCondition.fromOpenWeatherMain(main);

            return WeatherCurrentResponse.builder()
                    .condition(condition)
                    .emoji(condition.getEmoji())
                    .iconCode(icon) // 프론트가 자체 아이콘 쓰면 무시해도 OK
                    .temperature(temperature)
                    .fetchedAt(LocalDateTime.now())
                    .recommendation(condition.getRecommendation()) // 아래 DTO 참고
                    .build();

        } catch (RestClientException | NullPointerException e) {
            // 외부 호출 실패 시 안전한 기본값
            return WeatherCurrentResponse.ofFallback(WeatherCondition.UNKNOWN);
        } catch (Exception e) {
            return WeatherCurrentResponse.ofFallback(WeatherCondition.UNKNOWN);
        }
    }
}
