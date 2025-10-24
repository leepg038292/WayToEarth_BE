package com.waytoearth.controller.v1.crew;

import com.waytoearth.dto.request.crew.CrewJoinRequest;
import com.waytoearth.dto.request.crew.JoinRequestProcessRequest;
import com.waytoearth.dto.response.crew.JoinRequestResponse;
import com.waytoearth.entity.crew.CrewJoinRequestEntity;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.crew.CrewJoinService;
import com.waytoearth.service.file.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.waytoearth.security.AuthUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/crews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Crew Join Management", description = "크루 가입 관리 API")
public class CrewJoinController {

    private final CrewJoinService crewJoinService;
    private final FileService fileService;

    @Operation(summary = "크루 가입 신청", description = "특정 크루에 가입 신청을 합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "가입 신청 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (이미 멤버이거나 대기 중인 신청 존재)"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "크루를 찾을 수 없음")
    })
    @PostMapping("/{crewId}/join-requests")
    public ResponseEntity<JoinRequestResponse> requestToJoin(
            @Parameter(description = "크루 ID") @PathVariable Long crewId,
            @AuthUser AuthenticatedUser user,
            @Valid @RequestBody CrewJoinRequest request) {

        log.info("크루 가입 신청 - crewId: {}, userId: {}", crewId, user.getUserId());

        CrewJoinRequestEntity joinRequest = crewJoinService.requestToJoinCrew(
                user, crewId, request.getMessage());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(joinRequest));
    }

    @Operation(summary = "가입 신청 승인", description = "대기 중인 가입 신청을 승인합니다. 크루장만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "승인 성공"),
            @ApiResponse(responseCode = "400", description = "이미 처리된 신청이거나 정원 초과"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (크루장이 아님)"),
            @ApiResponse(responseCode = "404", description = "가입 신청을 찾을 수 없음")
    })
    @PostMapping("/join-requests/{requestId}/approve")
    public ResponseEntity<Void> approveJoinRequest(
            @Parameter(description = "가입 신청 ID") @PathVariable Long requestId,
            @AuthUser AuthenticatedUser user,
            @Valid @RequestBody JoinRequestProcessRequest request) {

        log.info("가입 신청 승인 - requestId: {}, userId: {}", requestId, user.getUserId());

        crewJoinService.approveJoinRequest(user, requestId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "가입 신청 거부", description = "대기 중인 가입 신청을 거부합니다. 크루장만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "거부 성공"),
            @ApiResponse(responseCode = "400", description = "이미 처리된 신청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (크루장이 아님)"),
            @ApiResponse(responseCode = "404", description = "가입 신청을 찾을 수 없음")
    })
    @PostMapping("/join-requests/{requestId}/reject")
    public ResponseEntity<Void> rejectJoinRequest(
            @Parameter(description = "가입 신청 ID") @PathVariable Long requestId,
            @AuthUser AuthenticatedUser user,
            @Valid @RequestBody JoinRequestProcessRequest request) {

        log.info("가입 신청 거부 - requestId: {}, userId: {}", requestId, user.getUserId());

        crewJoinService.rejectJoinRequest(user, requestId, request.getNote());

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "가입 신청 취소", description = "본인이 신청한 가입 신청을 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "취소 성공"),
            @ApiResponse(responseCode = "400", description = "이미 처리된 신청"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "본인이 신청한 것이 아님"),
            @ApiResponse(responseCode = "404", description = "가입 신청을 찾을 수 없음")
    })
    @DeleteMapping("/join-requests/{requestId}")
    public ResponseEntity<Void> cancelJoinRequest(
            @Parameter(description = "가입 신청 ID") @PathVariable Long requestId,
            @AuthUser AuthenticatedUser user) {

        log.info("가입 신청 취소 - requestId: {}, userId: {}", requestId, user.getUserId());

        crewJoinService.cancelJoinRequest(user, requestId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "크루별 가입 신청 목록", description = "특정 크루에 대한 가입 신청 목록을 조회합니다. 크루장만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (크루장이 아님)"),
            @ApiResponse(responseCode = "404", description = "크루를 찾을 수 없음")
    })
    @GetMapping("/{crewId}/join-requests")
    public ResponseEntity<Page<JoinRequestResponse>> getCrewJoinRequests(
            @Parameter(description = "크루 ID") @PathVariable Long crewId,
            @Parameter(description = "신청 상태 필터") @RequestParam(required = false) CrewJoinRequestEntity.JoinRequestStatus status,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @AuthUser AuthenticatedUser user) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));

        Page<CrewJoinRequestEntity> requests = crewJoinService.getCrewJoinRequests(
                user, crewId, status, pageable);

        Page<JoinRequestResponse> response = requests.map(this::toResponse);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 가입 신청 내역", description = "현재 사용자의 모든 가입 신청 내역을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/join-requests/my")
    public ResponseEntity<List<JoinRequestResponse>> getMyJoinRequests(
            @AuthUser AuthenticatedUser user) {

        List<CrewJoinRequestEntity> requests = crewJoinService.getUserJoinRequests(user);

        List<JoinRequestResponse> response = requests.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "가입 신청 상세 조회", description = "특정 가입 신청의 상세 정보를 조회합니다. 본인 또는 크루장만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인 또는 크루장 아님)"),
            @ApiResponse(responseCode = "404", description = "가입 신청을 찾을 수 없음")
    })
    @GetMapping("/join-requests/{requestId}")
    public ResponseEntity<JoinRequestResponse> getJoinRequestDetail(
            @AuthUser AuthenticatedUser user,
            @Parameter(description = "가입 신청 ID") @PathVariable Long requestId) {

        CrewJoinRequestEntity request = crewJoinService.getJoinRequest(requestId);

        // 본인 또는 크루장만 조회 가능
        boolean isOwner = request.getCrew().getOwner().getId().equals(user.getUserId());
        boolean isApplicant = request.getUser().getId().equals(user.getUserId());

        if (!isOwner && !isApplicant) {
            throw new com.waytoearth.exception.UnauthorizedAccessException("본인 또는 크루장만 조회할 수 있습니다.");
        }

        return ResponseEntity.ok(toResponse(request));
    }

    @Operation(summary = "특정 크루에 대한 내 가입 신청 상태", description = "특정 크루에 대한 현재 사용자의 가입 신청 상태를 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공 (신청 내역 있음)"),
            @ApiResponse(responseCode = "204", description = "신청 내역 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/{crewId}/join-requests/my")
    public ResponseEntity<JoinRequestResponse> getMyJoinRequestForCrew(
            @Parameter(description = "크루 ID") @PathVariable Long crewId,
            @AuthUser AuthenticatedUser user) {

        CrewJoinRequestEntity request = crewJoinService.getUserJoinRequestForCrew(user, crewId);

        if (request == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(toResponse(request));
    }

    @Operation(summary = "가입 가능한 크루들", description = "현재 사용자가 가입 가능한 크루 ID 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/joinable")
    public ResponseEntity<List<Long>> getJoinableCrewIds(
            @AuthUser AuthenticatedUser user) {

        List<Long> joinableCrewIds = crewJoinService.getJoinableCrewIds(user);

        return ResponseEntity.ok(joinableCrewIds);
    }

    @Operation(summary = "크루 가입 가능 여부 확인", description = "특정 크루에 가입 가능한지 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "확인 완료")
    })
    @GetMapping("/{crewId}/can-join")
    public ResponseEntity<Boolean> canJoinCrew(
            @Parameter(description = "크루 ID") @PathVariable Long crewId,
            @AuthUser AuthenticatedUser user) {

        boolean canJoin = crewJoinService.canJoinCrew(user, crewId);

        return ResponseEntity.ok(canJoin);
    }

    @Operation(summary = "대기 중인 가입 신청 수", description = "특정 크루의 대기 중인 가입 신청 수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/{crewId}/pending-requests/count")
    public ResponseEntity<Long> getPendingRequestCount(
            @Parameter(description = "크루 ID") @PathVariable Long crewId) {

        long count = crewJoinService.getPendingRequestCount(crewId);

        return ResponseEntity.ok(count);
    }

    // CrewJoinRequestEntity → JoinRequestResponse 변환 (프로필 이미지 URL 포함)
    private JoinRequestResponse toResponse(CrewJoinRequestEntity request) {
        String profileImageUrl = null;
        if (request.getUser().getProfileImageKey() != null && !request.getUser().getProfileImageKey().isEmpty()) {
            profileImageUrl = fileService.createPresignedGetUrl(request.getUser().getProfileImageKey());
        }
        return JoinRequestResponse.from(request, profileImageUrl);
    }
}