package com.waytoearth.entity.crew;

import com.waytoearth.entity.common.BaseTimeEntity;
import com.waytoearth.entity.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "crew_join_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "크루 가입 신청 엔티티")
public class CrewJoinRequestEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "가입 신청 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_id", nullable = false)
    @Schema(description = "크루")
    private CrewEntity crew;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "신청자")
    private User user;

    @Schema(description = "신청 메시지", example = "안녕하세요! 함께 러닝하고 싶습니다.")
    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "신청 상태", example = "PENDING")
    @Builder.Default
    private JoinRequestStatus status = JoinRequestStatus.PENDING;

    @Schema(description = "처리일", example = "2024-01-15T10:30:00")
    private LocalDateTime processedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    @Schema(description = "처리자 (크루장)")
    private User processedBy;

    @Schema(description = "처리 메모", example = "환영합니다!")
    @Column(length = 500)
    private String processingNote;

    @Getter
    @RequiredArgsConstructor
    public enum JoinRequestStatus {
        PENDING("PENDING", "대기중"),
        APPROVED("APPROVED", "승인됨"),
        REJECTED("REJECTED", "거부됨"),
        CANCELLED("CANCELLED", "취소됨");

        private final String code;
        private final String description;
    }

    public boolean isPending() {
        return status == JoinRequestStatus.PENDING;
    }

    public boolean isApproved() {
        return status == JoinRequestStatus.APPROVED;
    }

    public boolean isRejected() {
        return status == JoinRequestStatus.REJECTED;
    }

    public boolean isCancelled() {
        return status == JoinRequestStatus.CANCELLED;
    }

    public void approve(User processedBy, String note) {
        this.status = JoinRequestStatus.APPROVED;
        this.processedAt = LocalDateTime.now();
        this.processedBy = processedBy;
        this.processingNote = note;
    }

    public void reject(User processedBy, String note) {
        this.status = JoinRequestStatus.REJECTED;
        this.processedAt = LocalDateTime.now();
        this.processedBy = processedBy;
        this.processingNote = note;
    }

    public void cancel() {
        this.status = JoinRequestStatus.CANCELLED;
        this.processedAt = LocalDateTime.now();
    }
}