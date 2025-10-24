package com.waytoearth.dto.request.crew;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "크루장 권한 이양 요청")
public class OwnershipTransferRequest {

    @NotNull(message = "새로운 크루장 ID는 필수입니다.")
    @Schema(description = "새로운 크루장 사용자 ID", example = "456", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long newOwnerId;
}