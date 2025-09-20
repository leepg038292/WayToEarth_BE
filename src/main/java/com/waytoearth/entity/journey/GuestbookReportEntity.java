package com.waytoearth.entity.journey;

import com.waytoearth.entity.common.BaseTimeEntity;
import com.waytoearth.entity.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "guestbook_reports",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "guestbook_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "방명록 신고 엔티티")
public class GuestbookReportEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "신고 ID", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "신고자")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guestbook_id", nullable = false)
    @Schema(description = "신고된 방명록")
    private GuestbookEntity guestbook;

    @Schema(description = "신고 사유", example = "SPAM")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    @Schema(description = "추가 설명", example = "광고성 내용이 포함되어 있습니다")
    @Column(length = 500)
    private String description;

    @Schema(description = "처리 상태", example = "PENDING")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    public enum ReportReason {
        SPAM,           // 스팸
        INAPPROPRIATE,  // 부적절한 내용
        HARASSMENT,     // 괴롭힘
        FALSE_INFO,     // 허위 정보
        OTHER          // 기타
    }

    public enum ReportStatus {
        PENDING,    // 대기 중
        REVIEWED,   // 검토 완료
        RESOLVED    // 해결됨
    }
}