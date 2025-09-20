package com.waytoearth.controller.v1.Journey;

import com.waytoearth.dto.response.journey.LandmarkDetailResponse;
import com.waytoearth.dto.response.journey.LandmarkSummaryResponse;
import com.waytoearth.dto.response.journey.StoryCardResponse;
import com.waytoearth.entity.enums.StoryType;
import com.waytoearth.service.Journey.LandmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/landmarks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Landmark API", description = "랜드마크 관리 API")
public class LandmarkController {

    private final LandmarkService landmarkService;

    @GetMapping("/{landmarkId}")
    @Operation(summary = "랜드마크 상세 정보", description = "랜드마크의 상세 정보를 조회합니다.")
    public ResponseEntity<LandmarkDetailResponse> getLandmark(
            @Parameter(description = "랜드마크 ID")
            @PathVariable Long landmarkId,
            @Parameter(description = "사용자 ID (스탬프 수집 여부 확인용)")
            @RequestParam(required = false) Long userId) {

        LandmarkDetailResponse response = landmarkService.getLandmarkDetail(landmarkId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{landmarkId}/stories")
    @Operation(summary = "랜드마크의 스토리 카드 목록", description = "랜드마크의 모든 스토리 카드를 조회합니다.")
    public ResponseEntity<List<StoryCardResponse>> getStoryCards(
            @Parameter(description = "랜드마크 ID")
            @PathVariable Long landmarkId,
            @Parameter(description = "스토리 타입 필터")
            @RequestParam(required = false) StoryType type) {

        List<StoryCardResponse> storyCards = (type != null)
                ? landmarkService.getStoryCardsByType(landmarkId, type)
                : landmarkService.getStoryCardsByLandmarkId(landmarkId);

        return ResponseEntity.ok(storyCards);
    }

    @GetMapping("/journey/{journeyId}")
    @Operation(summary = "여행의 랜드마크 목록", description = "특정 여행의 모든 랜드마크를 조회합니다.")
    public ResponseEntity<List<LandmarkSummaryResponse>> getLandmarksByJourney(
            @Parameter(description = "여행 ID")
            @PathVariable Long journeyId) {

        List<LandmarkSummaryResponse> landmarks = landmarkService.getLandmarksByJourneyId(journeyId);
        return ResponseEntity.ok(landmarks);
    }
}