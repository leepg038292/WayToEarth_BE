package com.waytoearth.controller.v1.running;

import com.waytoearth.dto.response.common.ApiResponse;
import com.waytoearth.dto.response.running.ai.RunningAnalysisResponse;
import com.waytoearth.security.AuthUser;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.ai.RunningAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "러닝 AI 분석 API", description = "OpenAI를 활용한 러닝 기록 분석 및 피드백 제공")
@RestController
@RequestMapping("/v1/running/analysis")
@RequiredArgsConstructor
public class RunningAnalysisController {

    private final RunningAnalysisService runningAnalysisService;

    @Operation(
            summary = "러닝 기록 AI 분석",
            description = """
                    완료된 러닝 기록을 AI로 분석하여 피드백을 제공합니다.

                    - 이미 분석된 기록은 캐싱된 결과 반환
                    - 미완료 기록은 분석 불가
                    - 거리, 시간, 페이스, 칼로리 데이터 기반 분석
                    - 향후 케이던스, 심박수 데이터 추가 예정
                    """
    )
    @PostMapping("/{runningRecordId}")
    public ResponseEntity<ApiResponse<RunningAnalysisResponse>> analyzeRunning(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long runningRecordId) {

        RunningAnalysisResponse response = runningAnalysisService.analyzeRunning(
                runningRecordId,
                user.getUser()
        );

        return ResponseEntity.ok(
                ApiResponse.success(response, "AI 분석이 완료되었습니다.")
        );
    }

    @Operation(
            summary = "러닝 기록 AI 분석 조회",
            description = "이미 생성된 AI 피드백을 조회합니다. 분석이 없으면 새로 생성합니다."
    )
    @GetMapping("/{runningRecordId}")
    public ResponseEntity<ApiResponse<RunningAnalysisResponse>> getFeedback(
            @AuthUser AuthenticatedUser user,
            @PathVariable Long runningRecordId) {

        RunningAnalysisResponse response = runningAnalysisService.analyzeRunning(
                runningRecordId,
                user.getUser()
        );

        return ResponseEntity.ok(
                ApiResponse.success(response, "AI 피드백을 조회했습니다.")
        );
    }
}
