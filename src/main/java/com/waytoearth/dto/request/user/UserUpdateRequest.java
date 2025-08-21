// com/waytoearth/dto/request/user/UserUpdateRequest.java
package com.waytoearth.dto.request.user;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {

        @Size(min = 2, max = 20)
        private String nickname;

        @JsonProperty("profile_image_url")
        @JsonAlias({"profileImageUrl"})
        @Pattern(regexp = "^https?://.*$", message = "유효한 URL이어야 합니다.")
        private String profileImageUrl;

        @Size(min = 2, max = 100)
        private String residence;

        @JsonProperty("weekly_goal_distance")
        @DecimalMin(value = "0.01")
        @DecimalMax(value = "999.99")
        private BigDecimal weeklyGoalDistance;
}
