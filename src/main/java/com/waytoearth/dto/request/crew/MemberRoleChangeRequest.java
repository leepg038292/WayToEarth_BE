package com.waytoearth.dto.request.crew;

import com.waytoearth.entity.enums.CrewRole;
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
@Schema(description = "멤버 역할 변경 요청")
public class MemberRoleChangeRequest {

    @NotNull(message = "새로운 역할은 필수입니다.")
    @Schema(description = "새로운 역할", example = "MEMBER", allowableValues = {"MEMBER"})
    private CrewRole newRole;
}