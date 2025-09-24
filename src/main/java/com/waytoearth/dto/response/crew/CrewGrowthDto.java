package com.waytoearth.dto.response.crew;

public class CrewGrowthDto {
    private final Long crewId;
    private final String currentMonth;
    private final String previousMonth;
    private final Double distanceGrowthRate;
    private final Integer memberGrowthCount;
    private final Double paceChange; // 양수: 느려짐, 음수: 빨라짐 (초 단위)

    public CrewGrowthDto(Long crewId, String currentMonth, String previousMonth,
                       Double distanceGrowthRate, Integer memberGrowthCount, Double paceChange) {
        this.crewId = crewId;
        this.currentMonth = currentMonth;
        this.previousMonth = previousMonth;
        this.distanceGrowthRate = distanceGrowthRate;
        this.memberGrowthCount = memberGrowthCount;
        this.paceChange = paceChange;
    }

    // getters
    public Long getCrewId() { return crewId; }
    public String getCurrentMonth() { return currentMonth; }
    public String getPreviousMonth() { return previousMonth; }
    public Double getDistanceGrowthRate() { return distanceGrowthRate; }
    public Integer getMemberGrowthCount() { return memberGrowthCount; }
    public Double getPaceChange() { return paceChange; }
}