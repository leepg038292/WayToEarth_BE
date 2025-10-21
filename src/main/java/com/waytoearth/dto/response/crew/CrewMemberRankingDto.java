package com.waytoearth.dto.response.crew;

import java.math.BigDecimal;

public class CrewMemberRankingDto {
    private final String month;
    private final Long userId;
    private final String userName;
    private final String profileImageUrl;
    private final BigDecimal totalDistance;
    private final Integer runCount;
    private final Integer rank;

    public CrewMemberRankingDto(String month, Long userId, String userName,
                               BigDecimal totalDistance, Integer runCount, Integer rank) {
        this.month = month;
        this.userId = userId;
        this.userName = userName;
        this.profileImageUrl = null;
        this.totalDistance = totalDistance;
        this.runCount = runCount;
        this.rank = rank;
    }

    public CrewMemberRankingDto(String month, Long userId, String userName, String profileImageUrl,
                               BigDecimal totalDistance, Integer runCount, Integer rank) {
        this.month = month;
        this.userId = userId;
        this.userName = userName;
        this.profileImageUrl = profileImageUrl;
        this.totalDistance = totalDistance;
        this.runCount = runCount;
        this.rank = rank;
    }

    // getters
    public String getMonth() { return month; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public BigDecimal getTotalDistance() { return totalDistance; }
    public Integer getRunCount() { return runCount; }
    public Integer getRank() { return rank; }
}