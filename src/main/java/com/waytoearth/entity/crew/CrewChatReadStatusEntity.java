package com.waytoearth.entity.crew;

import com.waytoearth.entity.common.BaseTimeEntity;
import com.waytoearth.entity.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "crew_chat_read_status",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"message_id", "reader_id"}, name = "uk_chat_read_status")
       },
       indexes = {
           @Index(name = "idx_chat_read_status_message", columnList = "message_id"),
           @Index(name = "idx_chat_read_status_reader", columnList = "reader_id"),
           @Index(name = "idx_chat_read_status_read_at", columnList = "read_at")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "크루 채팅 메시지 읽음 상태")
public class CrewChatReadStatusEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "읽음 상태 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    @Schema(description = "메시지")
    private CrewChatEntity message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reader_id", nullable = false)
    @Schema(description = "읽은 사용자")
    private User reader;

    @Schema(description = "읽은 시간")
    @Column(nullable = false)
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        if (readAt == null) {
            readAt = LocalDateTime.now();
        }
    }
}