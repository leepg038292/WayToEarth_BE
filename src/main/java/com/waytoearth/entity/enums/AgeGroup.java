package com.waytoearth.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum AgeGroup {
    TEENS("10대"),
    TWENTIES("20대"),
    THIRTIES("30대"),
    FORTIES("40대"),
    FIFTIES("50대"),
    SIXTIES_PLUS("60대 이상");

    private final String label;

    @JsonCreator
    public static AgeGroup from(String value) {
        return Arrays.stream(values())
                .filter(v -> v.label.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid age group: " + value));
    }

    @JsonValue
    public String toValue() {
        return this.label;
    }
}
