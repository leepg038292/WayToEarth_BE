package com.waytoearth.repository.statistics;

import java.time.LocalDateTime;
import java.util.List;

public interface CrewWeeklyStatsRepositoryCustom {

    List<CrewWeeklyStatsMemberDto> getCrewWeeklyCompare(Long crewId,
                                                        LocalDateTime thisStart,
                                                        LocalDateTime thisEnd,
                                                        LocalDateTime lastStart,
                                                        LocalDateTime lastEnd);

    List<CrewDailySumDto> getCrewDailySums(Long crewId,
                                           LocalDateTime start,
                                           LocalDateTime end);
}
