package com.waytoearth.service.crew;

import com.waytoearth.dto.response.crew.CrewWeeklyCompareResponse;
import com.waytoearth.dto.response.crew.CrewWeeklyDailyResponse;
import com.waytoearth.repository.running.RunningRecordRepository;
import com.waytoearth.repository.statistics.CrewWeeklyStatsMemberDto;
import com.waytoearth.repository.statistics.CrewDailySumDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CrewWeeklyStatsServiceImpl implements CrewWeeklyStatsService {

    private final RunningRecordRepository runningRecordRepository;

    @Override
    public CrewWeeklyCompareResponse getWeeklyCompare(Long crewId, LocalDate weekStart, int limit) {
        LocalDateTime thisStart = weekStart.atStartOfDay();
        LocalDateTime thisEnd = weekStart.plusDays(6).atTime(LocalTime.MAX);
        LocalDateTime lastStart = weekStart.minusWeeks(1).atStartOfDay();
        LocalDateTime lastEnd = weekStart.minusWeeks(1).plusDays(6).atTime(LocalTime.MAX);

        List<CrewWeeklyStatsMemberDto> raw = runningRecordRepository.getCrewWeeklyCompare(
                crewId, thisStart, thisEnd, lastStart, lastEnd);

        // 정렬: 이번주 합계 desc
        List<CrewWeeklyStatsMemberDto> sorted = raw.stream()
                .sorted(Comparator.comparingDouble(CrewWeeklyStatsMemberDto::getThisWeek).reversed())
                .collect(Collectors.toList());

        // 상위 limit & 랭크 부여(1부터)
        List<CrewWeeklyCompareResponse.Member> members = sorted.stream()
                .limit(limit)
                .map((dto) -> new CrewWeeklyCompareResponse.Member(
                        dto.getUserId(),
                        dto.getName(),
                        round1(dto.getThisWeek()),
                        round1(dto.getLastWeek()),
                        0 // placeholder, rank set below
                ))
                .collect(Collectors.toList());

        for (int i = 0; i < members.size(); i++) {
            CrewWeeklyCompareResponse.Member m = members.get(i);
            members.set(i, new CrewWeeklyCompareResponse.Member(
                    m.getUserId(), m.getName(), m.getThisWeek(), m.getLastWeek(), i + 1));
        }

        double thisWeekTotal = round1(raw.stream().mapToDouble(CrewWeeklyStatsMemberDto::getThisWeek).sum());
        double lastWeekTotal = round1(raw.stream().mapToDouble(CrewWeeklyStatsMemberDto::getLastWeek).sum());

        Double growth = computeGrowthRate(thisWeekTotal, lastWeekTotal);

        return new CrewWeeklyCompareResponse(thisWeekTotal, lastWeekTotal, growth, members);
    }

    @Override
    public CrewWeeklyDailyResponse getWeeklyDaily(Long crewId, LocalDate weekStart) {
        LocalDateTime thisStart = weekStart.atStartOfDay();
        LocalDateTime thisEnd = weekStart.plusDays(6).atTime(LocalTime.MAX);
        LocalDateTime lastStart = weekStart.minusWeeks(1).atStartOfDay();
        LocalDateTime lastEnd = weekStart.minusWeeks(1).plusDays(6).atTime(LocalTime.MAX);

        List<CrewDailySumDto> thisWeek = runningRecordRepository.getCrewDailySums(
                crewId, thisStart, thisEnd);
        List<CrewDailySumDto> lastWeek = runningRecordRepository.getCrewDailySums(
                crewId, lastStart, lastEnd);

        java.util.Map<java.time.LocalDate, Double> thisMap = thisWeek.stream()
                .collect(java.util.stream.Collectors.toMap(CrewDailySumDto::getDate, CrewDailySumDto::getDistance));
        java.util.Map<java.time.LocalDate, Double> lastMap = lastWeek.stream()
                .collect(java.util.stream.Collectors.toMap(CrewDailySumDto::getDate, CrewDailySumDto::getDistance));

        java.util.List<CrewWeeklyDailyResponse.Day> days = new java.util.ArrayList<>();
        double thisTotal = 0.0;
        double lastTotal = 0.0;
        for (int i = 0; i < 7; i++) {
            var d = weekStart.plusDays(i);
            double tw = round1(thisMap.getOrDefault(d, 0.0));
            double lw = round1(lastMap.getOrDefault(d.minusWeeks(1), 0.0));
            thisTotal += tw;
            lastTotal += lw;
            days.add(new CrewWeeklyDailyResponse.Day(
                    d.toString(), d.getDayOfWeek().name(), tw, lw
            ));
        }
        thisTotal = round1(thisTotal);
        lastTotal = round1(lastTotal);
        Double growth = computeGrowthRate(thisTotal, lastTotal);

        return new CrewWeeklyDailyResponse(thisTotal, lastTotal, growth, days);
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private static Double computeGrowthRate(double current, double previous) {
        if (previous <= 0.0) {
            if (current <= 0.0) return 0.0; // 둘 다 0이면 0%
            return null; // 분모 0이면서 증가 -> 무한대/표시 위임
        }
        double rate = ((current - previous) / previous) * 100.0;
        return Math.round(rate * 10.0) / 10.0;
    }
}
