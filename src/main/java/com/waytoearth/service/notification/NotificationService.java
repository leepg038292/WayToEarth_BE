package com.waytoearth.service.notification;

import com.waytoearth.dto.request.notification.FcmTokenRequest;
import com.waytoearth.dto.request.notification.NotificationSettingUpdateRequest;
import com.waytoearth.dto.response.notification.NotificationSettingResponse;
import com.waytoearth.entity.notification.FcmToken;
import com.waytoearth.entity.notification.NotificationSetting;
import com.waytoearth.entity.user.User;
import com.waytoearth.exception.UserNotFoundException;
import com.waytoearth.repository.notification.FcmTokenRepository;
import com.waytoearth.repository.notification.NotificationSettingRepository;
import com.waytoearth.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserRepository userRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final NotificationSettingRepository notificationSettingRepository;

    /**
     * FCM 토큰 등록/갱신
     */
    @Transactional
    public void registerFcmToken(Long userId, FcmTokenRequest request) {
        // Expo 토큰 검증 (FCM 토큰만 허용)
        if (request.getFcmToken() != null && request.getFcmToken().startsWith("ExponentPushToken")) {
            log.warn("Expo 토큰은 등록할 수 없습니다. userId={}, token={}", userId, request.getFcmToken());
            throw new IllegalArgumentException("유효하지 않은 FCM 토큰 형식입니다. Expo 토큰은 지원되지 않습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 알림 설정이 없으면 자동 생성
        ensureNotificationSettingExists(user);

        // 기존 토큰 조회
        FcmToken existingToken = fcmTokenRepository.findByUserAndDeviceId(user, request.getDeviceId())
                .orElse(null);

        if (existingToken != null) {
            // 토큰 갱신
            existingToken.setFcmToken(request.getFcmToken());
            existingToken.setDeviceType(request.getDeviceType());
            existingToken.setIsActive(true);
            fcmTokenRepository.save(existingToken);
            log.info("FCM 토큰 갱신: userId={}, deviceId={}", userId, request.getDeviceId());
        } else {
            // 신규 토큰 등록
            FcmToken newToken = FcmToken.builder()
                    .user(user)
                    .fcmToken(request.getFcmToken())
                    .deviceId(request.getDeviceId())
                    .deviceType(request.getDeviceType())
                    .isActive(true)
                    .build();
            fcmTokenRepository.save(newToken);
            log.info("FCM 토큰 등록: userId={}, deviceId={}", userId, request.getDeviceId());
        }
    }

    /**
     * FCM 토큰 비활성화 (로그아웃)
     */
    @Transactional
    public void deactivateFcmToken(Long userId, String deviceId) {
        fcmTokenRepository.deactivateUserDeviceToken(userId, deviceId);
        log.info("FCM 토큰 비활성화: userId={}, deviceId={}", userId, deviceId);
    }

    /**
     * 사용자의 모든 FCM 토큰 비활성화
     */
    @Transactional
    public void deactivateAllUserTokens(Long userId) {
        fcmTokenRepository.deactivateAllUserTokens(userId);
        log.info("모든 FCM 토큰 비활성화: userId={}", userId);
    }

    /**
     * 알림 설정 조회
     */
    @Transactional(readOnly = true)
    public NotificationSettingResponse getNotificationSettings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        NotificationSetting setting = notificationSettingRepository.findByUser(user)
                .orElseGet(() -> createDefaultSettings(user));

        return NotificationSettingResponse.builder()
                .scheduledRunningReminder(setting.getScheduledRunningReminder())
                .crewNotification(setting.getCrewNotification())
                .feedNotification(setting.getFeedNotification())
                .emblemNotification(setting.getEmblemNotification())
                .allNotificationsEnabled(setting.getAllNotificationsEnabled())
                .build();
    }

    /**
     * 알림 설정 업데이트
     */
    @Transactional
    public NotificationSettingResponse updateNotificationSettings(Long userId, NotificationSettingUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        NotificationSetting setting = notificationSettingRepository.findByUser(user)
                .orElseGet(() -> createDefaultSettings(user));

        // 요청에 포함된 값만 업데이트
        if (request.getScheduledRunningReminder() != null) {
            setting.setScheduledRunningReminder(request.getScheduledRunningReminder());
        }
        if (request.getCrewNotification() != null) {
            setting.setCrewNotification(request.getCrewNotification());
        }
        if (request.getFeedNotification() != null) {
            setting.setFeedNotification(request.getFeedNotification());
        }
        if (request.getEmblemNotification() != null) {
            setting.setEmblemNotification(request.getEmblemNotification());
        }
        if (request.getAllNotificationsEnabled() != null) {
            setting.setAllNotificationsEnabled(request.getAllNotificationsEnabled());
        }

        NotificationSetting saved = notificationSettingRepository.save(setting);
        log.info("알림 설정 업데이트: userId={}", userId);

        return NotificationSettingResponse.builder()
                .scheduledRunningReminder(saved.getScheduledRunningReminder())
                .crewNotification(saved.getCrewNotification())
                .feedNotification(saved.getFeedNotification())
                .emblemNotification(saved.getEmblemNotification())
                .allNotificationsEnabled(saved.getAllNotificationsEnabled())
                .build();
    }

    /**
     * 기본 알림 설정 생성
     */
    private NotificationSetting createDefaultSettings(User user) {
        NotificationSetting setting = NotificationSetting.builder()
                .user(user)
                .scheduledRunningReminder(true)
                .crewNotification(true)
                .feedNotification(true)
                .emblemNotification(true)
                .allNotificationsEnabled(true)
                .build();
        return notificationSettingRepository.save(setting);
    }

    /**
     * 알림 설정이 없으면 자동 생성
     */
    private void ensureNotificationSettingExists(User user) {
        if (!notificationSettingRepository.findByUser(user).isPresent()) {
            createDefaultSettings(user);
            log.info("알림 설정 자동 생성: userId={}", user.getId());
        }
    }
}
