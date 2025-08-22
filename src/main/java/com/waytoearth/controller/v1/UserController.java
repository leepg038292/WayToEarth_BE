package com.waytoearth.controller.v1;

import com.waytoearth.dto.request.user.UserUpdateRequest;
import com.waytoearth.dto.response.user.UserInfoResponse;
import com.waytoearth.dto.response.user.UserSummaryResponse;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.security.AuthUser;
import com.waytoearth.service.user.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;


import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "사용자 API", description = "사용자 정보 조회 및 관리 관련 API")
public class UserController {

    private final UserService userService;


    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Object>> checkNicknameDuplicate(
            @Parameter(description = "중복 확인할 닉네임", example = "runner_kim", required = true)
            @RequestParam String nickname
    ) {
        log.info("[UserController] 닉네임 중복 확인 요청 - nickname: {}", nickname);

        boolean isDuplicate = userService.existsByNickname(nickname);
        Map<String, Object> response = new HashMap<>();
        response.put("isDuplicate", isDuplicate);
        response.put("message", isDuplicate ? "이미 사용 중인 닉네임입니다." : "사용 가능한 닉네임입니다.");

log.info("[NicknameCheck] 결과 - nickname: {}, isDuplicate: {}", nickname, isDuplicate);
        return ResponseEntity.ok(response);
    }

    /**
     * 내 정보 전체 조회
     * GET /v1/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> me(@AuthUser AuthenticatedUser me) {
        log.info("[Users:Me] 내 정보 조회 - userId: {}", me.getUserId());
        UserInfoResponse body = userService.getMe(me.getUserId());
        return ResponseEntity.ok(body);
    }

    /**
     * 내 정보 요약(총거리/횟수/엠블럼/완성도)
     * GET /v1/users/me/summary
     */
    @GetMapping("/me/summary")
    public ResponseEntity<UserSummaryResponse> summary(@AuthUser AuthenticatedUser me) {
        log.info("[Users:Summary] 요약 조회 - userId: {}", me.getUserId());
        UserSummaryResponse body = userService.getSummary(me.getUserId());
        return ResponseEntity.ok(body);
    }

    /**
     * 프로필 수정
     * PUT /v1/users/me
     * Body: nickname, profile_image_url, residence, weekly_goal_distance(선택/부분 수정)
     */
    @PutMapping("/me")
    public ResponseEntity<Map<String, Object>> updateProfile(@AuthUser AuthenticatedUser me,
                                                             @Valid @RequestBody UserUpdateRequest request) {
        log.info("[Users:Update] 프로필 수정 요청 - userId: {}, payload: {}", me.getUserId(), request);
        userService.updateProfile(me.getUserId(), request);

        Map<String, Object> res = new HashMap<>();
        res.put("message", "프로필이 수정되었습니다.");
        res.put("updated_at", Instant.now().toString());
        return ResponseEntity.ok(res);
    }
}
