package com.waytoearth.controller.v1.notification;

import com.waytoearth.dto.request.notification.FcmTokenRequest;
import com.waytoearth.dto.request.notification.NotificationSettingUpdateRequest;
import com.waytoearth.dto.response.common.ApiResponse;
import com.waytoearth.dto.response.notification.NotificationSettingResponse;
import com.waytoearth.security.AuthUser;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.notification.FcmService;
import com.waytoearth.service.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "알림 API", description = "FCM 토큰 관리 및 알림 설정 API")
@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final FcmService fcmService;

    @Operation(
            summary = "FCM 토큰 등록",
            description = "푸시 알림을 위한 FCM 토큰 등록/갱신",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping("/fcm-token")
    public ResponseEntity<ApiResponse<String>> registerFcmToken(
            @Parameter(hidden = true) @AuthUser AuthenticatedUser user,
            @Parameter(description = "FCM 토큰 등록 요청", required = true)
            @RequestBody @Valid FcmTokenRequest request) {

        notificationService.registerFcmToken(user.getUserId(), request);

        return ResponseEntity.ok(ApiResponse.success(
                null,
                "FCM 토큰이 등록되었습니다."
        ));
    }

    @Operation(
            summary = "FCM 토큰 비활성화",
            description = "특정 디바이스의 FCM 토큰 비활성화 (로그아웃 시)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @DeleteMapping("/fcm-token/{deviceId}")
    public ResponseEntity<ApiResponse<String>> deactivateFcmToken(
            @Parameter(hidden = true) @AuthUser AuthenticatedUser user,
            @Parameter(description = "디바이스 ID", required = true)
            @PathVariable String deviceId) {

        notificationService.deactivateFcmToken(user.getUserId(), deviceId);

        return ResponseEntity.ok(ApiResponse.success(
                null,
                "FCM 토큰이 비활성화되었습니다."
        ));
    }

    @Operation(
            summary = "모든 FCM 토큰 비활성화",
            description = "사용자의 모든 디바이스 FCM 토큰 비활성화",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @DeleteMapping("/fcm-token")
    public ResponseEntity<ApiResponse<String>> deactivateAllTokens(
            @Parameter(hidden = true) @AuthUser AuthenticatedUser user) {

        notificationService.deactivateAllUserTokens(user.getUserId());

        return ResponseEntity.ok(ApiResponse.success(
                null,
                "모든 FCM 토큰이 비활성화되었습니다."
        ));
    }

    @Operation(
            summary = "알림 설정 조회",
            description = "사용자의 알림 설정 조회",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<NotificationSettingResponse>> getNotificationSettings(
            @Parameter(hidden = true) @AuthUser AuthenticatedUser user) {

        NotificationSettingResponse response = notificationService.getNotificationSettings(user.getUserId());

        return ResponseEntity.ok(ApiResponse.success(
                response,
                "알림 설정 조회 성공"
        ));
    }

    @Operation(
            summary = "알림 설정 업데이트",
            description = "사용자의 알림 설정 업데이트",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PatchMapping("/settings")
    public ResponseEntity<ApiResponse<NotificationSettingResponse>> updateNotificationSettings(
            @Parameter(hidden = true) @AuthUser AuthenticatedUser user,
            @Parameter(description = "알림 설정 업데이트 요청", required = true)
            @RequestBody @Valid NotificationSettingUpdateRequest request) {

        NotificationSettingResponse response = notificationService.updateNotificationSettings(
                user.getUserId(),
                request
        );

        return ResponseEntity.ok(ApiResponse.success(
                response,
                "알림 설정이 업데이트되었습니다."
        ));
    }

    @Operation(
            summary = "[테스트] 나에게 푸시알림 전송",
            description = "개발/테스트용: 현재 로그인한 사용자에게 테스트 푸시알림을 즉시 전송합니다.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping("/test/send-to-me")
    public ResponseEntity<ApiResponse<String>> sendTestNotificationToMe(
            @Parameter(hidden = true) @AuthUser AuthenticatedUser user,
            @Parameter(description = "알림 제목 (선택)") @RequestParam(required = false, defaultValue = "테스트 알림") String title,
            @Parameter(description = "알림 내용 (선택)") @RequestParam(required = false, defaultValue = "푸시알림 테스트입니다!") String body) {

        log.info("테스트 알림 전송 요청 - userId: {}, title: {}, body: {}", user.getUserId(), title, body);
        fcmService.sendNotificationToUser(user.getUserId(), title, body);

        return ResponseEntity.ok(ApiResponse.success(
                null,
                "테스트 알림이 전송되었습니다."
        ));
    }

    @Operation(
            summary = "[테스트] 모든 사용자에게 푸시알림 전송",
            description = "개발/테스트용: 모든 활성 사용자에게 테스트 푸시알림을 즉시 전송합니다. (주의: 실제 사용자에게 전송됨)",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping("/test/send-to-all")
    public ResponseEntity<ApiResponse<String>> sendTestNotificationToAll(
            @Parameter(hidden = true) @AuthUser AuthenticatedUser user,
            @Parameter(description = "알림 제목 (선택)") @RequestParam(required = false, defaultValue = "공지사항") String title,
            @Parameter(description = "알림 내용 (선택)") @RequestParam(required = false, defaultValue = "WayToEarth 테스트 알림입니다.") String body) {

        log.info("전체 테스트 알림 전송 요청 - requestedBy: {}, title: {}, body: {}", user.getUserId(), title, body);
        fcmService.sendScheduledRunningReminder(title, body);

        return ResponseEntity.ok(ApiResponse.success(
                null,
                "모든 사용자에게 테스트 알림이 전송되었습니다."
        ));
    }
}
