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

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 🎯 완전한 사용자 플로우 테스트 - 러닝 + 엠블럼 + 피드 + 파일 업로드
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("postman")
class PostmanProfileSmokeTest {

    // 📁 모든 API 경로 정의
    private static final String PATH_EMBLEM_SUMMARY = "/v1/emblems/me/summary";
    private static final String PATH_EMBLEM_CATALOG = "/v1/emblems/catalog";
    private static final String PATH_EMBLEM_DETAIL = "/v1/emblems/{id}";
    private static final String PATH_EMBLEM_AWARD_ONE = "/v1/emblems/{id}/award";
    private static final String PATH_EMBLEM_SCAN_AWARD = "/v1/emblems/award/scan";

    private static final String PATH_FEED_CREATE = "/v1/feeds";
    private static final String PATH_FEED_LIST = "/v1/feeds";
    private static final String PATH_FEED_DETAIL = "/v1/feeds/{feedId}";
    private static final String PATH_FEED_LIKE = "/v1/feeds/{feedId}/like";
    private static final String PATH_FEED_DELETE = "/v1/feeds/{feedId}";

    private static final String PATH_FILE_PRESIGN_PROFILE = "/v1/files/presign/profile";
    private static final String PATH_FILE_PRESIGN_FEED = "/v1/files/presign/feed";

    // ➕ 러닝 API 경로
    private static final String PATH_RUNNING_START = "/v1/running/start";
    private static final String PATH_RUNNING_UPDATE = "/v1/running/update";
    private static final String PATH_RUNNING_PAUSE = "/v1/running/pause";
    private static final String PATH_RUNNING_RESUME = "/v1/running/resume";
    private static final String PATH_RUNNING_COMPLETE = "/v1/running/complete";
    private static final String PATH_RUNNING_DETAIL = "/v1/running/{recordId}";
    private static final String PATH_RUNNING_UPDATE_TITLE = "/v1/running/{recordId}/title";

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // ==============================================
    // 🎯 완전 통합 시나리오 (러닝 → 엠블럼 → 피드 → 파일 업로드)
    // ==============================================
    @Test
    @DisplayName("🚀 완전한 사용자 플로우 (러닝 + 엠블럼 + 피드 + 파일)")
    void complete_user_flow_with_running() throws Exception {
        System.out.println("\n🚀 === 통합 시나리오 시작 ===");

        // 1️⃣ 러닝 시작
        MvcResult startResult = mockMvc.perform(
                        post(PATH_RUNNING_START)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        Map.of("runningType", "OUTDOOR", "title", "테스트 러닝")
                                ))
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
        JsonNode startNode = objectMapper.readTree(startResult.getResponse().getContentAsString());
        String sessionId = startNode.path("sessionId").asText();
        System.out.println("🏃 세션 시작: " + sessionId);

