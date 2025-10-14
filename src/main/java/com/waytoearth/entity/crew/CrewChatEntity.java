package com.waytoearth.entity.crew;

import com.waytoearth.entity.common.BaseTimeEntity;
import com.waytoearth.entity.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "crew_chats",
       indexes = {
           @Index(name = "idx_crew_chat_crew_sent_at", columnList = "crew_id, sent_at"),
           @Index(name = "idx_crew_chat_crew_deleted", columnList = "crew_id, is_deleted"),
           @Index(name = "idx_crew_chat_sender", columnList = "sender_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "크루 채팅 메시지")
public class CrewChatEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "채팅 메시지 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_id", nullable = true)
    @Schema(description = "크루 (삭제된 크루는 null)")
    private CrewEntity crew;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    @Schema(description = "발신자")
    private User sender;

    @Schema(description = "메시지 내용", example = "오늘 러닝 어떠셨나요?")
    @Column(nullable = false, length = 1000)
    private String message;

    @Schema(description = "메시지 타입", example = "TEXT")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    @Schema(description = "전송 시간")
    @Column(nullable = false)
    private LocalDateTime sentAt;

    @Schema(description = "삭제 여부", example = "false")
    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Schema(description = "메시지 읽음 정보")
    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CrewChatReadStatusEntity> readStatus = new ArrayList<>();

    @Schema(description = "메시지 타입")
    public enum MessageType {
        TEXT,           // 일반 텍스트
        SYSTEM,         // 시스템 메시지 (입장/퇴장 등)
        ANNOUNCEMENT    // 공지사항 (크루장 전용)
    }

    @Schema(description = "활성 여부", example = "true")
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
    }

    // 읽지 않은 사용자 수 계산
    public int getUnreadCount(int totalMembers) {
        return Math.max(0, totalMembers - readStatus.size() - 1); // 발신자 제외
    }

    // 특정 사용자가 읽었는지 확인
    public boolean isReadBy(Long userId) {
        // 발신자는 항상 읽은 것으로 처리
        if (sender.getId().equals(userId)) {
            return true;
        }
        return readStatus.stream()
                .anyMatch(status -> status.getReader().getId().equals(userId));
    }
}