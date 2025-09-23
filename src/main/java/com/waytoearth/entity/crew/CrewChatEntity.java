package com.waytoearth.entity.crew;

import com.waytoearth.entity.common.BaseTimeEntity;
import com.waytoearth.entity.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "crew_chats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "크루 채팅 엔티티")
public class CrewChatEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "채팅 메시지 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_id", nullable = false)
    @Schema(description = "크루")
    private CrewEntity crew;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    @Schema(description = "발신자")
    private User sender;

    @Schema(description = "메시지 내용", example = "오늘 러닝 어떠셨나요?")
    @Column(nullable = false, length = 1000)
    private String message;

    @Schema(description = "활성화 상태", example = "true")
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}