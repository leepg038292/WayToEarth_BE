package com.waytoearth.controller.v1.running;

import com.waytoearth.dto.request.running.PaceCoachCheckRequest;
import com.waytoearth.dto.response.common.ApiResponse;
import com.waytoearth.dto.response.running.PaceCoachCheckResponse;
import com.waytoearth.security.AuthUser;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.running.PaceCoachService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "실시간 페이스 코치 API", description = "러닝 중 페이스 체크 및 알림")
@RestController
@RequestMapping("/v1/running/pace-coach")
@RequiredArgsConstructor
public class PaceCoachController {

    private final PaceCoachService paceCoachService;

    @Operation(
            summary = "페이스 코치 체크",
            description = """
                    러닝 중 km를 통과할 때마다 호출하여 페이스를 체크합니다.

                    **동작 방식:**
                    - 설정에서 "AI 페이스 코치"가 OFF면 비활성화 응답
                    - 완료된 러닝 기록이 5회 미만이면 사용 불가 응답
                    - 5회 이상이면 최근 5회 평균 페이스 계산
                    - 현재 페이스와 비교하여 느리면 알림 필요 (shouldAlert: true)
                    - 빠르거나 같으면 알림 불필요 (shouldAlert: false)

                    **호출 시점:**
                    - 프론트엔드가 러닝 중 1km, 2km, 3km... 통과할 때마다 호출

                    **응답 필드:**
                    - `shouldAlert`: true면 팝업 띄우기, false면 무시
                    - `alertMessage`: 팝업에 표시할 메시지
                    """
    )
    @PostMapping("/check")
    public ResponseEntity<ApiResponse<PaceCoachCheckResponse>> checkPace(
            @AuthUser AuthenticatedUser user,
            @Valid @RequestBody PaceCoachCheckRequest request) {

        PaceCoachCheckResponse response = paceCoachService.checkPace(
                user.getUserId(),
                request.getSessionId(),
                request.getCurrentKm(),
                request.getCurrentPaceSeconds()
        );

        return ResponseEntity.ok(
                ApiResponse.success(response, "페이스 코치 체크 완료")
        );
    }
}
