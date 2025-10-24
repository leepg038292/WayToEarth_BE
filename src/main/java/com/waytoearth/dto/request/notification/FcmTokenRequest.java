package com.waytoearth.dto.request.notification;

import com.waytoearth.entity.notification.FcmToken.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "FCM 토큰 등록 요청")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FcmTokenRequest {

    @Schema(description = "FCM 토큰", example = "dXGfz1234...", required = true)
    @NotBlank(message = "FCM 토큰은 필수입니다")
    private String fcmToken;

    @Schema(description = "디바이스 ID (고유 식별자)", example = "device-uuid-1234", required = true)
    @NotBlank(message = "디바이스 ID는 필수입니다")
    private String deviceId;

    @Schema(description = "디바이스 타입", example = "ANDROID", required = true)
    @NotNull(message = "디바이스 타입은 필수입니다")
    private DeviceType deviceType;
}
