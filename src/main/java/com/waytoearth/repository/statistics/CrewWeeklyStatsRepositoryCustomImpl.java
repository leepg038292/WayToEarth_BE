package com.waytoearth.repository.statistics;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.waytoearth.entity.crew.QCrewMemberEntity;
import com.waytoearth.entity.running.QRunningRecord;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;

public class CrewWeeklyStatsRepositoryCustomImpl implements CrewWeeklyStatsRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QCrewMemberEntity cm = QCrewMemberEntity.crewMemberEntity;
    private final QRunningRecord r = QRunningRecord.runningRecord;

    public CrewWeeklyStatsRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<CrewWeeklyStatsMemberDto> getCrewWeeklyCompare(Long crewId,
                                                               LocalDateTime thisStart,
                                                               LocalDateTime thisEnd,
                                                               LocalDateTime lastStart,
                                                               LocalDateTime lastEnd) {

        NumberExpression<Double> thisWeekExpr = new CaseBuilder()
                .when(r.startedAt.between(thisStart, thisEnd)).then(r.distance.doubleValue())
                .otherwise(0.0)
                .sum();

        NumberExpression<Double> lastWeekExpr = new CaseBuilder()
                .when(r.startedAt.between(lastStart, lastEnd)).then(r.distance.doubleValue())
                .otherwise(0.0)
                .sum();

        return queryFactory
                .select(Projections.constructor(
                        CrewWeeklyStatsMemberDto.class,
                        cm.user.id,
                        cm.user.nickname,
                        thisWeekExpr,
                        lastWeekExpr
                ))
                .from(cm)
                .leftJoin(r)
                .on(r.user.id.eq(cm.user.id)
                        .and(r.isCompleted.isTrue())
                        .and(r.startedAt.between(lastStart, thisEnd)))
                .where(cm.crew.id.eq(crewId))
                .groupBy(cm.user.id, cm.user.nickname)
                .fetch();
    }

    @Override
    public List<CrewDailySumDto> getCrewDailySums(Long crewId, LocalDateTime start, LocalDateTime end) {
        var y = r.startedAt.year();
        var m = r.startedAt.month();
        var d = r.startedAt.dayOfMonth();

        // from RunningRecord (inner join) to avoid null year/month/day when no record
        return queryFactory
                .select(Projections.constructor(
                        CrewDailySumDto.class,
                        y,
                        m,
                        d,
                        r.distance.sum().doubleValue()
                ))
                .from(r)
                .join(cm).on(cm.user.id.eq(r.user.id))
                .where(
                        cm.crew.id.eq(crewId),
                        r.isCompleted.isTrue(),
                        r.startedAt.between(start, end)
                )
                .groupBy(y, m, d)
                .orderBy(y.asc(), m.asc(), d.asc())
                .fetch();
    }
}
