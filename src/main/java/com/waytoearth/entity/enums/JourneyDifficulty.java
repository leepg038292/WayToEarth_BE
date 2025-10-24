package com.waytoearth.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JourneyDifficulty {
    EASY("EASY", "쉬움"),
    MEDIUM("MEDIUM", "보통"),
    HARD("HARD", "어려움");

    private final String code;
    private final String description;
}