package com.waytoearth.controller.v1.User;

import com.waytoearth.dto.request.user.UserUpdateRequest;
import com.waytoearth.dto.response.common.ApiResponse;
import com.waytoearth.dto.response.user.UserInfoResponse;
import com.waytoearth.dto.response.user.UserSummaryResponse;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.security.AuthUser;
import com.waytoearth.service.user.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "사용자 API", description = "사용자 정보 조회 및 관리 관련 API")
public class UserController {

    private final UserService userService;


    @GetMapping("/check-nickname")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkNicknameDuplicate(
            @Parameter(description = "중복 확인할 닉네임", example = "runner_kim", required = true)
            @RequestParam String nickname
    ) {
        log.info("[UserController] 닉네임 중복 확인 요청 - nickname: {}", nickname);

        boolean isDuplicate = userService.existsByNickname(nickname);
        Map<String, Boolean> data = Map.of("isDuplicate", isDuplicate);
        String message = isDuplicate ? "이미 사용 중인 닉네임입니다." : "사용 가능한 닉네임입니다.";

        log.info("[NicknameCheck] 결과 - nickname: {}, isDuplicate: {}", nickname, isDuplicate);
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    /**
     * 내 정보 전체 조회
     * GET /v1/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> me(@AuthUser AuthenticatedUser me) {
        log.info("[Users:Me] 내 정보 조회 - userId: {}", me.getUserId());
        UserInfoResponse body = userService.getMe(me.getUserId());
        return ResponseEntity.ok(ApiResponse.success(body, "사용자 정보를 성공적으로 조회했습니다."));
    }

    /**
     * 내 정보 요약(총거리/횟수/엠블럼/완성도)
     * GET /v1/users/me/summary
     */
    @GetMapping("/me/summary")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> summary(@AuthUser AuthenticatedUser me) {
        log.info("[Users:Summary] 요약 조회 - userId: {}", me.getUserId());
        UserSummaryResponse body = userService.getSummary(me.getUserId());
        return ResponseEntity.ok(ApiResponse.success(body, "사용자 요약 정보를 성공적으로 조회했습니다."));
    }

    /**
     * 프로필 수정
     * PUT /v1/users/me
     * Body: nickname, profile_image_url, residence, weekly_goal_distance(선택/부분 수정)
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@AuthUser AuthenticatedUser me,
                                                           @Valid @RequestBody UserUpdateRequest request) {
        log.info("[Users:Update] 프로필 수정 요청 - userId: {}, payload: {}", me.getUserId(), request);
        userService.updateProfile(me.getUserId(), request);

        return ResponseEntity.ok(ApiResponse.success("프로필이 성공적으로 수정되었습니다."));
    }
}
