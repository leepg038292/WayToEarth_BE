package com.waytoearth.dto.response.running;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RunningPauseResumeResponse {
    private boolean ack;
    private String status;
}