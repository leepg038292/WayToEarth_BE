package com.waytoearth.dto.request.file;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PresignRequest {

    @NotBlank
    private String contentType;

    @Positive
    private long size;

    public PresignRequest(String contentType, long size) {
        this.contentType = contentType;
        this.size = size;
    }
}
