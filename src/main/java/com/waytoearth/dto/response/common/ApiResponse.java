package com.waytoearth.dto.response.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "공통 API 응답")
public class ApiResponse<T> {

    @Schema(description = "성공 여부", example = "true")
    private final boolean success;

    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    private final String message;

    @Schema(description = "응답 데이터")
    private final T data;

    @Schema(description = "응답 시간", example = "2024-01-01T12:00:00Z")
    private final String timestamp;

    @Schema(description = "에러 코드 (실패시에만)", example = "USER_NOT_FOUND")
    private final String errorCode;

    // 성공 응답 (데이터 + 메시지)
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, Instant.now().toString(), null);
    }

    // 성공 응답 (데이터만)
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "요청이 성공적으로 처리되었습니다.");
    }

    // 성공 응답 (메시지만)
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, Instant.now().toString(), null);
    }

    // 성공 응답 (기본)
    public static <T> ApiResponse<T> success() {
        return success("요청이 성공적으로 처리되었습니다.");
    }

    // 실패 응답 (에러코드 + 메시지)
    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return new ApiResponse<>(false, message, null, Instant.now().toString(), errorCode);
    }

    // 실패 응답 (메시지만)
    public static <T> ApiResponse<T> error(String message) {
        return error("INTERNAL_ERROR", message);
    }
}