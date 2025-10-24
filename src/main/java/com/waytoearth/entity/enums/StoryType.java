package com.waytoearth.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StoryType {
    HISTORY("HISTORY", "역사"),
    CULTURE("CULTURE", "문화"),
    NATURE("NATURE", "자연");

    private final String code;
    private final String description;
}