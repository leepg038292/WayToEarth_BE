package com.waytoearth.dto.response.crew;

import com.waytoearth.entity.crew.CrewChatEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "크루 채팅 메시지 응답")
public class CrewChatMessageDto {

    @Schema(description = "메시지 ID", example = "1")
    private Long messageId;

    @Schema(description = "크루 ID", example = "1")
    private Long crewId;

    @Schema(description = "발신자 ID", example = "1")
    private Long senderId;

    @Schema(description = "발신자 닉네임", example = "러너123")
    private String senderName;

    @Schema(description = "메시지 내용", example = "오늘 러닝 어떠셨나요?")
    private String message;

    @Schema(description = "메시지 타입", example = "TEXT")
    private CrewChatEntity.MessageType messageType;

    @Schema(description = "전송 시간")
    private LocalDateTime sentAt;

    @Schema(description = "읽음 여부", example = "true")
    private boolean isRead;

    @Schema(description = "읽은 사용자 수", example = "5")
    private int readCount;
}