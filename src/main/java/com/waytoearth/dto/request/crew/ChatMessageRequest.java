package com.waytoearth.dto.request.crew;

import com.waytoearth.entity.crew.CrewChatEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅 메시지 전송 요청")
public class ChatMessageRequest {

    @NotBlank(message = "메시지 내용은 필수입니다.")
    @Size(min = 1, max = 1000, message = "메시지는 1자 이상 1000자 이하여야 합니다.")
    @Schema(description = "메시지 내용", example = "오늘 러닝 어떠셨나요?")
    private String message;

    @Schema(description = "메시지 타입", example = "TEXT")
    private CrewChatEntity.MessageType messageType = CrewChatEntity.MessageType.TEXT;
}