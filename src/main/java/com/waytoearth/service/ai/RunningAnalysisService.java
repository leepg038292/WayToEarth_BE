package com.waytoearth.service.ai;

import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.waytoearth.dto.response.running.ai.RunningAnalysisResponse;
import com.waytoearth.entity.running.RunningFeedback;
import com.waytoearth.entity.running.RunningRecord;
import com.waytoearth.entity.user.User;
import com.waytoearth.exception.InvalidParameterException;
import com.waytoearth.exception.UnauthorizedException;
import com.waytoearth.repository.running.RunningFeedbackRepository;
import com.waytoearth.repository.running.RunningRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 러닝 기록 AI 분석 서비스
 * - OpenAI API를 활용한 러닝 데이터 분석
 * - 분석 결과를 DB에 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RunningAnalysisService {

    private final OpenAIService openAIService;
    private final RunningRecordRepository runningRecordRepository;
    private final RunningFeedbackRepository runningFeedbackRepository;

    /**
     * 러닝 기록 분석 및 피드백 생성
     * - 이미 분석된 기록은 기존 결과 반환
     * - 미완료 기록은 분석 불가
     *
     * @param runningRecordId 러닝 기록 ID
     * @param user            요청 사용자
     * @return 분석 결과
     */
    @Transactional
    public RunningAnalysisResponse analyzeRunning(Long runningRecordId, User user) {
        // 1. 러닝 기록 조회 및 권한 검증
        RunningRecord runningRecord = runningRecordRepository.findByIdAndUser(runningRecordId, user)
                .orElseThrow(() -> new InvalidParameterException("러닝 기록을 찾을 수 없거나 접근 권한이 없습니다."));

        // 2. 완료된 기록만 분석 가능
        if (!runningRecord.getIsCompleted()) {
            throw new InvalidParameterException("완료된 러닝 기록만 분석할 수 있습니다.");
        }

        // 3. 이미 분석된 기록이 있는지 확인
        return runningFeedbackRepository.findByRunningRecord(runningRecord)
                .map(this::toResponse)
                .orElseGet(() -> generateNewFeedback(runningRecord));
    }

    /**
     * 새로운 AI 피드백 생성
     */
    private RunningAnalysisResponse generateNewFeedback(RunningRecord runningRecord) {
        log.info("Generating new AI feedback for running record: {}", runningRecord.getId());

        // 1. 프롬프트 생성
        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(runningRecord);

        // 2. OpenAI API 호출
        ChatCompletionResult result = openAIService.createChatCompletion(systemPrompt, userPrompt);
        String feedbackContent = openAIService.getCompletionText(result);

        // 3. 피드백 저장
        RunningFeedback feedback = RunningFeedback.builder()
                .runningRecord(runningRecord)
                .feedbackContent(feedbackContent)
                .modelName(result.getModel())
                .promptTokens(result.getUsage().getPromptTokens())
                .completionTokens(result.getUsage().getCompletionTokens())
                .totalTokens(result.getUsage().getTotalTokens())
                .build();

        feedback = runningFeedbackRepository.save(feedback);
        log.info("AI feedback saved - ID: {}, Tokens: {}", feedback.getId(), feedback.getTotalTokens());

        return toResponse(feedback);
    }

    /**
     * 시스템 프롬프트 생성
     * - AI의 역할과 응답 형식 정의
     */
    private String buildSystemPrompt() {
        return """
                당신은 전문 러닝 코치입니다.
                사용자의 러닝 기록을 분석하고, 긍정적이고 구체적인 피드백을 제공합니다.

                응답 가이드라인:
                1. 3-5문장으로 간결하게 작성
                2. 운동 성과에 대한 칭찬 1-2문장
                3. 개선 가능한 점 또는 다음 목표 제안 1-2문장
                4. 격려와 동기부여로 마무리
                5. 친근하고 긍정적인 톤 유지

                [향후 확장 예정]
                - 케이던스 분석
                - 심박수 기반 운동 강도 평가
                - 페이스 변화 패턴 분석
                """;
    }

    /**
     * 사용자 프롬프트 생성
     * - 실제 러닝 데이터를 포함
     */
    private String buildUserPrompt(RunningRecord record) {
        BigDecimal distance = record.getDistance() != null ? record.getDistance() : BigDecimal.ZERO;
        Integer duration = record.getDuration() != null ? record.getDuration() : 0;
        Integer pace = record.getAveragePaceSeconds() != null ? record.getAveragePaceSeconds() : 0;
        Integer calories = record.getCalories() != null ? record.getCalories() : 0;

        String paceFormatted = formatPace(pace);
        String durationFormatted = formatDuration(duration);

        return String.format("""
                다음 러닝 기록을 분석해주세요:

                - 거리: %s km
                - 시간: %s
                - 평균 페이스: %s
                - 칼로리: %d kcal
                - 러닝 타입: %s

                위 데이터를 바탕으로 사용자에게 피드백을 제공해주세요.
                """,
                distance.setScale(2, RoundingMode.HALF_UP),
                durationFormatted,
                paceFormatted,
                calories,
                record.getRunningType() != null ? record.getRunningType().name() : "UNKNOWN"
        );
    }

    /**
     * 페이스를 "분:초/km" 형식으로 변환
     */
    private String formatPace(Integer paceSeconds) {
        if (paceSeconds == null || paceSeconds == 0) {
            return "측정 안됨";
        }
        int minutes = paceSeconds / 60;
        int seconds = paceSeconds % 60;
        return String.format("%d:%02d/km", minutes, seconds);
    }

    /**
     * 시간을 "시간분초" 형식으로 변환
     */
    private String formatDuration(Integer seconds) {
        if (seconds == null || seconds == 0) {
            return "0초";
        }
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        if (hours > 0) {
            return String.format("%d시간 %d분 %d초", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%d분 %d초", minutes, secs);
        } else {
            return String.format("%d초", secs);
        }
    }

    /**
     * Entity -> Response DTO 변환
     */
    private RunningAnalysisResponse toResponse(RunningFeedback feedback) {
        return RunningAnalysisResponse.builder()
                .feedbackId(feedback.getId())
                .runningRecordId(feedback.getRunningRecord().getId())
                .feedbackContent(feedback.getFeedbackContent())
                .createdAt(feedback.getCreatedAt())
                .modelName(feedback.getModelName())
                .build();
    }
}
