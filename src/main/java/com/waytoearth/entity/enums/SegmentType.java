package com.waytoearth.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "세그먼트 타입 ENUM")
public enum SegmentType {
    DOMESTIC,      // 국내 구간
    FLIGHT,        // 항공 구간
    INTERNATIONAL  // 해외 구간
}