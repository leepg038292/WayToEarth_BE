package com.waytoearth.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "가상 코스 진행 상태 ENUM")
public enum VirtualCourseStatus {
    ACTIVE, COMPLETED
}
