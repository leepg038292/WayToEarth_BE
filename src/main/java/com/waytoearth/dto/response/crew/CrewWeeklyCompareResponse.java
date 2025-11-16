package com.waytoearth.dto.response.crew;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "크루 주간/전주 비교 응답")
public class CrewWeeklyCompareResponse {

    @Schema(description = "이번 주 총합 (km)")
    private final double thisWeekTotal;

    @Schema(description = "지난 주 총합 (km)")
    private final double lastWeekTotal;

    @Schema(description = "지난 주 대비 성장률(%)")
    private final Double growthRate;

    @Schema(description = "멤버별 주간/전주 데이터(정렬 및 랭크 포함)")
    private final List<Member> members;

    public CrewWeeklyCompareResponse(double thisWeekTotal, double lastWeekTotal, Double growthRate, List<Member> members) {
        this.thisWeekTotal = thisWeekTotal;
        this.lastWeekTotal = lastWeekTotal;
        this.growthRate = growthRate;
        this.members = members;
    }

    public double getThisWeekTotal() { return thisWeekTotal; }
    public double getLastWeekTotal() { return lastWeekTotal; }
    public Double getGrowthRate() { return growthRate; }
    public List<Member> getMembers() { return members; }

    @Schema(description = "멤버 주간/전주 비교")
    public static class Member {
        @Schema(description = "유저 ID")
        private final Long userId;
        @Schema(description = "표시 이름(닉네임)")
        private final String name;
        @Schema(description = "이번 주 합계(km)")
        private final double thisWeek;
        @Schema(description = "지난 주 합계(km)")
        private final double lastWeek;
        @Schema(description = "랭크(1부터 시작)")
        private final int rank;

        public Member(Long userId, String name, double thisWeek, double lastWeek, int rank) {
            this.userId = userId;
            this.name = name;
            this.thisWeek = thisWeek;
            this.lastWeek = lastWeek;
            this.rank = rank;
        }

        public Long getUserId() { return userId; }
        public String getName() { return name; }
        public double getThisWeek() { return thisWeek; }
        public double getLastWeek() { return lastWeek; }
        public int getRank() { return rank; }
    }
}

