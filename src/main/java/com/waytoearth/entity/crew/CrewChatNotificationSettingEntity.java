package com.waytoearth.entity.crew;

import com.waytoearth.entity.common.BaseTimeEntity;
import com.waytoearth.entity.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "crew_chat_notification_settings",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"crew_id", "user_id"}, name = "uk_chat_notification_crew_user")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "크루 채팅 알림 설정")
public class CrewChatNotificationSettingEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "알림 설정 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_id", nullable = false)
    @Schema(description = "크루")
    private CrewEntity crew;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "사용자")
    private User user;

    @Schema(description = "채팅 알림 활성화", example = "true")
    @Column(nullable = false)
    @Builder.Default
    private Boolean isEnabled = true;

    @Schema(description = "알림 타입", example = "ALL")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NotificationType notificationType = NotificationType.ALL;

    @Schema(description = "무음 설정", example = "false")
    @Column(nullable = false)
    @Builder.Default
    private Boolean isMuted = false;

    @Schema(description = "알림 타입")
    public enum NotificationType {
        ALL,           // 모든 메시지
        MENTIONS_ONLY, // 멘션된 메시지만
        ANNOUNCEMENTS  // 공지사항만
    }

    // 편의 메서드
    public boolean shouldNotify(CrewChatEntity.MessageType messageType, boolean isMentioned) {
        if (!isEnabled || isMuted) {
            return false;
        }

        return switch (notificationType) {
            case ALL -> true;
            case MENTIONS_ONLY -> isMentioned;
            case ANNOUNCEMENTS -> messageType == CrewChatEntity.MessageType.ANNOUNCEMENT;
        };
    }
}