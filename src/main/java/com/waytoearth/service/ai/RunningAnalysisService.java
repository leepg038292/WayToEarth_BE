package com.waytoearth.service.ai;

import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.waytoearth.dto.response.running.ai.RunningAnalysisResponse;
import com.waytoearth.entity.running.RunningFeedback;
import com.waytoearth.entity.running.RunningRecord;
import com.waytoearth.entity.user.User;
import com.waytoearth.exception.DuplicateResourceException;
import com.waytoearth.exception.InvalidParameterException;
import com.waytoearth.exception.UserNotFoundException;
import com.waytoearth.repository.running.RunningFeedbackRepository;
import com.waytoearth.repository.running.RunningRecordRepository;
import com.waytoearth.repository.user.UserRepository;
import com.waytoearth.service.ratelimit.AIAnalysisRateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final UserRepository userRepository;
    private final AIAnalysisRateLimiter rateLimiter;

    @Value("${openai.min-completed-records}")
    private int minCompletedRecords;

    @Value("${openai.analysis-history-limit}")
    private int analysisHistoryLimit;

    /**
     * 새로운 AI 분석 생성 (POST)
     * - 이미 분석된 기록은 409 Conflict
     * - 미완료 기록은 분석 불가
     *
     * @param runningRecordId 러닝 기록 ID
     * @param userId          요청 사용자 ID
     * @return 분석 결과
     */
    @Transactional
    public RunningAnalysisResponse createNewAnalysis(Long runningRecordId, Long userId) {
        // 0. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        // 1. 러닝 기록 조회 및 권한 검증
        RunningRecord runningRecord = runningRecordRepository.findByIdAndUser(runningRecordId, user)
                .orElseThrow(() -> new InvalidParameterException("러닝 기록을 찾을 수 없거나 접근 권한이 없습니다."));

        // 2. 완료된 기록만 분석 가능
        if (!runningRecord.getIsCompleted()) {
            throw new InvalidParameterException("완료된 러닝 기록만 분석할 수 있습니다.");
        }

        // 3. 일일 분석 횟수 제한 확인
        if (!rateLimiter.canAnalyze(userId)) {
            int used = rateLimiter.getUsedCount(userId);
            throw new InvalidParameterException(
                    String.format("일일 AI 분석 횟수를 초과했습니다. (사용: %d회, 내일 다시 시도해주세요)", used)
            );
        }

        // 4. 이미 분석된 기록인지 확인 (중복 방지)
        if (runningFeedbackRepository.existsByRunningRecord(runningRecord)) {
            throw new DuplicateResourceException("이미 AI 분석이 완료된 기록입니다. GET 요청으로 조회하세요.");
        }

        // 5. 최소 완료 기록 수 검증
        long completedCount = runningRecordRepository.countByUserAndIsCompletedTrue(user);
        if (completedCount < minCompletedRecords) {
            throw new InvalidParameterException(
                    String.format("AI 분석을 위해서는 최소 %d회 이상의 완료된 러닝 기록이 필요합니다. (현재: %d회)",
                            minCompletedRecords, completedCount)
            );
        }

        // 6. 분석 횟수 증가 (실제 OpenAI API 호출 전)
        rateLimiter.incrementCount(userId);

        // 7. 새로운 피드백 생성
        return generateNewFeedback(runningRecord, userId);
    }

    /**
     * 기존 AI 분석 조회 (GET)
     * - 캐싱된 데이터만 반환
     * - 없으면 404 Not Found
     *
     * @param runningRecordId 러닝 기록 ID
     * @param userId          요청 사용자 ID
     * @return 분석 결과
     */
    @Transactional(readOnly = true)
    public RunningAnalysisResponse getExistingAnalysis(Long runningRecordId, Long userId) {
        // 0. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        // 1. 러닝 기록 조회 및 권한 검증
        RunningRecord runningRecord = runningRecordRepository.findByIdAndUser(runningRecordId, user)
                .orElseThrow(() -> new InvalidParameterException("러닝 기록을 찾을 수 없거나 접근 권한이 없습니다."));

        // 2. 피드백 조회
        RunningFeedback feedback = runningFeedbackRepository.findByRunningRecord(runningRecord)
                .orElseThrow(() -> new InvalidParameterException("AI 분석 기록이 없습니다. POST 요청으로 새로 분석하세요."));

        return toResponse(feedback);
    }

    /**
     * 새로운 AI 피드백 생성
     */
    private RunningAnalysisResponse generateNewFeedback(RunningRecord currentRecord, Long userId) {
        log.info("Generating new AI feedback for running record: {}", currentRecord.getId());

        // 1. 과거 러닝 기록 조회 (설정 가능한 개수, 현재 기록 제외)
        List<RunningRecord> recentRecords = runningRecordRepository
                .findAllByUserIdAndIsCompletedTrueOrderByStartedAtDesc(userId)
                .stream()
                .filter(r -> !r.getId().equals(currentRecord.getId()))
                .limit(analysisHistoryLimit)
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
                .promptTokens((int) result.getUsage().getPromptTokens())
                .completionTokens((int) result.getUsage().getCompletionTokens())
                .totalTokens((int) result.getUsage().getTotalTokens())
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
                You are a professional running coach with 10 years of experience specializing in data-driven analysis.
                You analyze user's running data with **specific numbers** and provide **actionable advice**.

                ## Analysis Checklist (Must verify all items)

                1. **Pace Consistency**: If segment pace data is available, analyze it
                   - Compare early vs late pace
                   - If difference > 40 seconds: "Warn about starting too fast, need better pacing"
                   - If difference < 20 seconds: "Excellent consistency, perfect pace distribution"

                2. **Trend Analysis**: If trend data is available, mention it
                   - Compare recent records vs previous records
                   - Use specific expressions like "Recent 3 runs improved by 15 seconds compared to previous 3"
                   - If improving: "At this rate, you can achieve [goal]"
                   - If stagnant/declining: "Focus on recovery or adjust training intensity"

                3. **Weekday Pattern**: If weekday data is available, provide insights
                   - Example: "Your Tuesday pace is fastest, try this pace on weekends too"
                   - If large variance exists, infer reasons (weekday fatigue, weekend leisure, etc.)

                4. **Goal Setting**: Suggest next goals based on current level (achievable within 2 weeks)
                   - Distance goal: Current +1km or +20%
                   - Pace goal: Current -10~20 seconds/km
                   - NO unrealistic goals (e.g., 2x faster pace)

                5. **Motivation**: Encourage with specific numbers
                   - "You've improved by average 15 seconds per week for the past 3 weeks"
                   - "You ran 4 times this month, double the 2 times last month"

                ## Output Format (Use Markdown)

                ### 오늘 러닝 요약
                [Summarize today's key metrics in 1-2 lines]

                ### 성장 분석
                [Evaluate growth based on trend data]

                ### 페이스 분배
                [Evaluate pace consistency - only if segment data exists]

                ### 다음 목표
                1. 거리: [Specific distance + estimated time]
                2. 페이스: [Target pace + expected achievement period]

                ## Important Rules
                - **CRITICAL: Always respond in Korean (한국어)**
                - Use casual speech (반말) and maintain a friendly tone
                - NO emojis
                - Always use specific numbers (NOT "a bit" → "15 seconds", NOT "more" → "1.5km")
                - Do NOT speculate about data that isn't provided - skip if not available
                - Maximize usage of provided statistical data
                - Total length: 8-12 sentences

                [Future enhancements]
                - Heart rate based intensity analysis
                - Cadence analysis (stride efficiency)
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

    // ==================== 통계 분석 메서드 ====================

    /**
     * 추세 분석: 최근 3회 vs 이전 3회 비교
     * - 페이스 개선도
     * - 거리 증가 추세
     *
     * @param recentRecords 과거 러닝 기록 (최신순 정렬)
     * @return 추세 분석 결과 문자열
     */
    private String analyzeTrend(List<RunningRecord> recentRecords) {
        if (recentRecords.size() < 6) {
            return "다음 러닝부터는 성장 추세 분석도 제공됩니다! (6회 이상 기록 필요, 현재: " + recentRecords.size() + "회)";
        }

        // 최근 3회 vs 이전 3회로 분할
        List<RunningRecord> recent3 = recentRecords.subList(0, 3);
        List<RunningRecord> previous3 = recentRecords.subList(3, 6);

        // 1. 페이스 추세 분석
        double recentAvgPace = recent3.stream()
                .map(RunningRecord::getAveragePaceSeconds)
                .filter(p -> p != null && p > 0)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        double previousAvgPace = previous3.stream()
                .map(RunningRecord::getAveragePaceSeconds)
                .filter(p -> p != null && p > 0)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        // 2. 거리 추세 분석
        double recentAvgDistance = recent3.stream()
                .map(RunningRecord::getDistance)
                .filter(d -> d != null)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);

        double previousAvgDistance = previous3.stream()
                .map(RunningRecord::getDistance)
                .filter(d -> d != null)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);

        StringBuilder trend = new StringBuilder();
        trend.append("### 최근 추세 (최근 3회 vs 이전 3회)\n");

        // 페이스 개선도
        double paceImprovement = previousAvgPace - recentAvgPace;
        if (paceImprovement > 10) {
            trend.append(String.format("- 페이스: %s → %s (%.0f초 개선, 상승 추세)\n",
                    formatPace((int) previousAvgPace),
                    formatPace((int) recentAvgPace),
                    paceImprovement));
        } else if (paceImprovement < -10) {
            trend.append(String.format("- 페이스: %s → %s (%.0f초 느려짐, 주의 필요)\n",
                    formatPace((int) previousAvgPace),
                    formatPace((int) recentAvgPace),
                    -paceImprovement));
        } else {
            trend.append(String.format("- 페이스: %s (안정적 유지 중)\n",
                    formatPace((int) recentAvgPace)));
        }

        // 거리 증가 추세
        double distanceChange = recentAvgDistance - previousAvgDistance;
        if (distanceChange > 0.5) {
            trend.append(String.format("- 거리: %.2f km → %.2f km (+%.2f km, 증가 추세)\n",
                    previousAvgDistance, recentAvgDistance, distanceChange));
        } else if (distanceChange < -0.5) {
            trend.append(String.format("- 거리: %.2f km → %.2f km (%.2f km 감소)\n",
                    previousAvgDistance, recentAvgDistance, distanceChange));
        } else {
            trend.append(String.format("- 거리: %.2f km (안정적 유지 중)\n",
                    recentAvgDistance));
        }

        return trend.toString();
    }

    /**
     * 요일별 패턴 분석
     * - 요일별 평균 페이스
     * - 가장 잘 뛰는 요일 vs 힘든 요일
     *
     * @param recentRecords 과거 러닝 기록 (최신순 정렬)
     * @return 요일별 패턴 분석 결과 문자열
     */
    private String analyzeWeekdayPattern(List<RunningRecord> recentRecords) {
        if (recentRecords.isEmpty()) {
            return "";
        }

        // 요일별 페이스 그룹핑
        Map<DayOfWeek, List<Integer>> paceByDay = recentRecords.stream()
                .filter(r -> r.getStartedAt() != null && r.getAveragePaceSeconds() != null && r.getAveragePaceSeconds() > 0)
                .collect(Collectors.groupingBy(
                        r -> r.getStartedAt().getDayOfWeek(),
                        Collectors.mapping(RunningRecord::getAveragePaceSeconds, Collectors.toList())
                ));

        // 요일별 평균 페이스 계산
        Map<DayOfWeek, Double> avgPaceByDay = paceByDay.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().mapToInt(Integer::intValue).average().orElse(0.0)
                ));

        if (avgPaceByDay.isEmpty()) {
            return "";
        }

        StringBuilder pattern = new StringBuilder();
        pattern.append("### 요일별 패턴\n");

        // 가장 빠른 요일 (페이스 값이 작을수록 빠름)
        Map.Entry<DayOfWeek, Double> bestDay = avgPaceByDay.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .orElse(null);

        // 가장 느린 요일 (페이스 값이 클수록 느림)
        Map.Entry<DayOfWeek, Double> slowestDay = avgPaceByDay.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        if (bestDay != null) {
            String bestDayName = bestDay.getKey().getDisplayName(TextStyle.FULL, Locale.KOREAN);
            pattern.append(String.format("- 가장 잘 뛰는 요일: %s (평균 %s)\n",
                    bestDayName,
                    formatPace(bestDay.getValue().intValue())));
        }

        if (slowestDay != null && !slowestDay.equals(bestDay)) {
            String slowestDayName = slowestDay.getKey().getDisplayName(TextStyle.FULL, Locale.KOREAN);
            pattern.append(String.format("- 가장 느린 요일: %s (평균 %s)\n",
                    slowestDayName,
                    formatPace(slowestDay.getValue().intValue())));
        }

        // 요일별 상세 데이터 (월~일 순서)
        pattern.append("- 요일별 평균 페이스:\n");
        avgPaceByDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String dayName = entry.getKey().getDisplayName(TextStyle.SHORT, Locale.KOREAN);
                    int count = paceByDay.get(entry.getKey()).size();
                    pattern.append(String.format("  • %s: %s (%d회)\n",
                            dayName,
                            formatPace(entry.getValue().intValue()),
                            count));
                });

        return pattern.toString();
    }

    /**
     * 구간별 페이스 분석 (1km 단위)
     * - 초반/중반/후반 페이스 일관성
     * - 페이스 유지 능력 평가
     *
     * @param currentRecord 현재 러닝 기록
     * @return 구간별 페이스 분석 결과 문자열
     */
    private String analyzeSegmentPace(RunningRecord currentRecord) {
        if (currentRecord.getRoutes() == null || currentRecord.getRoutes().isEmpty()) {
            return ""; // 구간 데이터 없음 (워치 미연동)
        }

        // paceSeconds가 있는 route만 필터링
        List<RunningRoute> routesWithPace = currentRecord.getRoutes().stream()
                .filter(r -> r.getPaceSeconds() != null && r.getPaceSeconds() > 0)
                .toList();

        if (routesWithPace.isEmpty()) {
            return ""; // 페이스 데이터 없음
        }

        // 1km 구간별 평균 페이스 계산
        Map<Integer, List<Integer>> paceByKm = new java.util.HashMap<>();
        for (RunningRoute route : routesWithPace) {
            if (route.getCumulativeDistanceMeters() != null) {
                int km = route.getCumulativeDistanceMeters() / 1000; // 0km, 1km, 2km...
                paceByKm.computeIfAbsent(km, k -> new java.util.ArrayList<>()).add(route.getPaceSeconds());
            }
        }

        if (paceByKm.size() < 2) {
            return ""; // 최소 2개 구간 필요
        }

        // 각 km의 평균 페이스 계산
        Map<Integer, Double> avgPaceByKm = new java.util.LinkedHashMap<>();
        paceByKm.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    double avg = entry.getValue().stream().mapToInt(Integer::intValue).average().orElse(0.0);
                    avgPaceByKm.put(entry.getKey(), avg);
                });

        StringBuilder segment = new StringBuilder();
        segment.append("### 구간별 페이스 (1km 단위)\n");

        // 구간별 페이스 나열
        avgPaceByKm.forEach((km, pace) -> {
            segment.append(String.format("- %dkm: %s\n", km + 1, formatPace(pace.intValue())));
        });

        // 초반/후반 페이스 차이 분석
        Double firstKmPace = avgPaceByKm.get(0);
        Double lastKmPace = avgPaceByKm.get(avgPaceByKm.size() - 1);

        if (firstKmPace != null && lastKmPace != null) {
            double paceDiff = lastKmPace - firstKmPace;
            segment.append("\n");
            if (paceDiff > 40) {
                segment.append(String.format("- 페이스 변화: 초반 %s → 후반 %s (%.0f초 느려짐, 초반 과속 주의)\n",
                        formatPace(firstKmPace.intValue()),
                        formatPace(lastKmPace.intValue()),
                        paceDiff));
            } else if (paceDiff > 20) {
                segment.append(String.format("- 페이스 변화: 초반 %s → 후반 %s (%.0f초 느려짐, 적당한 수준)\n",
                        formatPace(firstKmPace.intValue()),
                        formatPace(lastKmPace.intValue()),
                        paceDiff));
            } else if (paceDiff < -20) {
                segment.append(String.format("- 페이스 변화: 초반 %s → 후반 %s (%.0f초 빨라짐, 후반 스퍼트!)\n",
                        formatPace(firstKmPace.intValue()),
                        formatPace(lastKmPace.intValue()),
                        -paceDiff));
            } else {
                segment.append(String.format("- 페이스 변화: 초반 %s → 후반 %s (일관성 우수!)\n",
                        formatPace(firstKmPace.intValue()),
                        formatPace(lastKmPace.intValue())));
            }
        }

        return segment.toString();
    }
}
