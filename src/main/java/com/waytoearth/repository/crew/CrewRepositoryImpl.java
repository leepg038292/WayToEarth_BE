package com.waytoearth.repository.crew;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.waytoearth.entity.crew.CrewEntity;
import com.waytoearth.entity.crew.CrewJoinRequestEntity;
import com.waytoearth.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.waytoearth.entity.crew.QCrewEntity.crewEntity;
import static com.waytoearth.entity.crew.QCrewMemberEntity.crewMemberEntity;
import static com.waytoearth.entity.crew.QCrewJoinRequestEntity.crewJoinRequestEntity;
import static com.waytoearth.entity.user.QUser.user;

/**
 * Crew Repository의 QueryDSL 구현체
 * 서브쿼리 최적화 및 페이지네이션 이슈 해결
 */
@Repository
@RequiredArgsConstructor
public class CrewRepositoryImpl implements CrewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CrewEntity> findJoinableCrewsOptimized(User userParam) {
        // 서브쿼리 별칭 생성
        var crewMemberSub = crewMemberEntity;
        var crewJoinRequestSub = crewJoinRequestEntity;

        return queryFactory
                .selectFrom(crewEntity)
                .leftJoin(crewMemberSub)
                    .on(crewMemberSub.crew.eq(crewEntity)
                        .and(crewMemberSub.user.eq(userParam)))
                .leftJoin(crewJoinRequestSub)
                    .on(crewJoinRequestSub.crew.eq(crewEntity)
                        .and(crewJoinRequestSub.user.eq(userParam))
                        .and(crewJoinRequestSub.status.eq(CrewJoinRequestEntity.JoinRequestStatus.PENDING)))
                .where(crewMemberSub.id.isNull()
                        .and(crewJoinRequestSub.id.isNull()))
                .orderBy(crewEntity.createdAt.desc())
                .fetch();
    }

    @Override
    public Page<CrewEntity> findCrewsByUserWithOwnerPagedOptimized(User userParam, Pageable pageable) {
        // 1단계: Crew ID만 조회 (페이징 적용)
        List<Long> crewIds = queryFactory
                .select(crewEntity.id)
                .from(crewMemberEntity)
                .where(crewMemberEntity.user.eq(userParam))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(crewEntity.createdAt.desc())
                .fetch();

        // ID가 없으면 빈 페이지 반환
        if (crewIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // 2단계: Fetch Join으로 Crew + Owner 조회
        List<CrewEntity> crews = queryFactory
                .selectFrom(crewEntity)
                .join(crewEntity.owner, user).fetchJoin()
                .where(crewEntity.id.in(crewIds))
                .orderBy(crewEntity.createdAt.desc())
                .fetch();

        // 3단계: 총 개수 조회
        Long total = queryFactory
                .select(crewMemberEntity.crew.countDistinct())
                .from(crewMemberEntity)
                .where(crewMemberEntity.user.eq(userParam))
                .fetchOne();

        return new PageImpl<>(crews, pageable, total != null ? total : 0);
    }
}
