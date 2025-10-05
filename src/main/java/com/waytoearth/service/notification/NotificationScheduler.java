package com.waytoearth.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final FcmService fcmService;

    @Value("${fcm.notifications.scheduled.morning-message.title}")
    private String morningTitle;

    @Value("${fcm.notifications.scheduled.morning-message.body}")
    private String morningBody;

    @Value("${fcm.notifications.scheduled.evening-message.title}")
    private String eveningTitle;

    @Value("${fcm.notifications.scheduled.evening-message.body}")
    private String eveningBody;

    /**
     * 오전 6:30 정기 알림
     */
    @Scheduled(cron = "${fcm.notifications.scheduled.morning-time}", zone = "${fcm.notifications.scheduled.timezone}")
    public void sendMorningReminder() {
        log.info("=== 오전 러닝 알림 스케줄 실행 ===");
        fcmService.sendScheduledRunningReminder(morningTitle, morningBody);
    }

    /**
     * 오후 8:00 정기 알림
     */
    @Scheduled(cron = "${fcm.notifications.scheduled.evening-time}", zone = "${fcm.notifications.scheduled.timezone}")
    public void sendEveningReminder() {
        log.info("=== 저녁 러닝 알림 스케줄 실행 ===");
        fcmService.sendScheduledRunningReminder(eveningTitle, eveningBody);
    }
}
