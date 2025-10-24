package com.waytoearth.entity.notification;

import com.waytoearth.entity.common.BaseTimeEntity;
import com.waytoearth.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * FCM 디바이스 토큰 엔티티
 * - 사용자 디바이스별 푸시 알림 토큰 관리
 * - 한 사용자가 여러 디바이스 가능 (폰, 태블릿 등)
 */
@Entity
@Table(
        name = "fcm_tokens",
        indexes = {
                @Index(name = "idx_fcm_user", columnList = "user_id"),
                @Index(name = "idx_fcm_token", columnList = "fcm_token"),
                @Index(name = "idx_fcm_active", columnList = "is_active")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_fcm_user_device", columnNames = {"user_id", "device_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FcmToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 토큰 소유자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** FCM 디바이스 토큰 (Firebase가 발급) */
    @Column(name = "fcm_token", nullable = false, length = 500)
    private String fcmToken;

    /** 디바이스 고유 ID (앱에서 생성) */
    @Column(name = "device_id", nullable = false, length = 100)
    private String deviceId;

    /** 디바이스 타입 */
    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", length = 20)
    private DeviceType deviceType;

    /** 활성화 여부 (로그아웃 시 false) */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    public enum DeviceType {
        IOS,
        ANDROID
    }

    /**
     * 토큰 업데이트 (같은 디바이스에서 토큰 갱신 시)
     */
    public void updateToken(String newToken) {
        this.fcmToken = newToken;
        this.isActive = true;
    }

    /**
     * 비활성화 (로그아웃 시)
     */
    public void deactivate() {
        this.isActive = false;
    }
}
