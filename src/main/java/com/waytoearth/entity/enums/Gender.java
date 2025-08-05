package com.waytoearth.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Gender {
    MALE("남성"),
    FEMALE("여성");

    private final String value;

    Gender(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Gender fromValue(String value) {
        for (Gender gender : Gender.values()) {
            if (gender.value.equals(value)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Invalid gender value: " + value);
    }
}
