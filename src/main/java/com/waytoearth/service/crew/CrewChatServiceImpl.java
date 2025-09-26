package com.waytoearth.service.crew;

import com.waytoearth.dto.response.crew.CrewChatMessageDto;
import com.waytoearth.entity.crew.*;
import com.waytoearth.entity.user.User;
import com.waytoearth.exception.CrewNotFoundException;
import com.waytoearth.exception.UnauthorizedAccessException;
import com.waytoearth.repository.crew.*;
import com.waytoearth.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CrewChatServiceImpl implements CrewChatService {

    private final CrewChatRepository crewChatRepository;
    private final CrewChatReadStatusRepository crewChatReadStatusRepository;
    private final CrewChatNotificationSettingRepository notificationSettingRepository;
    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CrewChatEntity saveMessage(Long crewId, Long senderId, String message,
                                     CrewChatEntity.MessageType messageType) {
        CrewEntity crew = getCrewEntity(crewId);
        User sender = getUserEntity(senderId);

        // 크루 멤버 여부 확인
        validateCrewMember(crewId, senderId);

        // 공지사항 메시지는 크루장만 작성 가능
        if (messageType == CrewChatEntity.MessageType.ANNOUNCEMENT) {
            CrewMemberEntity membership = crewMemberRepository.findMembership(senderId, crewId)
                    .orElseThrow(() -> new UnauthorizedAccessException("크루 멤버가 아닙니다."));

            if (!membership.isOwner()) {
                throw new UnauthorizedAccessException("공지사항은 크루장만 작성할 수 있습니다.");
            }
        }

        CrewChatEntity chatMessage = CrewChatEntity.builder()
                .crew(crew)
                .sender(sender)
                .message(message)
                .messageType(messageType != null ? messageType : CrewChatEntity.MessageType.TEXT)
                .sentAt(LocalDateTime.now())
                .build();

        CrewChatEntity saved = crewChatRepository.save(chatMessage);

        log.info("채팅 메시지 저장 완료 - crewId: {}, senderId: {}, messageId: {}",
                 crewId, senderId, saved.getId());

        return saved;
    }

    @Override
    public Page<CrewChatMessageDto> getChatMessages(Long crewId, Long userId, Pageable pageable) {
        validateCrewMember(crewId, userId);

        return crewChatRepository.findChatMessagesWithReadStatus(crewId, userId, pageable);
    }

    @Override
    @Transactional
    public void markMessageAsRead(Long messageId, Long userId) {
        CrewChatEntity message = crewChatRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("메시지를 찾을 수 없습니다."));

        validateCrewMember(message.getCrew().getId(), userId);

        // 이미 읽음 처리된 경우 중복 방지
        if (message.isReadBy(userId)) {
            return;
        }

        User reader = getUserEntity(userId);

        CrewChatReadStatusEntity readStatus = CrewChatReadStatusEntity.builder()
                .message(message)
                .reader(reader)
                .readAt(LocalDateTime.now())
                .build();

        crewChatReadStatusRepository.save(readStatus);

        log.debug("메시지 읽음 처리 완료 - messageId: {}, userId: {}", messageId, userId);
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Long crewId, Long userId, List<Long> messageIds) {
        validateCrewMember(crewId, userId);

        User reader = getUserEntity(userId);

        for (Long messageId : messageIds) {
            Optional<CrewChatEntity> messageOpt = crewChatRepository.findById(messageId);
            if (messageOpt.isPresent() && !messageOpt.get().isReadBy(userId)) {
                CrewChatReadStatusEntity readStatus = CrewChatReadStatusEntity.builder()
                        .message(messageOpt.get())
                        .reader(reader)
                        .readAt(LocalDateTime.now())
                        .build();

                crewChatReadStatusRepository.save(readStatus);
            }
        }

        log.info("다중 메시지 읽음 처리 완료 - crewId: {}, userId: {}, count: {}",
                 crewId, userId, messageIds.size());
    }

    @Override
    @Transactional
    public void markAllMessagesAsReadAfter(Long crewId, Long userId, Long afterMessageId) {
        validateCrewMember(crewId, userId);

        List<CrewChatEntity> unreadMessages = crewChatRepository.findUnreadMessagesAfter(crewId, userId, afterMessageId);
        User reader = getUserEntity(userId);

        for (CrewChatEntity message : unreadMessages) {
            if (!message.isReadBy(userId)) {
                CrewChatReadStatusEntity readStatus = CrewChatReadStatusEntity.builder()
                        .message(message)
                        .reader(reader)
                        .readAt(LocalDateTime.now())
                        .build();

                crewChatReadStatusRepository.save(readStatus);
            }
        }

        log.info("특정 시점 이후 모든 메시지 읽음 처리 완료 - crewId: {}, userId: {}, afterMessageId: {}",
                 crewId, userId, afterMessageId);
    }

    @Override
    public int getUnreadMessageCount(Long crewId, Long userId) {
        validateCrewMember(crewId, userId);

        return crewChatRepository.countUnreadMessages(crewId, userId);
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        CrewChatEntity message = crewChatRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("메시지를 찾을 수 없습니다."));

        // 발신자 또는 크루장만 삭제 가능
        if (!message.getSender().getId().equals(userId)) {
            CrewMemberEntity membership = crewMemberRepository.findMembership(userId, message.getCrew().getId())
                    .orElseThrow(() -> new UnauthorizedAccessException("크루 멤버가 아닙니다."));

            if (!membership.isOwner()) {
                throw new UnauthorizedAccessException("메시지는 작성자 또는 크루장만 삭제할 수 있습니다.");
            }
        }

        message.setIsDeleted(true);
        crewChatRepository.save(message);

        log.info("메시지 삭제 완료 - messageId: {}, userId: {}", messageId, userId);
    }

    @Override
    public CrewChatNotificationSettingEntity getNotificationSetting(Long crewId, Long userId) {
        validateCrewMember(crewId, userId);

        return notificationSettingRepository.findByCrewIdAndUserId(crewId, userId)
                .orElseGet(() -> {
                    // 기본 설정 생성
                    createDefaultNotificationSetting(crewId, userId);
                    return notificationSettingRepository.findByCrewIdAndUserId(crewId, userId)
                            .orElseThrow(() -> new RuntimeException("알림 설정을 생성할 수 없습니다."));
                });
    }

    @Override
    @Transactional
    public CrewChatNotificationSettingEntity updateNotificationSetting(Long crewId, Long userId,
                                                                       boolean isEnabled,
                                                                       CrewChatNotificationSettingEntity.NotificationType notificationType,
                                                                       boolean isMuted) {
        validateCrewMember(crewId, userId);

        CrewChatNotificationSettingEntity setting = getNotificationSetting(crewId, userId);
        setting.setIsEnabled(isEnabled);
        setting.setNotificationType(notificationType);
        setting.setIsMuted(isMuted);

        CrewChatNotificationSettingEntity saved = notificationSettingRepository.save(setting);

        log.info("알림 설정 업데이트 완료 - crewId: {}, userId: {}", crewId, userId);

        return saved;
    }

    @Override
    @Transactional
    public void createDefaultNotificationSetting(Long crewId, Long userId) {
        CrewEntity crew = getCrewEntity(crewId);
        User user = getUserEntity(userId);

        // 기존 설정이 있으면 생성하지 않음
        if (notificationSettingRepository.existsByCrewIdAndUserId(crewId, userId)) {
            return;
        }

        CrewChatNotificationSettingEntity setting = CrewChatNotificationSettingEntity.builder()
                .crew(crew)
                .user(user)
                .isEnabled(true)
                .notificationType(CrewChatNotificationSettingEntity.NotificationType.ALL)
                .isMuted(false)
                .build();

        notificationSettingRepository.save(setting);

        log.info("기본 알림 설정 생성 완료 - crewId: {}, userId: {}", crewId, userId);
    }

    @Override
    public List<CrewChatMessageDto> getRecentMessages(Long crewId, Long userId, int limit) {
        validateCrewMember(crewId, userId);

        return crewChatRepository.findRecentMessages(crewId, userId, limit);
    }

    private CrewEntity getCrewEntity(Long crewId) {
        return crewRepository.findById(crewId)
                .orElseThrow(() -> new CrewNotFoundException(crewId));
    }

    private User getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. userId: " + userId));
    }

    private void validateCrewMember(Long crewId, Long userId) {
        if (!crewMemberRepository.existsByCrewIdAndUserIdAndIsActiveTrue(crewId, userId)) {
            throw new UnauthorizedAccessException("크루 멤버가 아닙니다.");
        }
    }
}