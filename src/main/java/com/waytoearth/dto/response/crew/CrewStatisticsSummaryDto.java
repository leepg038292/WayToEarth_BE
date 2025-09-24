package com.waytoearth.dto.response.crew;

public class CrewStatisticsSummaryDto {
    private final String month;
    private final Integer totalCrews;
    private final Double totalDistance;
    private final Integer totalActiveMembers;
    private final Double averagePace;

    public CrewStatisticsSummaryDto(String month, Integer totalCrews,
                                  Double totalDistance, Integer totalActiveMembers, Double averagePace) {
        this.month = month;
        this.totalCrews = totalCrews;
        this.totalDistance = totalDistance;
        this.totalActiveMembers = totalActiveMembers;
        this.averagePace = averagePace;
    }

    // getters
    public String getMonth() { return month; }
    public Integer getTotalCrews() { return totalCrews; }
    public Double getTotalDistance() { return totalDistance; }
    public Integer getTotalActiveMembers() { return totalActiveMembers; }
    public Double getAveragePace() { return averagePace; }
}