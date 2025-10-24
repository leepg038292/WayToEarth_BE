package com.waytoearth.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JourneyCategory {
    DOMESTIC("DOMESTIC", "국내"),
    INTERNATIONAL("INTERNATIONAL", "해외");

    private final String code;
    private final String description;
}