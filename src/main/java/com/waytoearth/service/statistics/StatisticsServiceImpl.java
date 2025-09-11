package com.waytoearth.service.statistics;

import com.waytoearth.dto.response.statistics.RunningWeeklyStatsResponse;
import com.waytoearth.repository.Running.RunningRecordRepository;
import com.waytoearth.repository.statistics.WeeklyStatsDto;
import com.waytoearth.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class StatisticsServiceImpl implements StatisticsService {

    private final RunningRecordRepository runningRecordRepository;

    public StatisticsServiceImpl(RunningRecordRepository runningRecordRepository) {
        this.runningRecordRepository = runningRecordRepository;
    }

    @Override
    public RunningWeeklyStatsResponse getWeeklyStats(AuthenticatedUser user) {
        // 🇰🇷 서울 기준 주간 범위 (월~일)
        ZoneId zone = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(zone);
        LocalDate startDate = today.with(DayOfWeek.MONDAY);
        LocalDate endDate = today.with(DayOfWeek.SUNDAY);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        // 합계/평균
        WeeklyStatsDto stats = runningRecordRepository.getWeeklyStats(user.getUserId(), start, end);
        if (stats == null) {
            // 일단 전부 0으로 채워서 반환
            return emptyWeeklyResponse();
        }

        // 요일별 거리 (빈 요일 0으로 보정)
        List<RunningWeeklyStatsResponse.DailyDistance> rawDaily = runningRecordRepository.getDailyDistances(user.getUserId(), start, end);
        List<RunningWeeklyStatsResponse.DailyDistance> filledDaily = fillMissingDays(rawDaily);

        // 평균 페이스 seconds → "mm:ss"
        String avgPace = formatPaceSeconds(stats.getAveragePaceSeconds());

        return new RunningWeeklyStatsResponse(
                round1(stats.getTotalDistance()),
                stats.getTotalDuration(),
                avgPace,
                stats.getTotalCalories(),
                filledDaily
        );
    }

    private RunningWeeklyStatsResponse emptyWeeklyResponse() {
        return new RunningWeeklyStatsResponse(
                0.0,
                0L,
                "00:00",
                0,
                fillMissingDays(new ArrayList<>())
        );
    }

    // 월~일 순서로 비어있는 요일 0km 채움
    private List<RunningWeeklyStatsResponse.DailyDistance> fillMissingDays(List<RunningWeeklyStatsResponse.DailyDistance> raw) {
        Map<DayOfWeek, Double> map = new EnumMap<>(DayOfWeek.class);
        for (RunningWeeklyStatsResponse.DailyDistance d : raw) {
            DayOfWeek dow = DayOfWeek.valueOf(d.getDay());
            map.put(dow, map.getOrDefault(dow, 0.0) + d.getDistance());
        }
        List<RunningWeeklyStatsResponse.DailyDistance> list = new ArrayList<>();
        for (DayOfWeek dow : new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY}) {
            double v = map.getOrDefault(dow, 0.0);
            list.add(new RunningWeeklyStatsResponse.DailyDistance(dow.name(), round1(v)));
        }
        return list;
    }

    private String formatPaceSeconds(Double secondsNullable) {
        if (secondsNullable == null || secondsNullable <= 0) return "00:00";
        long sec = Math.round(secondsNullable);
        long mm = sec / 60;
        long ss = sec % 60;
        return String.format("%02d:%02d", mm, ss);
    }

    private double round1(Double v) {
        if (v == null) return 0.0;
        return Math.round(v * 10.0) / 10.0; // 한 자리 소수로 반올림 (예: 29.7km)
    }
}
