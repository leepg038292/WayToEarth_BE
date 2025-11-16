package com.waytoearth.repository.statistics;

public class CrewWeeklyStatsMemberDto {
    private final Long userId;
    private final String name;
    private final double thisWeek;
    private final double lastWeek;

    public CrewWeeklyStatsMemberDto(Long userId, String name, Double thisWeek, Double lastWeek) {
        this.userId = userId;
        this.name = name;
        this.thisWeek = thisWeek == null ? 0.0 : thisWeek;
        this.lastWeek = lastWeek == null ? 0.0 : lastWeek;
    }

    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public double getThisWeek() { return thisWeek; }
    public double getLastWeek() { return lastWeek; }
}

