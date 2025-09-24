package com.waytoearth.dto.response.crew;

public class CrewGrowthDto {
    private final Long crewId;
    private final String currentMonth;
    private final String previousMonth;
    private final Double distanceGrowthRate;
    private final Integer memberGrowthCount;
    private final Double paceImprovement;

    public CrewGrowthDto(Long crewId, String currentMonth, String previousMonth,
                       Double distanceGrowthRate, Integer memberGrowthCount, Double paceImprovement) {
        this.crewId = crewId;
        this.currentMonth = currentMonth;
        this.previousMonth = previousMonth;
        this.distanceGrowthRate = distanceGrowthRate;
        this.memberGrowthCount = memberGrowthCount;
        this.paceImprovement = paceImprovement;
    }

    // getters
    public Long getCrewId() { return crewId; }
    public String getCurrentMonth() { return currentMonth; }
    public String getPreviousMonth() { return previousMonth; }
    public Double getDistanceGrowthRate() { return distanceGrowthRate; }
    public Integer getMemberGrowthCount() { return memberGrowthCount; }
    public Double getPaceImprovement() { return paceImprovement; }
}