package com.waytoearth.repository.journey;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.waytoearth.dto.response.journey.JourneySummaryResponse;
import com.waytoearth.entity.enums.JourneyCategory;
import com.waytoearth.entity.enums.JourneyProgressStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.waytoearth.entity.journey.QJourneyEntity.journeyEntity;
import static com.waytoearth.entity.journey.QLandmarkEntity.landmarkEntity;
import static com.waytoearth.entity.journey.QUserJourneyProgressEntity.userJourneyProgressEntity;

/**
 * Journey Repository의 QueryDSL 구현체
 * N+1 문제를 해결하기 위해 JOIN + GROUP BY를 사용한 집계 쿼리 제공
 */
@Repository
@RequiredArgsConstructor
public class JourneyRepositoryImpl implements JourneyRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<JourneySummaryResponse> findActiveJourneysWithStats() {
        return queryFactory
                .select(Projections.constructor(
                        JourneySummaryResponse.class,
                        journeyEntity.id,
                        journeyEntity.title,
                        journeyEntity.description,
                        journeyEntity.thumbnailUrl,
                        journeyEntity.totalDistanceKm,
                        journeyEntity.difficulty.stringValue(),
                        journeyEntity.category.stringValue(),
                        journeyEntity.estimatedDays,
                        landmarkEntity.id.countDistinct().intValue(),
                        new CaseBuilder()
                                .when(userJourneyProgressEntity.status.eq(JourneyProgressStatus.COMPLETED))
                                .then(userJourneyProgressEntity.id)
                                .otherwise((Long) null)
                                .countDistinct()
                ))
                .from(journeyEntity)
                .leftJoin(landmarkEntity).on(landmarkEntity.journey.eq(journeyEntity))
                .leftJoin(userJourneyProgressEntity).on(userJourneyProgressEntity.journey.eq(journeyEntity))
                .where(journeyEntity.isActive.isTrue())
                .groupBy(journeyEntity.id,
                        journeyEntity.title,
                        journeyEntity.description,
                        journeyEntity.thumbnailUrl,
                        journeyEntity.totalDistanceKm,
                        journeyEntity.difficulty,
                        journeyEntity.category,
                        journeyEntity.estimatedDays,
                        journeyEntity.createdAt)
                .orderBy(journeyEntity.createdAt.desc())
                .fetch();
    }

    @Override
    public List<JourneySummaryResponse> findJourneysByCategoryWithStats(JourneyCategory category) {
        return queryFactory
                .select(Projections.constructor(
                        JourneySummaryResponse.class,
                        journeyEntity.id,
                        journeyEntity.title,
                        journeyEntity.description,
                        journeyEntity.thumbnailUrl,
                        journeyEntity.totalDistanceKm,
                        journeyEntity.difficulty.stringValue(),
                        journeyEntity.category.stringValue(),
                        journeyEntity.estimatedDays,
                        landmarkEntity.id.countDistinct().intValue(),
                        new CaseBuilder()
                                .when(userJourneyProgressEntity.status.eq(JourneyProgressStatus.COMPLETED))
                                .then(userJourneyProgressEntity.id)
                                .otherwise((Long) null)
                                .countDistinct()
                ))
                .from(journeyEntity)
                .leftJoin(landmarkEntity).on(landmarkEntity.journey.eq(journeyEntity))
                .leftJoin(userJourneyProgressEntity).on(userJourneyProgressEntity.journey.eq(journeyEntity))
                .where(journeyEntity.isActive.isTrue()
                        .and(journeyEntity.category.eq(category)))
                .groupBy(journeyEntity.id,
                        journeyEntity.title,
                        journeyEntity.description,
                        journeyEntity.thumbnailUrl,
                        journeyEntity.totalDistanceKm,
                        journeyEntity.difficulty,
                        journeyEntity.category,
                        journeyEntity.estimatedDays,
                        journeyEntity.createdAt)
                .orderBy(journeyEntity.createdAt.desc())
                .fetch();
    }

    @Override
    public List<JourneySummaryResponse> searchJourneysByTitleWithStats(String keyword) {
        return queryFactory
                .select(Projections.constructor(
                        JourneySummaryResponse.class,
                        journeyEntity.id,
                        journeyEntity.title,
                        journeyEntity.description,
                        journeyEntity.thumbnailUrl,
                        journeyEntity.totalDistanceKm,
                        journeyEntity.difficulty.stringValue(),
                        journeyEntity.category.stringValue(),
                        journeyEntity.estimatedDays,
                        landmarkEntity.id.countDistinct().intValue(),
                        new CaseBuilder()
                                .when(userJourneyProgressEntity.status.eq(JourneyProgressStatus.COMPLETED))
                                .then(userJourneyProgressEntity.id)
                                .otherwise((Long) null)
                                .countDistinct()
                ))
                .from(journeyEntity)
                .leftJoin(landmarkEntity).on(landmarkEntity.journey.eq(journeyEntity))
                .leftJoin(userJourneyProgressEntity).on(userJourneyProgressEntity.journey.eq(journeyEntity))
                .where(journeyEntity.isActive.isTrue()
                        .and(journeyEntity.title.containsIgnoreCase(keyword)))
                .groupBy(journeyEntity.id,
                        journeyEntity.title,
                        journeyEntity.description,
                        journeyEntity.thumbnailUrl,
                        journeyEntity.totalDistanceKm,
                        journeyEntity.difficulty,
                        journeyEntity.category,
                        journeyEntity.estimatedDays,
                        journeyEntity.createdAt)
                .orderBy(journeyEntity.createdAt.desc())
                .fetch();
    }
}
