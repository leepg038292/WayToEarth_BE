package com.waytoearth.service.notification;

import com.google.firebase.messaging.*;
import com.waytoearth.entity.notification.FcmToken;
import com.waytoearth.entity.notification.NotificationSetting;
import com.waytoearth.entity.user.User;
import com.waytoearth.repository.notification.FcmTokenRepository;
import com.waytoearth.repository.notification.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final FcmTokenRepository fcmTokenRepository;
    private final NotificationSettingRepository notificationSettingRepository;

    @Value("${fcm.notifications.enabled:true}")
    private boolean notificationsEnabled;

    /**
     * 단일 사용자에게 푸시 알림 전송
     */
    public void sendNotificationToUser(Long userId, String title, String body) {
        if (!notificationsEnabled) {
            log.debug("FCM 알림이 비활성화되어 있습니다.");
            return;
        }

        // 알림 설정 확인
        NotificationSetting setting = notificationSettingRepository.findByUserId(userId)
                .orElse(null);

        if (setting == null || !setting.getAllNotificationsEnabled()) {
            log.debug("사용자 {}는 알림을 비활성화했습니다.", userId);
            return;
        }

        // 활성 토큰 조회
        List<FcmToken> tokens = fcmTokenRepository.findActiveTokensByUserId(userId);
        if (tokens.isEmpty()) {
            log.debug("사용자 {}의 활성 FCM 토큰이 없습니다.", userId);
            return;
        }

        // 각 디바이스에 전송
        for (FcmToken token : tokens) {
            sendSingleMessage(token.getFcmToken(), title, body);
        }
    }

    /**
     * 정기 러닝 알림 (모든 사용자)
     */
    public void sendScheduledRunningReminder(String title, String body) {
        if (!notificationsEnabled) {
            return;
        }

        log.info("정기 러닝 알림 전송 시작: {}", title);

        List<FcmToken> allTokens = fcmTokenRepository.findAllActiveTokens();
        List<String> validTokens = new ArrayList<>();

        for (FcmToken token : allTokens) {
            NotificationSetting setting = notificationSettingRepository.findByUserId(token.getUser().getId())
                    .orElse(null);

            if (setting != null && setting.canReceiveScheduledReminder()) {
                validTokens.add(token.getFcmToken());
            }
        }

        if (validTokens.isEmpty()) {
            log.info("정기 알림을 받을 사용자가 없습니다.");
            return;
        }

        // 멀티캐스트 전송 (최대 500개씩)
        sendMulticastMessage(validTokens, title, body);
        log.info("정기 러닝 알림 전송 완료: {} 사용자", validTokens.size());
    }

    /**
     * 크루 관련 알림
     */
    public void sendCrewNotification(Long userId, String title, String body) {
        if (!notificationsEnabled) {
            return;
        }

        NotificationSetting setting = notificationSettingRepository.findByUserId(userId)
                .orElse(null);

        if (setting == null || !setting.canReceiveCrewNotification()) {
            return;
        }

        sendNotificationToUser(userId, title, body);
    }

    /**
     * 피드 관련 알림
     */
    public void sendFeedNotification(Long userId, String title, String body) {
        if (!notificationsEnabled) {
            return;
        }

        NotificationSetting setting = notificationSettingRepository.findByUserId(userId)
                .orElse(null);

        if (setting == null || !setting.canReceiveFeedNotification()) {
            return;
        }

        sendNotificationToUser(userId, title, body);
    }

    /**
     * 엠블럼 획득 알림
     */
    public void sendEmblemNotification(Long userId, String title, String body) {
        if (!notificationsEnabled) {
            return;
        }

        NotificationSetting setting = notificationSettingRepository.findByUserId(userId)
                .orElse(null);

        if (setting == null || !setting.canReceiveEmblemNotification()) {
            return;
        }

        sendNotificationToUser(userId, title, body);
    }

    /**
     * 단일 메시지 전송
     */
    private void sendSingleMessage(String token, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.debug("FCM 전송 성공: {}", response);

        } catch (FirebaseMessagingException e) {
            handleMessagingException(token, e);
        }
    }

    /**
     * 멀티캐스트 메시지 전송
     */
    private void sendMulticastMessage(List<String> tokens, String title, String body) {
        // FCM 멀티캐스트는 최대 500개씩만 가능
        int batchSize = 500;
        for (int i = 0; i < tokens.size(); i += batchSize) {
            List<String> batch = tokens.subList(i, Math.min(i + batchSize, tokens.size()));

            try {
                MulticastMessage message = MulticastMessage.builder()
                        .addAllTokens(batch)
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .build();

                BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
                log.debug("멀티캐스트 전송 성공: {}/{}", response.getSuccessCount(), batch.size());

                // 실패한 토큰 처리
                handleBatchResponse(batch, response);

            } catch (FirebaseMessagingException e) {
                log.error("멀티캐스트 전송 실패: {}", e.getMessage());
            }
        }
    }

    /**
     * 배치 응답 처리 (실패한 토큰 비활성화)
     */
    protected void handleBatchResponse(List<String> tokens, BatchResponse response) {
        for (int i = 0; i < response.getResponses().size(); i++) {
            SendResponse sendResponse = response.getResponses().get(i);
            if (!sendResponse.isSuccessful()) {
                String token = tokens.get(i);
                FirebaseMessagingException exception = sendResponse.getException();
                if (exception != null) {
                    handleMessagingException(token, exception);
                }
            }
        }
    }

    /**
     * FCM 예외 처리 (무효한 토큰 비활성화)
     */
    protected void handleMessagingException(String token, FirebaseMessagingException e) {
        String errorCode = e.getMessagingErrorCode() != null ? e.getMessagingErrorCode().name() : "";

        if ("UNREGISTERED".equals(errorCode) || "INVALID_ARGUMENT".equals(errorCode)) {
            log.warn("무효한 FCM 토큰 비활성화: {}", token);
            fcmTokenRepository.deactivateToken(token);
        } else {
            log.error("FCM 전송 실패: {} - {}", errorCode, e.getMessage());
        }
    }
}
