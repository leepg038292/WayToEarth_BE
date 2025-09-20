package com.waytoearth.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum RunningType {
    SINGLE("SINGLE", "싱글 러닝"),
    JOURNEY("JOURNEY", "여정 러닝");

    private final String code;
    private final String description;

    @JsonValue
    public String getCode() {
        return code;
    }

    public static RunningType fromCode(String code) {
        return Arrays.stream(values())
                .filter(type -> type.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid RunningType code: " + code));
    }
}
