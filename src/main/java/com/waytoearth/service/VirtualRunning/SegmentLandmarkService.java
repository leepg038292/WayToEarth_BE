package com.waytoearth.service.VirtualRunning;

import com.waytoearth.dto.response.Virtual.SegmentLandmarkResponse;
import com.waytoearth.repository.VirtualRunning.SegmentLandmarkRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Schema(description = "세그먼트 랜드마크 서비스")
public class SegmentLandmarkService {

    private final SegmentLandmarkRepository segmentLandmarkRepository;

    public List<SegmentLandmarkResponse> getLandmarks(Long segmentId) {
        return segmentLandmarkRepository.findBySegmentId(segmentId).stream()
                .map(lm -> new SegmentLandmarkResponse(
                        lm.getId(),
                        lm.getName(),
                        lm.getLatitude(),
                        lm.getLongitude(),
                        lm.getPhotoUrl(),
                        lm.getDescription()
                ))
                .toList();
    }
}
