package com.waytoearth.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CrewRole {
    OWNER("OWNER", "크루장"),
    MEMBER("MEMBER", "일반 멤버");

    private final String code;
    private final String description;
}