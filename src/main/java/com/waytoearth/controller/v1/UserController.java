package com.waytoearth.controller.v1;

import com.waytoearth.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "닉네임 중복 확인",
            description = "사용자가 입력한 닉네임이 이미 존재하는지 확인합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "닉네임 중복 여부 반환",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Map.class),
                                    examples = {
                                            @ExampleObject(name = "중복인 경우", value = "{\"isDuplicate\": true, \"message\": \"이미 사용 중인 닉네임입니다.\"}"),
                                            @ExampleObject(name = "중복이 아닌 경우", value = "{\"isDuplicate\": false, \"message\": \"사용 가능한 닉네임입니다.\"}")
                                    }
                            )
                    )
            }
    )
    @GetMapping("/check-nickname")
    public ResponseEntity<Map<String, Object>> checkNicknameDuplicate(
            @Parameter(description = "중복 확인할 닉네임", example = "runner_kim")
            @RequestParam String nickname
    ) {
        log.info("[NicknameCheck] 닉네임 중복 확인 요청 - nickname: {}", nickname);

        boolean isDuplicate = userService.existsByNickname(nickname);
        Map<String, Object> response = new HashMap<>();
        response.put("isDuplicate", isDuplicate);
        response.put("message", isDuplicate ? "이미 사용 중인 닉네임입니다." : "사용 가능한 닉네임입니다.");

        log.info("[NicknameCheck] 결과 - nickname: {}, isDuplicate: {}", nickname, isDuplicate);

        return ResponseEntity.ok(response);
    }

}
