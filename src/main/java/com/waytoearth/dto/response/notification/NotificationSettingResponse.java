package com.waytoearth.dto.response.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "알림 설정 응답")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingResponse {

    @Schema(description = "정기 러닝 알림", example = "true")
    private Boolean scheduledRunningReminder;

    @Schema(description = "크루 알림", example = "true")
    private Boolean crewNotification;

    @Schema(description = "피드 알림", example = "true")
    private Boolean feedNotification;

    @Schema(description = "엠블럼 알림", example = "true")
    private Boolean emblemNotification;

    @Schema(description = "전체 알림", example = "true")
    private Boolean allNotificationsEnabled;
}
