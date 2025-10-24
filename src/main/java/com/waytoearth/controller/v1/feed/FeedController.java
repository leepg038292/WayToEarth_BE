package com.waytoearth.controller.v1.feed;

import com.waytoearth.dto.request.feed.FeedCreateRequest;
import com.waytoearth.dto.request.file.PresignRequest;
import com.waytoearth.dto.response.common.ApiResponse;
import com.waytoearth.dto.response.feed.FeedLikeResponse;
import com.waytoearth.dto.response.feed.FeedResponse;
import com.waytoearth.dto.response.file.PresignResponse;
import com.waytoearth.security.AuthUser;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.feed.FeedService;
import com.waytoearth.service.file.FileService;
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
    public ResponseEntity<ApiResponse<FeedResponse>> createFeed(
            @AuthUser AuthenticatedUser user,
            @RequestBody FeedCreateRequest req
    ) {
        FeedResponse response = feedService.createFeed(user, req);
        return ResponseEntity.ok(ApiResponse.success(response, "피드가 성공적으로 작성되었습니다."));
    }

    @Operation(summary = "피드 목록 조회 (무한 스크롤)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<FeedResponse>>> getFeeds(
            @AuthUser AuthenticatedUser user,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "8") int limit
    ) {
        List<FeedResponse> feeds = feedService.getFeeds(user, offset, limit);
        return ResponseEntity.ok(ApiResponse.success(feeds, "피드 목록을 성공적으로 조회했습니다."));
    }

    @Operation(summary = "피드 단건 조회 (좋아요 여부 포함)")
    @GetMapping("/{feedId}")
    public ResponseEntity<ApiResponse<FeedResponse>> getFeed(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long feedId
    ) {
        FeedResponse feed = feedService.getFeed(user, feedId);
        return ResponseEntity.ok(ApiResponse.success(feed, "피드를 성공적으로 조회했습니다."));
    }

    @Operation(summary = "피드 삭제")
    @DeleteMapping("/{feedId}")
    public ResponseEntity<ApiResponse<Void>> deleteFeed(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long feedId
    ) {
        feedService.deleteFeed(user, feedId);
        return ResponseEntity.ok(ApiResponse.success("피드가 성공적으로 삭제되었습니다."));
    }

    @Operation(summary = "피드 좋아요 (토글)")
    @PostMapping("/{feedId}/like")
    public ResponseEntity<ApiResponse<FeedLikeResponse>> toggleLike(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long feedId
    ) {
        FeedLikeResponse response = feedService.toggleLike(user, feedId);
        String message = response.liked() ? "좋아요를 추가했습니다." : "좋아요를 취소했습니다.";
        return ResponseEntity.ok(ApiResponse.success(response, message));
    }

    @Operation(summary = "피드 이미지 업로드 Presigned URL 발급")
    @PostMapping("/{feedId}/image/presign")
    public ResponseEntity<ApiResponse<PresignResponse>> presignImageUpload(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long feedId,
            @RequestBody PresignRequest req
    ) {
        PresignResponse response = fileService.presignProfile(user.getUserId(), req);
        return ResponseEntity.ok(ApiResponse.success(response, "Presigned URL이 성공적으로 발급되었습니다."));
    }
}
