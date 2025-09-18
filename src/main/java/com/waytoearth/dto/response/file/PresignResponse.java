package com.waytoearth.dto.response.file;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(name = "PresignResponse", description = "S3 Presigned URL 응답")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PresignResponse {

    @Schema(description = "업로드용 Presigned URL")
    private String uploadUrl;

    @Schema(description = "다운로드용 Presigned URL")
    private String downloadUrl;

    @Schema(description = "S3 오브젝트 키", example = "profiles/12345/profile.png")
    private String key;

    @Schema(description = "만료 시간(초)", example = "300")
    private int expiresIn;

    public PresignResponse() { }

    public PresignResponse(String uploadUrl, String downloadUrl, String key, int expiresIn) {
        this.uploadUrl = uploadUrl;
        this.downloadUrl = downloadUrl;
        this.key = key;
        this.expiresIn = expiresIn;
    }
}
