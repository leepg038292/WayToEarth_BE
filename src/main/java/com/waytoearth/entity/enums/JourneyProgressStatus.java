package com.waytoearth.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JourneyProgressStatus {
    ACTIVE("ACTIVE", "진행 중"),
    COMPLETED("COMPLETED", "완료"),
    PAUSED("PAUSED", "일시정지");

    private final String code;
    private final String description;
}