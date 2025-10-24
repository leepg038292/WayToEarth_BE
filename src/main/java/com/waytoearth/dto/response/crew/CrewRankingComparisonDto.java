package com.waytoearth.dto.response.crew;

public class CrewRankingComparisonDto {
    private final Long crewId;
    private final String crewName;
    private final Integer currentRank;
    private final Integer previousRank;
    private final Integer rankChange;

    public CrewRankingComparisonDto(Long crewId, String crewName,
                                  Integer currentRank, Integer previousRank, Integer rankChange) {
        this.crewId = crewId;
        this.crewName = crewName;
        this.currentRank = currentRank;
        this.previousRank = previousRank;
        this.rankChange = rankChange;
    }

    // getters
    public Long getCrewId() { return crewId; }
    public String getCrewName() { return crewName; }
    public Integer getCurrentRank() { return currentRank; }
    public Integer getPreviousRank() { return previousRank; }
    public Integer getRankChange() { return rankChange; }
}