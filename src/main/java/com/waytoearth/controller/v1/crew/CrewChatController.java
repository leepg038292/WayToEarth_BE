package com.waytoearth.controller.v1.crew;

import com.waytoearth.dto.response.common.ApiResponse;
import com.waytoearth.dto.response.crew.CrewChatMessageDto;
import com.waytoearth.entity.crew.CrewChatNotificationSettingEntity;
import com.waytoearth.security.AuthUser;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.crew.CrewChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/crews/{crewId}/chat")
@RequiredArgsConstructor
@Tag(name = "크루 채팅", description = "크루 채팅 관련 API")
@Slf4j
public class CrewChatController {

    private final CrewChatService crewChatService;

    @GetMapping("/messages")
    @Operation(summary = "채팅 메시지 목록 조회", description = "크루의 채팅 메시지를 페이지네이션으로 조회합니다.")
    public ResponseEntity<ApiResponse<Page<CrewChatMessageDto>>> getChatMessages(
            @AuthUser AuthenticatedUser me,
            @PathVariable @Parameter(description = "크루 ID") Long crewId,
            @PageableDefault(size = 50, sort = "sentAt") Pageable pageable) {

        Page<CrewChatMessageDto> messages = crewChatService.getChatMessages(crewId, me.getUserId(), pageable);

        return ResponseEntity.ok(ApiResponse.success(messages, "채팅 메시지를 조회했습니다."));
    }

    @GetMapping("/messages/recent")
    @Operation(summary = "최근 채팅 메시지 조회", description = "크루의 최근 채팅 메시지를 조회합니다.")
    public ResponseEntity<ApiResponse<List<CrewChatMessageDto>>> getRecentMessages(
            @AuthUser AuthenticatedUser me,
            @PathVariable @Parameter(description = "크루 ID") Long crewId,
            @RequestParam(defaultValue = "20") @Parameter(description = "조회할 메시지 수") int limit) {

        List<CrewChatMessageDto> messages = crewChatService.getRecentMessages(crewId, me.getUserId(), limit);

        return ResponseEntity.ok(ApiResponse.success(messages, "최근 채팅 메시지를 조회했습니다."));
    }

    @PostMapping("/messages/{messageId}/read")
    @Operation(summary = "메시지 읽음 처리", description = "특정 메시지를 읽음으로 표시합니다.")
    public ResponseEntity<ApiResponse<Void>> markMessageAsRead(
            @AuthUser AuthenticatedUser me,
            @PathVariable @Parameter(description = "크루 ID") Long crewId,
            @PathVariable @Parameter(description = "메시지 ID") Long messageId) {

        crewChatService.markMessageAsRead(messageId, me.getUserId());

        return ResponseEntity.ok(ApiResponse.success(null, "메시지를 읽음으로 처리했습니다."));
    }

    @PostMapping("/messages/read/batch")
    @Operation(summary = "다중 메시지 읽음 처리", description = "여러 메시지를 읽음으로 표시합니다.")
    public ResponseEntity<ApiResponse<Void>> markMessagesAsRead(
            @AuthUser AuthenticatedUser me,
            @PathVariable @Parameter(description = "크루 ID") Long crewId,
            @RequestBody @Parameter(description = "메시지 ID 목록") List<Long> messageIds) {

        crewChatService.markMessagesAsRead(crewId, me.getUserId(), messageIds);

        return ResponseEntity.ok(ApiResponse.success(null, "메시지들을 읽음으로 처리했습니다."));
    }

    @PostMapping("/messages/read/all-after/{afterMessageId}")
    @Operation(summary = "특정 시점 이후 모든 메시지 읽음 처리", description = "특정 메시지 이후의 모든 메시지를 읽음으로 표시합니다.")
    public ResponseEntity<ApiResponse<Void>> markAllMessagesAsReadAfter(
            @AuthUser AuthenticatedUser me,
            @PathVariable @Parameter(description = "크루 ID") Long crewId,
            @PathVariable @Parameter(description = "기준 메시지 ID") Long afterMessageId) {

        crewChatService.markAllMessagesAsReadAfter(crewId, me.getUserId(), afterMessageId);

        return ResponseEntity.ok(ApiResponse.success(null, "모든 메시지를 읽음으로 처리했습니다."));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "읽지 않은 메시지 수 조회", description = "사용자가 읽지 않은 메시지 수를 조회합니다.")
    public ResponseEntity<ApiResponse<Integer>> getUnreadMessageCount(
            @AuthUser AuthenticatedUser me,
            @PathVariable @Parameter(description = "크루 ID") Long crewId) {

        int unreadCount = crewChatService.getUnreadMessageCount(crewId, me.getUserId());

        return ResponseEntity.ok(ApiResponse.success(unreadCount, "읽지 않은 메시지 수를 조회했습니다."));
    }

    @DeleteMapping("/messages/{messageId}")
    @Operation(summary = "메시지 삭제", description = "채팅 메시지를 삭제합니다. (작성자 또는 크루장만 가능)")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @AuthUser AuthenticatedUser me,
            @PathVariable @Parameter(description = "크루 ID") Long crewId,
            @PathVariable @Parameter(description = "메시지 ID") Long messageId) {

        crewChatService.deleteMessage(messageId, me.getUserId());

        return ResponseEntity.ok(ApiResponse.success(null, "메시지를 삭제했습니다."));
    }

    @GetMapping("/notification-settings")
    @Operation(summary = "알림 설정 조회", description = "사용자의 채팅 알림 설정을 조회합니다.")
    public ResponseEntity<ApiResponse<CrewChatNotificationSettingEntity>> getNotificationSettings(
            @AuthUser AuthenticatedUser me,
            @PathVariable @Parameter(description = "크루 ID") Long crewId) {

        CrewChatNotificationSettingEntity settings = crewChatService.getNotificationSetting(crewId, me.getUserId());

        return ResponseEntity.ok(ApiResponse.success(settings, "알림 설정을 조회했습니다."));
    }

    @PutMapping("/notification-settings")
    @Operation(summary = "알림 설정 업데이트", description = "사용자의 채팅 알림 설정을 업데이트합니다.")
    public ResponseEntity<ApiResponse<CrewChatNotificationSettingEntity>> updateNotificationSettings(
            @AuthUser AuthenticatedUser me,
            @PathVariable @Parameter(description = "크루 ID") Long crewId,
            @RequestBody @Valid NotificationSettingsRequest request) {

        CrewChatNotificationSettingEntity settings = crewChatService.updateNotificationSetting(
                crewId, me.getUserId(), request.isEnabled, request.notificationType, request.isMuted);

        return ResponseEntity.ok(ApiResponse.success(settings, "알림 설정을 업데이트했습니다."));
    }

    public static class NotificationSettingsRequest {
        @Parameter(description = "알림 활성화 여부", example = "true")
        public boolean isEnabled;

        @Parameter(description = "알림 타입", example = "ALL")
        public CrewChatNotificationSettingEntity.NotificationType notificationType;

        @Parameter(description = "무음 설정 여부", example = "false")
        public boolean isMuted;
    }
}