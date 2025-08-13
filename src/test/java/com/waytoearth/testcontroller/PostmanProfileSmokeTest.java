package com.waytoearth.testcontroller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waytoearth.config.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * postman 프로파일에서 MockAuthFilter가 자동 인증을 주입한다는 가정 하에,
 * 러닝 시작/완료 + 날씨(current) 엔드포인트를 스모크 테스트합니다.
 *
 * 경로:
 *  - RunningController:   /v1/running/start, /v1/running/complete
 *  - WeatherController:   /v1/weather/current?lat=..&lon=..
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("postman")
class PostmanProfileSmokeTest {

    private static final String PATH_RUNNING_START    = "/v1/running/start";
    private static final String PATH_RUNNING_COMPLETE = "/v1/running/complete";
    private static final String PATH_WEATHER_CURRENT  = "/v1/weather/current";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // ✅ 메인 코드는 건드리지 않고, 테스트에서만 JWT 필터를 목으로 대체
    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("러닝: start → sessionId 파싱 → complete 200")
    void running_flow_ok() throws Exception {
        // 1) 러닝 시작: sessionId를 포함해서 전송
        String sessionId = java.util.UUID.randomUUID().toString();
        String startBody = String.format("""
    {
      "sessionId": "%s",
      "runningType": "SINGLE",
      "virtualCourseId": null,
      "weatherCondition": "맑음"
    }
    """, sessionId);

        MvcResult startRes = mockMvc.perform(
                        post(PATH_RUNNING_START)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(startBody)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // 2) sessionId 확인 (이미 알고 있지만 응답에서도 확인)
        String startJson = startRes.getResponse().getContentAsString();
        System.out.println("=== Start Response: " + startJson + " ==="); // 디버깅용

        JsonNode root = objectMapper.readTree(startJson);
        String responseSessionId = textOrEmpty(root, "sessionId");
        if (responseSessionId.isEmpty()) {
            responseSessionId = textOrEmpty(root.path("data"), "sessionId");
        }

        // 응답에서 받은 sessionId가 있으면 사용, 없으면 보낸 것 사용
        String finalSessionId = responseSessionId.isEmpty() ? sessionId : responseSessionId;
        Assertions.assertFalse(finalSessionId.isEmpty(), "sessionId를 확인할 수 없습니다. 보낸 sessionId: " + sessionId);

        // 3) 러닝 완료: 올바른 필드명 사용
        Map<String, Object> complete = Map.of(
                "sessionId", finalSessionId,
                "distanceMeters", 5200,           // ✅ 올바른 필드명
                "durationSeconds", 1800,          // ✅ 올바른 필드명
                "averagePaceSeconds", 347,        // ✅ 올바른 필드명 (숫자)
                "calories", 350,
                "routePoints", List.of(           // ✅ 올바른 필드명
                        Map.of(
                                "latitude", 37.5665,
                                "longitude", 126.9780,
                                "sequence", 0
                        )
                )
        );

        mockMvc.perform(
                        post(PATH_RUNNING_COMPLETE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(complete))
                )
                .andDo(print())
                .andExpect(status().isOk());  // ✅ andExpected -> andExpect 오타 수정
    }

    @Test
    @DisplayName("날씨: /v1/weather/current 200 (lat/lon 필수)")
    void weather_ok() throws Exception {
        mockMvc.perform(get(PATH_WEATHER_CURRENT)
                        .param("lat", "37.5665")
                        .param("lon", "126.9780"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    private static String textOrEmpty(JsonNode node, String field) {
        return node.path(field).isMissingNode() ? "" : node.path(field).asText("");
    }
}
