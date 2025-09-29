package com.waytoearth.service.ranking;

import com.waytoearth.dto.response.crew.CrewMemberRankingDto;
import com.waytoearth.dto.response.crew.CrewRankingDto;

import java.util.List;

/**
 * Redis ZSet 기반 크루 랭킹 서비스
 * - 실시간 랭킹 관리 및 조회
 * - 월별 데이터 관리
 */
public interface CrewRankingService {

    /**
     * 크루 내 멤버 랭킹 조회 (Redis ZSet 기반)
     * @param crewId 크루 ID
     * @param month 조회할 월 (YYYY-MM 형식)
     * @param limit 조회할 순위 수
     * @return 멤버 랭킹 리스트
     */
    List<CrewMemberRankingDto> getMemberRankingInCrew(Long crewId, String month, int limit);

    /**
     * 전체 크루 랭킹 조회 (Redis ZSet 기반)
     * @param month 조회할 월 (YYYY-MM 형식)
     * @param limit 조회할 순위 수
     * @return 크루 랭킹 리스트
     */
    List<CrewRankingDto> getCrewRanking(String month, int limit);

    /**
     * 멤버의 러닝 기록 업데이트 시 랭킹 갱신
     * @param crewId 크루 ID
     * @param userId 사용자 ID
     * @param month 월 (YYYY-MM 형식)
     * @param newTotalDistance 새로운 누적 거리
     */
    void updateMemberRanking(Long crewId, Long userId, String month, Double newTotalDistance);

    /**
     * 크루 전체 거리 업데이트 시 크루 랭킹 갱신
     * @param crewId 크루 ID
     * @param month 월 (YYYY-MM 형식)
     * @param newTotalDistance 새로운 크루 전체 거리
     */
    void updateCrewRanking(Long crewId, String month, Double newTotalDistance);

    /**
     * DB 데이터를 기반으로 Redis 랭킹 재구축
     * @param month 재구축할 월 (YYYY-MM 형식)
     */
    void rebuildRankingFromDB(String month);
}