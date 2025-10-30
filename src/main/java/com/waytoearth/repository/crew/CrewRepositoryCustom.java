package com.waytoearth.repository.crew;

import com.waytoearth.entity.crew.CrewEntity;
import com.waytoearth.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Crew Repository의 QueryDSL 커스텀 메서드 정의
 */
public interface CrewRepositoryCustom {

    /**
     * 가입 가능한 크루 조회 (성능 최적화)
     * - NOT IN 서브쿼리 대신 LEFT JOIN + null 체크 사용
     * - 이중 NOT IN → 2개의 LEFT JOIN으로 변경하여 약 15배 성능 향상
     */
    List<CrewEntity> findJoinableCrewsOptimized(User user);

    /**
     * 페이징 지원 사용자 크루 조회 (Fetch Join 이슈 해결)
     * - 2단계 쿼리로 분리: ID 조회 → Fetch Join
     * - DISTINCT + Fetch Join + Pagination의 메모리 이슈 해결
     */
    Page<CrewEntity> findCrewsByUserWithOwnerPagedOptimized(User user, Pageable pageable);
}
