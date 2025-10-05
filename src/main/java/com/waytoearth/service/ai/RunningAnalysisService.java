package com.waytoearth.service.ai;

import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.waytoearth.dto.response.running.ai.RunningAnalysisResponse;
import com.waytoearth.entity.running.RunningFeedback;
import com.waytoearth.entity.running.RunningRecord;
import com.waytoearth.entity.user.User;
import com.waytoearth.exception.InvalidParameterException;
import com.waytoearth.repository.running.RunningFeedbackRepository;
import com.waytoearth.repository.running.RunningRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

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

    @Value("${openai.min-completed-records}")
    private int minCompletedRecords;

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

        // 3. 최소 완료 기록 수 검증
        long completedCount = runningRecordRepository.countByUserAndIsCompletedTrue(user);
        if (completedCount < minCompletedRecords) {
            throw new InvalidParameterException(
                    String.format("AI 분석을 위해서는 최소 %d회 이상의 완료된 러닝 기록이 필요합니다. (현재: %d회)",
                            minCompletedRecords, completedCount)
            );
        }

        // 4. 이미 분석된 기록이 있는지 확인
        return runningFeedbackRepository.findByRunningRecord(runningRecord)
                .map(this::toResponse)
                .orElseGet(() -> generateNewFeedback(runningRecord, user));
    }

    /**
     * 새로운 AI 피드백 생성
     */
    private RunningAnalysisResponse generateNewFeedback(RunningRecord currentRecord, User user) {
        log.info("Generating new AI feedback for running record: {}", currentRecord.getId());

        // 1. 과거 러닝 기록 조회 (최근 10개, 현재 기록 제외)
        List<RunningRecord> recentRecords = runningRecordRepository
                .findAllByUserIdAndIsCompletedTrueOrderByStartedAtDesc(user.getId())
                .stream()
                .filter(r -> !r.getId().equals(currentRecord.getId()))
                .limit(10)
                .toList();

        // 2. 프롬프트 생성
        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(currentRecord, recentRecords);

        // 3. OpenAI API 호출
        ChatCompletionResult result = openAIService.createChatCompletion(systemPrompt, userPrompt);
        String feedbackContent = openAIService.getCompletionText(result);

        // 4. 피드백 저장
        RunningFeedback feedback = RunningFeedback.builder()
                .runningRecord(currentRecord)
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
                당신은 데이터 기반으로 분석하는 전문 러닝 코치입니다.
                사용자의 현재 러닝 기록과 과거 기록을 비교 분석하여, 구체적이고 실용적인 피드백을 제공합니다.

                분석 우선순위:
                1. **성장 패턴 분석**: 과거 대비 거리, 페이스, 지속성 개선도 파악
                2. **강점 강화**: 잘하고 있는 부분을 구체적 수치로 칭찬
                3. **개선 제안**: 다음 목표를 명확하게 제시 (예: "페이스를 10초 단축", "거리 1km 연장")
                4. **동기부여**: 긍정적이고 격려하는 톤으로 마무리

                응답 형식:
                - 4-6문장으로 작성
                - 구체적인 수치 언급 (거리, 페이스, 시간 등)
                - 과거 기록과 비교 시 "이전 평균 대비", "최근 기록과 비교" 같은 표현 사용
                - 반말 사용, 친근한 톤 유지
                - 이모지 사용 금지

                [향후 확장 예정]
                - 케이던스 분석 (보폭 효율성)
                - 심박수 기반 운동 강도 평가
                - 페이스 변화 패턴 분석 (일관성)
                """;
    }

    /**
     * 사용자 프롬프트 생성 (과거 기록 포함)
     * - 실제 러닝 데이터를 포함
     */
    private String buildUserPrompt(RunningRecord currentRecord, List<RunningRecord> recentRecords) {
        StringBuilder prompt = new StringBuilder();

        // 1. 현재 기록
        prompt.append("## 오늘 러닝 기록\n");
        prompt.append(formatRecordDetails(currentRecord));
        prompt.append("\n\n");

        // 2. 과거 기록 통계
        if (!recentRecords.isEmpty()) {
            prompt.append("## 최근 러닝 기록 (참고용)\n");

            // 평균 계산
            double avgDistance = recentRecords.stream()
                    .map(RunningRecord::getDistance)
                    .filter(d -> d != null)
                    .mapToDouble(BigDecimal::doubleValue)
                    .average()
                    .orElse(0.0);

            double avgPace = recentRecords.stream()
                    .map(RunningRecord::getAveragePaceSeconds)
                    .filter(p -> p != null && p > 0)
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);

            prompt.append(String.format("- 최근 %d회 평균 거리: %.2f km\n", recentRecords.size(), avgDistance));
            prompt.append(String.format("- 최근 %d회 평균 페이스: %s\n", recentRecords.size(), formatPace((int) avgPace)));

            // 최고 기록
            RunningRecord bestDistance = recentRecords.stream()
                    .max((r1, r2) -> {
                        BigDecimal d1 = r1.getDistance() != null ? r1.getDistance() : BigDecimal.ZERO;
                        BigDecimal d2 = r2.getDistance() != null ? r2.getDistance() : BigDecimal.ZERO;
                        return d1.compareTo(d2);
                    })
                    .orElse(null);

            if (bestDistance != null && bestDistance.getDistance() != null) {
                prompt.append(String.format("- 최장 거리 기록: %.2f km\n", bestDistance.getDistance()));
            }

            RunningRecord bestPace = recentRecords.stream()
                    .filter(r -> r.getAveragePaceSeconds() != null && r.getAveragePaceSeconds() > 0)
                    .min((r1, r2) -> Integer.compare(r1.getAveragePaceSeconds(), r2.getAveragePaceSeconds()))
                    .orElse(null);

            if (bestPace != null) {
                prompt.append(String.format("- 최고 페이스 기록: %s\n", formatPace(bestPace.getAveragePaceSeconds())));
            }
        }

        prompt.append("\n## 요청\n");
        prompt.append("위 데이터를 바탕으로 오늘 러닝에 대한 구체적인 피드백을 제공해주세요.\n");
        prompt.append("과거 기록과 비교하여 성장한 부분과 개선할 부분을 분석해주세요.");

        return prompt.toString();
    }

    /**
     * 러닝 기록 상세 정보 포맷
     */
    private String formatRecordDetails(RunningRecord record) {
        BigDecimal distance = record.getDistance() != null ? record.getDistance() : BigDecimal.ZERO;
        Integer duration = record.getDuration() != null ? record.getDuration() : 0;
        Integer pace = record.getAveragePaceSeconds() != null ? record.getAveragePaceSeconds() : 0;
        Integer calories = record.getCalories() != null ? record.getCalories() : 0;

        return String.format("""
                - 거리: %s km
                - 시간: %s
                - 평균 페이스: %s
                - 칼로리: %d kcal
                - 러닝 타입: %s
                """,
                distance.setScale(2, RoundingMode.HALF_UP),
                formatDuration(duration),
                formatPace(pace),
                calories,
                record.getRunningType() != null ? record.getRunningType().name() : "일반"
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
