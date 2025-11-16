package com.waytoearth.repository.statistics;

import java.time.LocalDate;

public class CrewDailySumDto {
    private final LocalDate date;
    private final double distance;

    public CrewDailySumDto(Integer year, Integer month, Integer day, Double distance) {
        this.date = LocalDate.of(year, month, day);
        this.distance = distance == null ? 0.0 : distance;
    }

    public LocalDate getDate() { return date; }
    public double getDistance() { return distance; }
}

