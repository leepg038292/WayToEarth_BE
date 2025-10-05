package com.waytoearth.entity.notification;

import com.waytoearth.entity.common.BaseTimeEntity;
import com.waytoearth.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * 사용자별 알림 설정
 * - 정기 알림, 크루 알림, 피드 알림 등 ON/OFF
 */
@Entity
@Table(
        name = "notification_settings",
        indexes = {
                @Index(name = "idx_noti_user", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationSetting extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 설정 소유자 */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** 정기 러닝 알림 (오전/저녁) */
    @Column(name = "scheduled_running_reminder", nullable = false)
    @Builder.Default
    private Boolean scheduledRunningReminder = true;

    /** 크루 관련 알림 (가입 승인, 채팅 등) */
    @Column(name = "crew_notification", nullable = false)
    @Builder.Default
    private Boolean crewNotification = true;

    /** 피드 관련 알림 (좋아요, 댓글 등) */
    @Column(name = "feed_notification", nullable = false)
    @Builder.Default
    private Boolean feedNotification = true;

    /** 엠블럼 획득 알림 */
    @Column(name = "emblem_notification", nullable = false)
    @Builder.Default
    private Boolean emblemNotification = true;

    /** 전체 알림 끄기 */
    @Column(name = "all_notifications_enabled", nullable = false)
    @Builder.Default
    private Boolean allNotificationsEnabled = true;

    /**
     * 정기 알림 수신 가능 여부 확인
     */
    public boolean canReceiveScheduledReminder() {
        return allNotificationsEnabled && scheduledRunningReminder;
    }

    /**
     * 크루 알림 수신 가능 여부 확인
     */
    public boolean canReceiveCrewNotification() {
        return allNotificationsEnabled && crewNotification;
    }

    /**
     * 피드 알림 수신 가능 여부 확인
     */
    public boolean canReceiveFeedNotification() {
        return allNotificationsEnabled && feedNotification;
    }

    /**
     * 엠블럼 알림 수신 가능 여부 확인
     */
    public boolean canReceiveEmblemNotification() {
        return allNotificationsEnabled && emblemNotification;
    }
}