        // 2️⃣ 러닝 업데이트 (경로 1개 추가)
        mockMvc.perform(
                        post(PATH_RUNNING_UPDATE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        Map.of("sessionId", sessionId, "latitude", 37.5665, "longitude", 126.9780, "sequence", 1)
                                ))
                )
                .andExpect(status().isOk())
                .andDo(print());

        // 3️⃣ 러닝 완료
        MvcResult completeResult = mockMvc.perform(
                        post(PATH_RUNNING_COMPLETE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        Map.of("sessionId", sessionId, "distanceMeters", 5000, "durationSeconds", 1800, "calories", 350)
                                ))
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
        JsonNode completeNode = objectMapper.readTree(completeResult.getResponse().getContentAsString());
        Long runningRecordId = completeNode.path("runningRecordId").asLong();
        System.out.println("✅ 러닝 완료 기록 ID: " + runningRecordId);

        // 4️⃣ 엠블럼 요약 확인
        mockMvc.perform(get(PATH_EMBLEM_SUMMARY))
                .andExpect(status().isOk())
                .andDo(print());

        // 5️⃣ 엠블럼 스캔 지급
        mockMvc.perform(post(PATH_EMBLEM_SCAN_AWARD).param("scope", "DISTANCE"))
                .andExpect(status().isOk())
                .andDo(print());

        // 6️⃣ 프로필 이미지 Presigned URL 발급
        mockMvc.perform(
                        post(PATH_FILE_PRESIGN_PROFILE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of("contentType", "image/png", "size", 512000)))
                )
                .andExpect(status().isOk())
                .andDo(print());

        // 7️⃣ 피드 작성 (방금 러닝 기록 연결)
        MvcResult feedResult = mockMvc.perform(
                        post(PATH_FEED_CREATE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "runningRecordId", runningRecordId,
                                        "content", "방금 5km 달림! 🏅",
                                        "imageUrl", "https://example.com/test.jpg"
                                )))
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
        JsonNode feedNode = objectMapper.readTree(feedResult.getResponse().getContentAsString());
        Long feedId = feedNode.path("id").asLong();
        System.out.println("📝 생성된 피드 ID: " + feedId);

        // 8️⃣ 피드 좋아요
        mockMvc.perform(post(PATH_FEED_LIKE, feedId))
                .andExpect(status().isOk())
                .andDo(print());

        // 9️⃣ 피드 삭제
        mockMvc.perform(delete(PATH_FEED_DELETE, feedId))
                .andExpect(status().isOk())
                .andDo(print());

        System.out.println("\n🎉 === 통합 시나리오 완료 ===");
    }

    // ==============================================
    // 🧪 독립 테스트들
    // ==============================================
    @Test
    @DisplayName("🏆 엠블럼 API 독립 테스트")
    void emblem_only_test() throws Exception {
        mockMvc.perform(get(PATH_EMBLEM_SUMMARY)).andExpect(status().isOk()).andDo(print());
        mockMvc.perform(get(PATH_EMBLEM_CATALOG).param("filter", "OWNED").param("size", "5"))
                .andExpect(status().isOk()).andDo(print());
        MvcResult scanAllResult = mockMvc.perform(post(PATH_EMBLEM_SCAN_AWARD).param("scope", "ALL"))
                .andExpect(status().isOk()).andDo(print()).andReturn();
        String scanJson = scanAllResult.getResponse().getContentAsString();
        if (!scanJson.isEmpty()) {
            int awardedCount = objectMapper.readTree(scanJson).path("awarded_count").asInt();
            Assertions.assertTrue(awardedCount >= 0);
        }
    }

    @Test
    @DisplayName("📱 피드 API 독립 테스트")
    void feed_only_test() throws Exception {
        MvcResult createResult = mockMvc.perform(
                post(PATH_FEED_CREATE).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "테스트 피드", "imageUrl", "https://example.com/test.jpg")))
        ).andExpect(status().isOk()).andDo(print()).andReturn();
        Long feedId = objectMapper.readTree(createResult.getResponse().getContentAsString()).path("id").asLong();
        mockMvc.perform(get(PATH_FEED_LIST).param("offset", "0").param("limit", "3"))
                .andExpect(status().isOk()).andDo(print());
        if (feedId != 0) mockMvc.perform(post(PATH_FEED_LIKE, feedId)).andExpect(status().isOk()).andDo(print());
    }

    @Test
    @DisplayName("📁 파일 업로드 API 독립 테스트")
    void file_upload_test() throws Exception {
        mockMvc.perform(post(PATH_FILE_PRESIGN_PROFILE).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("contentType", "image/png", "size", 512000))))
                .andExpect(status().isOk()).andDo(print());
        mockMvc.perform(post(PATH_FILE_PRESIGN_FEED).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("contentType", "image/jpeg", "size", 3145728))))
                .andExpect(status().isOk()).andDo(print());
    }

    @Test
    @DisplayName("🚨 에러 시나리오 테스트")
    void error_scenarios_test() throws Exception {
        mockMvc.perform(get(PATH_EMBLEM_DETAIL, 99999L)).andDo(print());
        mockMvc.perform(post(PATH_FEED_LIKE, 99999L)).andDo(print());
        mockMvc.perform(post(PATH_FILE_PRESIGN_PROFILE).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("contentType", "image/jpeg", "size", -1))))
                .andDo(print());
    }
}
