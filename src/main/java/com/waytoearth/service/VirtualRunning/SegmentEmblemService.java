package com.waytoearth.service.VirtualRunning;

import com.waytoearth.dto.response.Virtual.SegmentEmblemResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.stereotype.Service;

@Service
@Schema(description = "세그먼트 엠블럼 서비스")
public class SegmentEmblemService {

    public SegmentEmblemResponse checkEmblem(Long userVirtualCourseId, Long segmentId, Double distance) {
        // TODO: 실제 조건 확인 로직 (예: segment 완주, 누적 거리 milestone)
        if (distance >= 10.0) {
            return new SegmentEmblemResponse(
                    segmentId,
                    "10km 달성!",
                    "EMBLEM_10K"
            );
        }
        return null;
    }
}
