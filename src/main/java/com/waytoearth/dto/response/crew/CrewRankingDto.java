package com.waytoearth.dto.response.crew;

import java.math.BigDecimal;

public class CrewRankingDto {
    private final String month;
    private final Long crewId;
    private final String crewName;
    private final String profileImageUrl;
    private final BigDecimal totalDistance;
    private final Integer runCount;
    private final Integer rank;

    public CrewRankingDto(String month, Long crewId, String crewName,
                         BigDecimal totalDistance, Integer runCount, Integer rank) {
        this.month = month;
        this.crewId = crewId;
        this.crewName = crewName;
        this.profileImageUrl = null;
        this.totalDistance = totalDistance;
        this.runCount = runCount;
        this.rank = rank;
    }

    public CrewRankingDto(String month, Long crewId, String crewName, String profileImageUrl,
                         BigDecimal totalDistance, Integer runCount, Integer rank) {
        this.month = month;
        this.crewId = crewId;
        this.crewName = crewName;
        this.profileImageUrl = profileImageUrl;
        this.totalDistance = totalDistance;
        this.runCount = runCount;
        this.rank = rank;
    }

    // getters
    public String getMonth() { return month; }
    public Long getCrewId() { return crewId; }
    public String getCrewName() { return crewName; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public BigDecimal getTotalDistance() { return totalDistance; }
    public Integer getRunCount() { return runCount; }
    public Integer getRank() { return rank; }
}