package com.waytoearth.controller.v1;

import com.waytoearth.dto.request.feed.FeedCreateRequest;
import com.waytoearth.dto.response.feed.FeedResponse;
import com.waytoearth.dto.response.feed.FeedLikeResponse;
import com.waytoearth.security.AuthUser;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.feed.FeedService;
import com.waytoearth.service.file.FileService;
import com.waytoearth.dto.request.file.PresignRequest;
import com.waytoearth.dto.response.file.PresignResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Feed API", description = "피드 작성, 조회, 좋아요, 삭제, 이미지 업로드 API")
@RestController
@RequestMapping("/v1/feeds")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;
    private final FileService fileService;

    @Operation(summary = "피드 작성")
    @PostMapping
    public ResponseEntity<FeedResponse> createFeed(
            @AuthUser AuthenticatedUser user,
            @RequestBody FeedCreateRequest req
    ) {
        return ResponseEntity.ok(feedService.createFeed(user, req));
    }

    @Operation(summary = "피드 목록 조회 (무한 스크롤)")
    @GetMapping
    public ResponseEntity<List<FeedResponse>> getFeeds(
            @AuthUser AuthenticatedUser user,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "8") int limit
    ) {
        return ResponseEntity.ok(feedService.getFeeds(user, offset, limit));
    }

    @Operation(summary = "피드 단건 조회 (좋아요 여부 포함)")
    @GetMapping("/{feedId}")
    public ResponseEntity<FeedResponse> getFeed(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long feedId
    ) {
        return ResponseEntity.ok(feedService.getFeed(user, feedId));
    }

    @Operation(summary = "피드 삭제")
    @DeleteMapping("/{feedId}")
    public ResponseEntity<?> deleteFeed(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long feedId
    ) {
        feedService.deleteFeed(user, feedId);
        return ResponseEntity.ok().body("{\"message\":\"피드가 삭제되었습니다.\"}");
    }

    @Operation(summary = "피드 좋아요 (토글)")
    @PostMapping("/{feedId}/like")
    public ResponseEntity<FeedLikeResponse> toggleLike(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long feedId
    ) {
        return ResponseEntity.ok(feedService.toggleLike(user, feedId));
    }

    @Operation(summary = "피드 이미지 업로드 Presigned URL 발급")
    @PostMapping("/{feedId}/image/presign")
    public ResponseEntity<PresignResponse> presignImageUpload(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long feedId,
            @RequestBody PresignRequest req
    ) {
        return ResponseEntity.ok(fileService.presignProfile(user.getUserId(), req));
    }
}
