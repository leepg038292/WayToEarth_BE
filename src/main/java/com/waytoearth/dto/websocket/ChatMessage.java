package com.waytoearth.dto.websocket;

import com.waytoearth.entity.crew.CrewChatEntity;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    private Long messageId;
    private Long crewId;
    private Long senderId;
    private String senderName;
    private String message;
    private CrewChatEntity.MessageType messageType;
    private LocalDateTime timestamp;
    private boolean isRead;
    private int unreadCount;
}