package com.waytoearth.dto.response.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoUserInfo {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("properties")
    private KakaoProperties properties;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoProperties {
        @JsonProperty("nickname")
        private String nickname;
    }
}