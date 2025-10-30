package com.waytoearth.repository.crew;

/**
 * CrewChat Repository의 QueryDSL 커스텀 메서드 정의
 */
public interface CrewChatRepositoryCustom {

    /**
     * 읽지 않은 메시지 개수 조회 (성능 최적화)
     * - NOT EXISTS 상관 서브쿼리 대신 LEFT JOIN + null 체크 사용
     * - 대용량 데이터에서 성능 향상
     */
    int countUnreadMessagesOptimized(Long crewId, Long userId);
}
