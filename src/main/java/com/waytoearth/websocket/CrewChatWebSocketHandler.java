package com.waytoearth.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waytoearth.dto.websocket.ChatMessage;
import com.waytoearth.entity.crew.CrewChatEntity;
import com.waytoearth.entity.crew.CrewEntity;
import com.waytoearth.entity.user.User;
import com.waytoearth.repository.crew.CrewMemberRepository;
import com.waytoearth.repository.crew.CrewRepository;
import com.waytoearth.repository.user.UserRepository;
import com.waytoearth.service.crew.CrewChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class CrewChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final CrewChatService crewChatService;
    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final UserRepository userRepository;

    // 크루별 연결된 세션들을 관리 (crewId -> Map<userId, WebSocketSession>)
    private final Map<Long, Map<Long, WebSocketSession>> crewConnections = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String uri = session.getUri().toString();
        Long crewId = extractCrewIdFromUri(uri);
        Long userId = extractUserIdFromSession(session);

        if (crewId == null || userId == null) {
            session.close(CloseStatus.BAD_DATA.withReason("Invalid crew ID or user ID"));
            return;
        }

        // 크루 멤버십 확인
        if (!crewMemberRepository.existsByCrewIdAndUserIdAndIsActiveTrue(crewId, userId)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Not a crew member"));
            return;
        }

        // 세션 저장
        crewConnections.computeIfAbsent(crewId, k -> new ConcurrentHashMap<>()).put(userId, session);

        log.info("웹소켓 연결 성공 - crewId: {}, userId: {}", crewId, userId);

        // 연결 알림 메시지 전송 (시스템 메시지)
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            ChatMessage joinMessage = ChatMessage.builder()
                    .crewId(crewId)
                    .senderId(userId)
                    .senderName(user.getNickname())
                    .message(user.getNickname() + "님이 채팅방에 참여했습니다.")
                    .messageType(CrewChatEntity.MessageType.SYSTEM)
                    .timestamp(LocalDateTime.now())
                    .build();

            broadcastToCrewMembers(crewId, joinMessage, userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String uri = session.getUri().toString();
            Long crewId = extractCrewIdFromUri(uri);
            Long userId = extractUserIdFromSession(session);

            if (crewId == null || userId == null) {
                return;
            }

            ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);

            // 메시지 유효성 검증
            if (chatMessage.getMessage() == null || chatMessage.getMessage().trim().isEmpty()) {
                return;
            }

            if (chatMessage.getMessage().length() > 1000) {
                sendErrorMessage(session, "메시지는 1000자를 초과할 수 없습니다.");
                return;
            }

            // 메시지 저장
            CrewChatEntity savedMessage = crewChatService.saveMessage(crewId, userId,
                    chatMessage.getMessage(), chatMessage.getMessageType());

            // 응답 메시지 구성
            User sender = userRepository.findById(userId).orElse(null);
            ChatMessage responseMessage = ChatMessage.builder()
                    .messageId(savedMessage.getId())
                    .crewId(crewId)
                    .senderId(userId)
                    .senderName(sender != null ? sender.getNickname() : "Unknown")
                    .message(savedMessage.getMessage())
                    .messageType(savedMessage.getMessageType())
                    .timestamp(savedMessage.getSentAt())
                    .build();

            // 크루 멤버들에게 브로드캐스트
            broadcastToCrewMembers(crewId, responseMessage, null);

            log.debug("메시지 전송 완료 - crewId: {}, userId: {}, messageId: {}",
                     crewId, userId, savedMessage.getId());

        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생", e);
            sendErrorMessage(session, "메시지 처리 중 오류가 발생했습니다.");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String uri = session.getUri().toString();
        Long crewId = extractCrewIdFromUri(uri);
        Long userId = extractUserIdFromSession(session);

        if (crewId != null && userId != null) {
            Map<Long, WebSocketSession> crewSessions = crewConnections.get(crewId);
            if (crewSessions != null) {
                crewSessions.remove(userId);
                if (crewSessions.isEmpty()) {
                    crewConnections.remove(crewId);
                }
            }

            log.info("웹소켓 연결 종료 - crewId: {}, userId: {}", crewId, userId);

            // 퇴장 알림 메시지 전송 (시스템 메시지)
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                ChatMessage leaveMessage = ChatMessage.builder()
                        .crewId(crewId)
                        .senderId(userId)
                        .senderName(user.getNickname())
                        .message(user.getNickname() + "님이 채팅방을 나갔습니다.")
                        .messageType(CrewChatEntity.MessageType.SYSTEM)
                        .timestamp(LocalDateTime.now())
                        .build();

                broadcastToCrewMembers(crewId, leaveMessage, userId);
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("웹소켓 전송 오류", exception);
        session.close(CloseStatus.SERVER_ERROR);
    }

    private void broadcastToCrewMembers(Long crewId, ChatMessage message, Long excludeUserId) {
        Map<Long, WebSocketSession> crewSessions = crewConnections.get(crewId);
        if (crewSessions == null || crewSessions.isEmpty()) {
            return;
        }

        String messageJson;
        try {
            messageJson = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("메시지 JSON 변환 실패", e);
            return;
        }

        crewSessions.entrySet().removeIf(entry -> {
            Long userId = entry.getKey();
            WebSocketSession session = entry.getValue();

            // 제외할 사용자는 스킵
            if (excludeUserId != null && excludeUserId.equals(userId)) {
                return false;
            }

            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(messageJson));
                    return false;
                } else {
                    return true; // 닫힌 세션은 제거
                }
            } catch (Exception e) {
                log.warn("메시지 전송 실패 - crewId: {}, userId: {}", crewId, userId, e);
                return true; // 오류 발생한 세션은 제거
            }
        });
    }

    private void sendErrorMessage(WebSocketSession session, String error) {
        try {
            ChatMessage errorMessage = ChatMessage.builder()
                    .message(error)
                    .messageType(CrewChatEntity.MessageType.SYSTEM)
                    .timestamp(LocalDateTime.now())
                    .build();

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorMessage)));
        } catch (Exception e) {
            log.error("에러 메시지 전송 실패", e);
        }
    }

    private Long extractCrewIdFromUri(String uri) {
        try {
            // URI 형식: /ws/crew/{crewId}/chat
            String[] parts = uri.split("/");
            for (int i = 0; i < parts.length - 1; i++) {
                if ("crew".equals(parts[i]) && i + 1 < parts.length) {
                    return Long.parseLong(parts[i + 1]);
                }
            }
        } catch (Exception e) {
            log.error("크루 ID 추출 실패: {}", uri, e);
        }
        return null;
    }

    private Long extractUserIdFromSession(WebSocketSession session) {
        // WebSocketAuthInterceptor에서 설정한 userId 사용
        Object userId = session.getAttributes().get("userId");
        if (userId instanceof Long) {
            return (Long) userId;
        }

        log.error("WebSocket 세션에서 userId를 찾을 수 없습니다. 인증 실패");
        return null;
    }
}