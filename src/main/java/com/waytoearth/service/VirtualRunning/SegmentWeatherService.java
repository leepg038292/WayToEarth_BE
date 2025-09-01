package com.waytoearth.service.VirtualRunning;

import com.waytoearth.dto.response.Virtual.SegmentWeatherResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.stereotype.Service;

@Service
@Schema(description = "세그먼트 날씨 서비스")
public class SegmentWeatherService {

    public SegmentWeatherResponse getWeather(Long segmentId) {
        // TODO: segment 좌표 조회 후 실제 날씨 API 연동
        // 지금은 Mock 데이터 반환
        return new SegmentWeatherResponse(
                segmentId,
                "맑음",
                26.5,
                2.3
        );
    }
}
