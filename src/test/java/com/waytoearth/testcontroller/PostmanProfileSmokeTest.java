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

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * postman 프로파일에서 MockAuthFilter가 자동 인증을 주입한다는 가정 하에
 * 러닝 시작/완료 + 날씨(current) + 일시정지/재개 엔드포인트를 스모크 테스트합니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("postman")
class PostmanProfileSmokeTest {

    private static final String PATH_RUNNING_START    = "/v1/running/start";
    private static final String PATH_RUNNING_UPDATE   = "/v1/running/update";
    private static final String PATH_RUNNING_PAUSE    = "/v1/running/pause";
    private static final String PATH_RUNNING_RESUME   = "/v1/running/resume";
    private static final String PATH_RUNNING_COMPLETE = "/v1/running/complete";
    private static final String PATH_WEATHER_CURRENT  = "/v1/weather/current";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("러닝: start → pause → resume → complete 200")
    void running_full_flow_ok() throws Exception {
        // 1) 러닝 시작
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

        // 응답에서 sessionId 확인
        String startJson = startRes.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(startJson);
        String responseSessionId = textOrEmpty(root, "sessionId");
        if (responseSessionId.isEmpty()) {
            responseSessionId = textOrEmpty(root.path("data"), "sessionId");
        }
        String finalSessionId = responseSessionId.isEmpty() ? sessionId : responseSessionId;
        Assertions.assertFalse(finalSessionId.isEmpty(), "sessionId를 확인할 수 없습니다.");

        // 2) 러닝 일시정지
        String pauseBody = String.format("{\"sessionId\":\"%s\"}", finalSessionId);
        mockMvc.perform(
                        post(PATH_RUNNING_PAUSE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(pauseBody)
                )
                .andDo(print())
                .andExpect(status().isOk());

        // 3) 러닝 재개
        String resumeBody = String.format("{\"sessionId\":\"%s\"}", finalSessionId);
        mockMvc.perform(
                        post(PATH_RUNNING_RESUME)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(resumeBody)
                )
                .andDo(print())
                .andExpect(status().isOk());

        // 4) 러닝 완료
        Map<String, Object> complete = Map.of(
                "sessionId", finalSessionId,
                "distanceMeters", 5200,
                "durationSeconds", 1800,
                "averagePaceSeconds", 347,
                "calories", 350,
                "routePoints", List.of(
                        Map.of(
                                "latitude", 37.5665,
                                "longitude", 126.9780,
                                "sequence", 0
                        )
                )
        );

        MvcResult completeRes = mockMvc.perform(
                        post(PATH_RUNNING_COMPLETE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(complete))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        // 완료 응답 확인
        String completeJson = completeRes.getResponse().getContentAsString();
        JsonNode completeRoot = objectMapper.readTree(completeJson);
        Long recordId = completeRoot.path("runningRecordId").asLong();
        Assertions.assertTrue(recordId > 0, "러닝 완료 recordId가 유효하지 않습니다.");
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
