package com.waytoearth.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException e) {
        log.error("사용자 없음 예외: {}", e.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code("USER_NOT_FOUND")
                        .message(e.getMessage())
                        .build())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(DuplicateResourceException e) {
        log.error("중복 리소스 예외: {}", e.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code("DUPLICATE_NICKNAME")
                        .message(e.getMessage())
                        .build())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException e) {
        log.error("인증 예외: {}", e.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code("UNAUTHORIZED")
                        .message(e.getMessage())
                        .build())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameterException(InvalidParameterException e) {
        log.error("잘못된 파라미터 예외: {}", e.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code("INVALID_PARAMETER")
                        .message(e.getMessage())
                        .build())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.error("검증 실패 예외: {}", e.getMessage());

        Map<String, String> validationErrors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        String details = validationErrors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .reduce((a, b) -> a + ", " + b)
                .orElse("검증 실패");

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code("INVALID_PARAMETER")
                        .message("입력값 검증에 실패했습니다")
                        .details(details)
                        .build())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 기존 IllegalArgumentException도 처리 (기존 코드 호환성)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("잘못된 인수 예외: {}", e.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code("INVALID_PARAMETER")
                        .message(e.getMessage())
                        .build())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(OpenAIServiceException.class)
    public ResponseEntity<ErrorResponse> handleOpenAIServiceException(OpenAIServiceException e) {
        log.error("OpenAI API 호출 예외: {}", e.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code("OPENAI_SERVICE_ERROR")
                        .message("AI 분석 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                        .details(e.getMessage())
                        .build())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccessException(UnauthorizedAccessException e) {
        log.error("권한 없는 접근: {}", e.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code("FORBIDDEN")
                        .message(e.getMessage())
                        .build())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(StoryCardNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStoryCardNotFoundException(StoryCardNotFoundException e) {
        log.error("스토리 카드 없음 예외: {}", e.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code("STORY_CARD_NOT_FOUND")
                        .message(e.getMessage())
                        .build())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(LandmarkNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLandmarkNotFoundException(LandmarkNotFoundException e) {
        log.error("랜드마크 없음 예외: {}", e.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code("LANDMARK_NOT_FOUND")
                        .message(e.getMessage())
                        .build())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(CrewNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCrewNotFoundException(CrewNotFoundException e) {
        log.warn("크루 없음 예외: {}", e.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code("CREW_NOT_FOUND")
                        .message(e.getMessage())
                        .build())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("예상치 못한 서버 오류", e);

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .error(ErrorDetail.builder()
                        .code("INTERNAL_SERVER_ERROR")
                        .message("서버 내부 오류가 발생했습니다")
                        .build())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}