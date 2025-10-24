package com.waytoearth.controller.v1.journey;

import com.waytoearth.dto.request.file.PresignRequest;
import com.waytoearth.dto.response.common.ApiResponse;
import com.waytoearth.dto.response.file.PresignResponse;
import com.waytoearth.dto.response.journey.StoryCardResponse;
import com.waytoearth.security.AuthUser;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.file.FileService;
import com.waytoearth.service.journey.LandmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    private final FileService fileService;

    @GetMapping("/{storyCardId}")
    @Operation(
        summary = "스토리 카드 상세",
        description = """
            스토리 카드의 상세 정보를 조회합니다.

            조회 정보:
            - 스토리 제목 및 내용
            - 이미지: 대표(커버) 이미지 `imageUrl`, 갤러리 `images[]`
            - 스토리 타입 (HISTORY/CULTURE/NATURE)
            - 표시 순서
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
