package com.waytoearth.service.crew;

import com.waytoearth.dto.response.crew.CrewChatMessageDto;
import com.waytoearth.entity.crew.CrewChatEntity;
import com.waytoearth.entity.crew.CrewChatNotificationSettingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CrewChatService {

    /**
     * 채팅 메시지 저장
     */
    CrewChatEntity saveMessage(Long crewId, Long senderId, String message,
                              CrewChatEntity.MessageType messageType);

    /**
     * 크루 채팅 메시지 목록 조회 (페이지네이션)
     */
    Page<CrewChatMessageDto> getChatMessages(Long crewId, Long userId, Pageable pageable);

    /**
     * 메시지 읽음 처리
     */
    void markMessageAsRead(Long messageId, Long userId);

    /**
     * 여러 메시지 읽음 처리
     */
    void markMessagesAsRead(Long crewId, Long userId, List<Long> messageIds);

    /**
     * 특정 시점 이후의 모든 메시지를 읽음 처리
     */
    void markAllMessagesAsReadAfter(Long crewId, Long userId, Long afterMessageId);

    /**
     * 읽지 않은 메시지 수 조회
     */
    int getUnreadMessageCount(Long crewId, Long userId);

    /**
     * 메시지 삭제 (소프트 삭제)
     */
    void deleteMessage(Long messageId, Long userId);

    /**
     * 알림 설정 조회
     */
    CrewChatNotificationSettingEntity getNotificationSetting(Long crewId, Long userId);

    /**
     * 알림 설정 업데이트
     */
    CrewChatNotificationSettingEntity updateNotificationSetting(Long crewId, Long userId,
                                                               boolean isEnabled,
                                                               CrewChatNotificationSettingEntity.NotificationType notificationType,
                                                               boolean isMuted);

    /**
     * 크루 멤버의 기본 알림 설정 생성
     */
    void createDefaultNotificationSetting(Long crewId, Long userId);

    /**
     * 최근 채팅 메시지 조회
     */
    List<CrewChatMessageDto> getRecentMessages(Long crewId, Long userId, int limit);
}