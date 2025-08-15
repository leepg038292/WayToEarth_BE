package com.waytoearth.dto.request.running;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RunningUpdateRequest {
    private String sessionId;
    private double distanceMeters;
    private int durationSeconds;
    private int averagePaceSeconds;
    private int calories;

    private PointDto currentPoint;

    @Getter
    @Setter
    public static class PointDto {
        private double latitude;
        private double longitude;
        private int sequence;
    }
}
