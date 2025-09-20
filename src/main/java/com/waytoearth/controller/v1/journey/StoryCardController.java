package com.waytoearth.controller.v1.journey;

import com.waytoearth.dto.response.journey.StoryCardResponse;
import com.waytoearth.service.Journey.LandmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/story-cards")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Story Card API", description = "스토리 카드 API")
public class StoryCardController {

    private final LandmarkService landmarkService;

    @GetMapping("/{storyCardId}")
    @Operation(
        summary = "스토리 카드 상세",
        description = """
            스토리 카드의 상세 정보를 조회합니다.

            **조회 정보:**
            - 스토리 제목 및 내용
            - 스토리 이미지
            - 오디오 가이드 URL (있는 경우)
            - 스토리 타입 (HISTORY/CULTURE/NATURE/LOCAL_TIP)
            - 표시 순서

            **스토리 타입:**
            - HISTORY: 역사 정보
            - CULTURE: 문화 정보
            - NATURE: 자연 정보
            - LOCAL_TIP: 현지 팁
            """,
        tags = {"Story Card API"}
    )
    public ResponseEntity<StoryCardResponse> getStoryCard(
            @Parameter(description = "스토리 카드 ID")
            @PathVariable Long storyCardId) {

        StoryCardResponse response = landmarkService.getStoryCardById(storyCardId);
        return ResponseEntity.ok(response);
    }
}