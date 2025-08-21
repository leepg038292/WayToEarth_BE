package com.waytoearth.repository.statistics;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.waytoearth.dto.response.statistics.RunningWeeklyStatsResponse;
import com.waytoearth.entity.QRunningRecord;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;

public class StatisticsRepositoryCustomImpl implements StatisticsRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QRunningRecord record = QRunningRecord.runningRecord;

    public StatisticsRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public WeeklyStatsDto getWeeklyStats(Long userId, LocalDateTime start, LocalDateTime end) {
        return queryFactory
                .select(Projections.constructor(
                        WeeklyStatsDto.class,
                        //  타입 명시적 캐스팅
                        record.distance.sum().doubleValue(),           // BigDecimal → Double
                        record.duration.sum().longValue(),             // Integer → Long
                        record.averagePaceSeconds.avg(),               // Double (그대로)
                        record.calories.sum().intValue()               // Integer (그대로, 명시적 표현)
                ))
                .from(record)
                .where(
                        record.user.id.eq(userId),
                        record.isCompleted.isTrue(),
                        record.startedAt.between(start, end)
                )
                .fetchOne();
    }

    @Override
    public List<RunningWeeklyStatsResponse.DailyDistance> getDailyDistances(
            Long userId, LocalDateTime start, LocalDateTime end) {

        var dayExp  = record.startedAt.dayOfWeek();            // IntegerExpression
        var distExp = record.distance.sum().doubleValue();     // NumberExpression<Double>

        return queryFactory
                .select(dayExp, distExp)
                .from(record)
                .where(
                        record.user.id.eq(userId),
                        record.isCompleted.isTrue(),
                        record.startedAt.between(start, end)
                )
                .groupBy(dayExp)
                .orderBy(dayExp.asc())
                .fetch()
                .stream()
                .map(tuple -> new RunningWeeklyStatsResponse.DailyDistance(
                        mapDayOfWeek(tuple.get(dayExp)),
                        safeDouble(tuple.get(distExp))
                ))
                .toList();
    }

    private static double safeDouble(Double v) { return v == null ? 0.0 : v; }

    private static String mapDayOfWeek(Integer v) {
        if (v == null) return "UNKNOWN";
        return switch (v) {
            case 1 -> "SUNDAY";
            case 2 -> "MONDAY";
            case 3 -> "TUESDAY";
            case 4 -> "WEDNESDAY";
            case 5 -> "THURSDAY";
            case 6 -> "FRIDAY";
            case 7 -> "SATURDAY";
            default -> "UNKNOWN";
        };
    }
}