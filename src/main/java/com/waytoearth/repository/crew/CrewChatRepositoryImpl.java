package com.waytoearth.repository.crew;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.waytoearth.entity.crew.QCrewChatEntity.crewChatEntity;
import static com.waytoearth.entity.crew.QCrewChatReadStatusEntity.crewChatReadStatusEntity;

/**
 * CrewChat Repository의 QueryDSL 구현체
 * NOT EXISTS 서브쿼리를 LEFT JOIN으로 최적화
 */
@Repository
@RequiredArgsConstructor
public class CrewChatRepositoryImpl implements CrewChatRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public int countUnreadMessagesOptimized(Long crewId, Long userId) {
        Long count = queryFactory
                .select(crewChatEntity.id.count())
                .from(crewChatEntity)
                .leftJoin(crewChatReadStatusEntity)
                    .on(crewChatReadStatusEntity.message.eq(crewChatEntity)
                        .and(crewChatReadStatusEntity.reader.id.eq(userId)))
                .where(crewChatEntity.crew.id.eq(crewId)
                        .and(crewChatEntity.isDeleted.isFalse())
                        .and(crewChatEntity.sender.id.ne(userId))
                        .and(crewChatReadStatusEntity.id.isNull()))
                .fetchOne();

        return count != null ? count.intValue() : 0;
    }
}
