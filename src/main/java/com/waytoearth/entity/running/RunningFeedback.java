package com.waytoearth.entity.running;

import com.waytoearth.entity.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "running_feedback",
        indexes = {
                @Index(name = "idx_feedback_running_record", columnList = "running_record_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunningFeedback extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 소속 러닝 기록 */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "running_record_id", nullable = false, unique = true)
    private RunningRecord runningRecord;

    /** AI 분석 결과 전문 */
    @Column(name = "feedback_content", columnDefinition = "TEXT", nullable = false)
    private String feedbackContent;

    /** 분석 모델명 */
    @Column(name = "model_name", length = 50)
    private String modelName;

    /** 분석에 사용된 프롬프트 토큰 수 */
    @Column(name = "prompt_tokens")
    private Integer promptTokens;

    /** 분석 응답 토큰 수 */
    @Column(name = "completion_tokens")
    private Integer completionTokens;

    /** 총 토큰 수 */
    @Column(name = "total_tokens")
    private Integer totalTokens;
}
