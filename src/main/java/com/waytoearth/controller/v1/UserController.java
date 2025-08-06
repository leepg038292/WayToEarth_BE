package com.waytoearth.controller.v1;

import com.waytoearth.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Object>> checkNicknameDuplicate(@RequestParam String nickname) {
        log.info("[NicknameCheck] 닉네임 중복 확인 요청 - nickname: {}", nickname);

        boolean isDuplicate = userService.existsByNickname(nickname);
        Map<String, Object> response = new HashMap<>();
        response.put("isDuplicate", isDuplicate);
        response.put("message", isDuplicate ? "이미 사용 중인 닉네임입니다." : "사용 가능한 닉네임입니다.");

        log.info("[NicknameCheck] 결과 - nickname: {}, isDuplicate: {}", nickname, isDuplicate);

        return ResponseEntity.ok(response);
    }

}
