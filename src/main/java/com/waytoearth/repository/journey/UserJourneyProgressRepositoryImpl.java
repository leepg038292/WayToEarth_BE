package com.waytoearth.repository.journey;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.waytoearth.dto.response.journey.JourneyProgressResponse;
import com.waytoearth.dto.response.journey.LandmarkSummaryResponse;
import com.waytoearth.entity.enums.JourneyProgressStatus;
import com.waytoearth.entity.journey.QLandmarkEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.waytoearth.entity.journey.QJourneyEntity.journeyEntity;
import static com.waytoearth.entity.journey.QLandmarkEntity.landmarkEntity;
import static com.waytoearth.entity.journey.QStampEntity.stampEntity;
import static com.waytoearth.entity.journey.QUserJourneyProgressEntity.userJourneyProgressEntity;

@Repository
@RequiredArgsConstructor
public class UserJourneyProgressRepositoryImpl implements UserJourneyProgressRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final LandmarkRepository landmarkRepository;

    private static class JourneyProgressDto {
        Long progressId;
        Long journeyId;
        String journeyTitle;
        Double totalDistanceKm;
        Double currentDistanceKm;
        Double progressPercent;
        JourneyProgressStatus status;
        Long nextLandmarkId; // 다음 랜드마크 ID만 조회

        public JourneyProgressDto(Long progressId, Long journeyId, String journeyTitle, Double totalDistanceKm,
                                  Double currentDistanceKm, Double progressPercent, JourneyProgressStatus status, Long nextLandmarkId) {
            this.progressId = progressId;
            this.journeyId = journeyId;
            this.journeyTitle = journeyTitle;
            this.totalDistanceKm = totalDistanceKm;
            this.currentDistanceKm = currentDistanceKm;
            this.progressPercent = progressPercent;
            this.status = status;
            this.nextLandmarkId = nextLandmarkId;
        }
    }

    @Override
    public List<JourneyProgressResponse> findProgressResponsesByUserId(Long userId) {
        // 1. 여정 진행 정보와 각종 통계, 다음 랜드마크 ID를 한 번에 조회
        List<JourneyProgressDto> progressDtos = queryFactory
                .select(Projections.constructor(JourneyProgressDto.class,
                        userJourneyProgressEntity.id,
                        journeyEntity.id,
                        journeyEntity.title,
                        journeyEntity.totalDistanceKm,
                        userJourneyProgressEntity.currentDistanceKm,
                        userJourneyProgressEntity.progressPercent,
                        userJourneyProgressEntity.status,
                        // 다음 랜드마크 ID 서브쿼리
                        JPAExpressions.select(landmarkEntity.id.min())
                                .from(landmarkEntity)
                                .where(landmarkEntity.journey.id.eq(journeyEntity.id)
                                        .and(landmarkEntity.distanceFromStart.gt(userJourneyProgressEntity.currentDistanceKm)))
                ))
                .from(userJourneyProgressEntity)
                .join(userJourneyProgressEntity.journey, journeyEntity)
                .where(userJourneyProgressEntity.user.id.eq(userId))
                .orderBy(userJourneyProgressEntity.createdAt.desc())
                .fetch();

        if (progressDtos.isEmpty()) {
            return List.of();
        }

        // 2. 다음 랜드마크 정보를 한 번의 쿼리로 조회
        List<Long> nextLandmarkIds = progressDtos.stream()
                .map(dto -> dto.nextLandmarkId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, LandmarkSummaryResponse> nextLandmarkMap = landmarkRepository.findAllById(nextLandmarkIds).stream()
                .map(LandmarkSummaryResponse::from)
                .collect(Collectors.toMap(LandmarkSummaryResponse::id, Function.identity()));

        // 3. 각 여정별 추가 정보(스탬프, 총 랜드마크, 함께하는 러너)를 각각의 쿼리로 조회
        // 이 부분도 최적화가 가능하지만, 우선 N+1의 주범인 루프 내 쿼리를 제거
        Map<Long, Long> collectedStampsMap = getCollectedStampsCount(progressDtos);
        Map<Long, Long> totalLandmarksMap = getTotalLandmarksCount(progressDtos);
        Map<Long, Long> runningTogetherMap = getRunningTogetherCount(progressDtos);


        // 4. DTO 조합
        return progressDtos.stream()
                .map(dto -> new JourneyProgressResponse(
                        dto.progressId,
                        dto.journeyId,
                        dto.journeyTitle,
                        dto.totalDistanceKm,
                        dto.currentDistanceKm,
                        dto.progressPercent,
                        dto.status.name(),
                        dto.nextLandmarkId != null ? nextLandmarkMap.get(dto.nextLandmarkId) : null,
                        collectedStampsMap.getOrDefault(dto.progressId, 0L).intValue(),
                        totalLandmarksMap.getOrDefault(dto.journeyId, 0L).intValue(),
                        runningTogetherMap.getOrDefault(dto.journeyId, 0L)
                ))
                .collect(Collectors.toList());
    }

    private Map<Long, Long> getCollectedStampsCount(List<JourneyProgressDto> progressDtos) {
        List<Long> progressIds = progressDtos.stream().map(dto -> dto.progressId).toList();
        return queryFactory
                .select(stampEntity.progress.id, stampEntity.count())
                .from(stampEntity)
                .where(stampEntity.progress.id.in(progressIds))
                .groupBy(stampEntity.progress.id)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(stampEntity.progress.id),
                        tuple -> tuple.get(stampEntity.count())
                ));
    }

    private Map<Long, Long> getTotalLandmarksCount(List<JourneyProgressDto> progressDtos) {
        List<Long> journeyIds = progressDtos.stream().map(dto -> dto.journeyId).distinct().toList();
        return queryFactory
                .select(landmarkEntity.journey.id, landmarkEntity.count())
                .from(landmarkEntity)
                .where(landmarkEntity.journey.id.in(journeyIds))
                .groupBy(landmarkEntity.journey.id)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(landmarkEntity.journey.id),
                        tuple -> tuple.get(landmarkEntity.count())
                ));
    }

    private Map<Long, Long> getRunningTogetherCount(List<JourneyProgressDto> progressDtos) {
        QLandmarkEntity ujpSub = new QLandmarkEntity("ujpSub");
        List<Long> journeyIds = progressDtos.stream().map(dto -> dto.journeyId).distinct().toList();
        
        // QUserJourneyProgressEntity를 별칭으로 사용해야 함
        com.waytoearth.entity.journey.QUserJourneyProgressEntity subUjp = new com.waytoearth.entity.journey.QUserJourneyProgressEntity("subUjp");

        return queryFactory
                .select(subUjp.journey.id, subUjp.user.id.countDistinct())
                .from(subUjp)
                .where(subUjp.journey.id.in(journeyIds)
                        .and(subUjp.status.in(JourneyProgressStatus.ACTIVE, JourneyProgressStatus.COMPLETED)))
                .groupBy(subUjp.journey.id)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(subUjp.journey.id),
                        tuple -> tuple.get(subUjp.user.id.countDistinct())
                ));
    }
}
