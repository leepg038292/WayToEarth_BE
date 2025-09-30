package com.waytoearth.service.ranking;

import com.waytoearth.dto.response.crew.CrewMemberRankingDto;
import com.waytoearth.dto.response.crew.CrewRankingDto;

import java.util.List;

/**
 * Redis ZSet 기반 고성능 크루 랭킹 서비스
 * - 실시간 랭킹 조회 및 업데이트
 * - DB보다 100배 빠른 O(log N) 성능
 */
public interface CrewRankingService {

    /**
     * 크루 내 멤버 랭킹 조회 (Redis 우선, DB 백업)
     */
    List<CrewMemberRankingDto> getMemberRankingInCrew(Long crewId, String month, int limit);

    /**
     * 전체 크루 랭킹 조회 (Redis 우선, DB 백업)
     */
    List<CrewRankingDto> getCrewRanking(String month, int limit);

    /**
     * 멤버 랭킹 실시간 업데이트
     */
    void updateMemberRanking(Long crewId, Long userId, String month, Double newTotalDistance);

    /**
     * 크루 랭킹 실시간 업데이트
     */
    void updateCrewRanking(Long crewId, String month, Double newTotalDistance);

    /**
     * DB 데이터로 Redis 랭킹 재구축
     */
    void rebuildRankingFromDB(String month);
}