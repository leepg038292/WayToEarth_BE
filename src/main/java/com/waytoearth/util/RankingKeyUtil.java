package com.waytoearth.util;

/**
 * Redis 랭킹 키 생성 유틸리티
 */
public class RankingKeyUtil {

    private static final String RANKING_PREFIX = "waytoearth:ranking:";

    /**
     * 크루 내 멤버 랭킹 키
     * @param crewId 크루 ID
     * @param month 월 (YYYY-MM)
     * @return Redis key (예: waytoearth:ranking:member:123:2024-03)
     */
    public static String memberRankingKey(Long crewId, String month) {
        return RANKING_PREFIX + "member:" + crewId + ":" + month;
    }

    /**
     * 전체 크루 랭킹 키
     * @param month 월 (YYYY-MM)
     * @return Redis key (예: waytoearth:ranking:crew:2024-03)
     */
    public static String crewRankingKey(String month) {
        return RANKING_PREFIX + "crew:" + month;
    }

    /**
     * 월별 랭킹 키 패턴 (삭제용)
     * @param month 월 (YYYY-MM)
     * @return Redis key pattern (예: waytoearth:ranking:*:2024-03)
     */
    public static String monthlyRankingPattern(String month) {
        return RANKING_PREFIX + "*:" + month;
    }
}