package com.waytoearth.controller.v1.crew;

import com.waytoearth.dto.request.crew.MemberRoleChangeRequest;
import com.waytoearth.dto.request.crew.OwnershipTransferRequest;
import com.waytoearth.dto.response.crew.CrewMemberResponse;
import com.waytoearth.entity.crew.CrewMemberEntity;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.crew.CrewMemberService;
import com.waytoearth.service.file.FileService;
import com.waytoearth.repository.running.RunningRecordRepository;
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
import org.springframework.http.ResponseEntity;
import com.waytoearth.security.AuthUser;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/crews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Crew Member Management", description = "크루 멤버 관리 API")
public class CrewMemberController {

    private final CrewMemberService crewMemberService;
    private final FileService fileService;
    private final RunningRecordRepository runningRecordRepository;

    /**
     * 여러 멤버의 최근 러닝 날짜를 배치로 조회 (N+1 문제 해결)
     * @param members 멤버 리스트
     * @return userId -> lastRunningDate 맵
     */
    private Map<Long, LocalDateTime> fetchLastRunningDatesInBatch(List<CrewMemberEntity> members) {
        if (members.isEmpty()) {
            return new HashMap<>();
        }

        // 1. userId 리스트 추출
        List<Long> userIds = members.stream()
                .map(member -> member.getUser().getId())
                .distinct()
                .collect(Collectors.toList());

        // 2. 배치 조회 (1번의 쿼리)
        List<Object[]> results = runningRecordRepository.findLatestRunningDatesByUserIds(userIds);

        // 3. Map으로 변환 (userId -> lastRunningDate)
        Map<Long, LocalDateTime> lastRunningDatesMap = new HashMap<>();
        for (Object[] result : results) {
            Long userId = (Long) result[0];
            LocalDateTime lastRunningDate = (LocalDateTime) result[1];
            lastRunningDatesMap.put(userId, lastRunningDate);
        }

        return lastRunningDatesMap;
    }

    @Operation(summary = "크루 멤버 목록 조회", description = "특정 크루의 멤버 목록을 페이징하여 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (크루 멤버가 아님)"),
            @ApiResponse(responseCode = "404", description = "크루를 찾을 수 없음")
    })
    @GetMapping("/{crewId}/members")
    public ResponseEntity<Page<CrewMemberResponse>> getCrewMembers(
            @AuthUser AuthenticatedUser user,
            @Parameter(description = "크루 ID") @PathVariable Long crewId,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {

        // 크루 멤버만 조회 가능
        if (!crewMemberService.isCrewMember(crewId, user.getUserId())) {
            throw new com.waytoearth.exception.UnauthorizedAccessException("크루 멤버만 조회할 수 있습니다.");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "joinedAt"));

        Page<CrewMemberEntity> members = crewMemberService.getCrewMembers(crewId, pageable);

        // 배치 조회로 N+1 문제 해결
        List<CrewMemberEntity> memberList = members.getContent();
        Map<Long, LocalDateTime> lastRunningDatesMap = fetchLastRunningDatesInBatch(memberList);

        Page<CrewMemberResponse> response = members.map(member -> {
            LocalDateTime lastRunningDate = lastRunningDatesMap.get(member.getUser().getId());
            return CrewMemberResponse.from(member, fileService, lastRunningDate);
        });

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "크루 멤버 목록 조회 (전체)", description = "특정 크루의 모든 멤버를 조회합니다. 사용자 정보 포함.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (크루 멤버가 아님)"),
            @ApiResponse(responseCode = "404", description = "크루를 찾을 수 없음")
    })
    @GetMapping("/{crewId}/members/all")
    public ResponseEntity<List<CrewMemberResponse>> getAllCrewMembers(
            @AuthUser AuthenticatedUser user,
            @Parameter(description = "크루 ID") @PathVariable Long crewId) {

        // 크루 멤버만 조회 가능
        if (!crewMemberService.isCrewMember(crewId, user.getUserId())) {
            throw new com.waytoearth.exception.UnauthorizedAccessException("크루 멤버만 조회할 수 있습니다.");
        }

        List<CrewMemberEntity> members = crewMemberService.getCrewMembersWithUser(crewId);

        // 배치 조회로 N+1 문제 해결
        Map<Long, LocalDateTime> lastRunningDatesMap = fetchLastRunningDatesInBatch(members);

        List<CrewMemberResponse> response = members.stream()
                .map(member -> {
                    LocalDateTime lastRunningDate = lastRunningDatesMap.get(member.getUser().getId());
                    return CrewMemberResponse.from(member, fileService, lastRunningDate);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "크루 멤버 추방", description = "크루에서 특정 멤버를 추방합니다. 크루장만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추방 성공"),
            @ApiResponse(responseCode = "400", description = "자기 자신을 추방하려고 시도"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (크루장이 아님)"),
            @ApiResponse(responseCode = "404", description = "크루 또는 멤버를 찾을 수 없음")
    })
    @DeleteMapping("/{crewId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @Parameter(description = "크루 ID") @PathVariable Long crewId,
            @Parameter(description = "추방할 사용자 ID") @PathVariable Long userId,
            @AuthUser AuthenticatedUser user) {

        log.info("크루 멤버 추방 - crewId: {}, targetUserId: {}, requestedBy: {}",
                crewId, userId, user.getUserId());

        crewMemberService.removeMemberFromCrew(user, crewId, userId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "크루 탈퇴", description = "현재 사용자가 크루에서 탈퇴합니다. 크루장은 탈퇴할 수 없습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "탈퇴 성공"),
            @ApiResponse(responseCode = "400", description = "크루장은 탈퇴할 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "크루 또는 멤버십을 찾을 수 없음")
    })
    @DeleteMapping("/{crewId}/members/leave")
    public ResponseEntity<Void> leaveCrew(
            @Parameter(description = "크루 ID") @PathVariable Long crewId,
            @AuthUser AuthenticatedUser user) {

        log.info("크루 탈퇴 - crewId: {}, userId: {}", crewId, user.getUserId());

        crewMemberService.leaveCrew(user, crewId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "크루 멤버 역할 변경", description = "크루 멤버의 역할을 변경합니다. 크루장만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "역할 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 역할 또는 자기 자신의 역할 변경 시도"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (크루장이 아님)"),
            @ApiResponse(responseCode = "404", description = "크루 또는 멤버를 찾을 수 없음")
    })
    @PatchMapping("/{crewId}/members/{userId}/role")
    public ResponseEntity<CrewMemberResponse> changeMemberRole(
            @Parameter(description = "크루 ID") @PathVariable Long crewId,
            @Parameter(description = "대상 사용자 ID") @PathVariable Long userId,
            @AuthUser AuthenticatedUser user,
            @Valid @RequestBody MemberRoleChangeRequest request) {

        log.info("크루 멤버 역할 변경 - crewId: {}, targetUserId: {}, newRole: {}, requestedBy: {}",
                crewId, userId, request.getNewRole(), user.getUserId());

        CrewMemberEntity member = crewMemberService.changeMemberRole(
                user, crewId, userId, request.getNewRole());

        // 단일 조회는 그대로 사용
        var lastRunningDate = runningRecordRepository
                .findLatestRunningDateByUserId(member.getUser().getId())
                .orElse(null);

        return ResponseEntity.ok(CrewMemberResponse.from(member, fileService, lastRunningDate));
    }

    @Operation(summary = "내가 속한 크루 목록", description = "현재 사용자가 속한 모든 크루의 멤버십 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/memberships/my")
    public ResponseEntity<List<CrewMemberResponse>> getMyCrewMemberships(
            @AuthUser AuthenticatedUser user) {

        List<CrewMemberEntity> memberships = crewMemberService.getUserCrewMemberships(user);

        // 배치 조회로 N+1 문제 해결
        Map<Long, LocalDateTime> lastRunningDatesMap = fetchLastRunningDatesInBatch(memberships);

        List<CrewMemberResponse> response = memberships.stream()
                .map(member -> {
                    LocalDateTime lastRunningDate = lastRunningDatesMap.get(member.getUser().getId());
                    return CrewMemberResponse.from(member, fileService, lastRunningDate);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 크루 멤버십 조회", description = "현재 사용자의 크루 멤버십 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "멤버십을 찾을 수 없음")
    })
    @GetMapping("/{crewId}/members/me")
    public ResponseEntity<CrewMemberResponse> getMyCrewMembership(
            @Parameter(description = "크루 ID") @PathVariable Long crewId,
            @AuthUser AuthenticatedUser user) {

        CrewMemberEntity membership = crewMemberService.getCrewMembership(crewId, user.getUserId());

        // 단일 조회는 그대로 사용
        var lastRunningDate = runningRecordRepository
                .findLatestRunningDateByUserId(membership.getUser().getId())
                .orElse(null);

        return ResponseEntity.ok(CrewMemberResponse.from(membership, fileService, lastRunningDate));
    }

    @Operation(summary = "크루 멤버 수 조회", description = "특정 크루의 활성 멤버 수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "크루를 찾을 수 없음")
    })
    @GetMapping("/{crewId}/members/count")
    public ResponseEntity<Long> getCrewMemberCount(
            @Parameter(description = "크루 ID") @PathVariable Long crewId) {

        long count = crewMemberService.getActiveCrewMemberCount(crewId);

        return ResponseEntity.ok(count);
    }

    @Operation(summary = "크루장 권한 이양", description = "현재 크루장이 다른 멤버에게 크루장 권한을 이양합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "권한 이양 성공"),
            @ApiResponse(responseCode = "400", description = "자기 자신에게 이양하려고 시도하거나 대상이 멤버가 아님"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (크루장이 아님)"),
            @ApiResponse(responseCode = "404", description = "크루 또는 대상 사용자를 찾을 수 없음")
    })
    @PostMapping("/{crewId}/transfer-ownership")
    public ResponseEntity<Void> transferOwnership(
            @Parameter(description = "크루 ID") @PathVariable Long crewId,
            @AuthUser AuthenticatedUser user,
            @Valid @RequestBody OwnershipTransferRequest request) {

        log.info("크루장 권한 이양 - crewId: {}, fromUserId: {}, toUserId: {}",
                crewId, user.getUserId(), request.getNewOwnerId());

        crewMemberService.transferOwnership(user, crewId, request.getNewOwnerId());

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "크루 일반 멤버 목록", description = "크루의 일반 멤버들만 조회합니다 (크루장 제외).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (크루 멤버가 아님)"),
            @ApiResponse(responseCode = "404", description = "크루를 찾을 수 없음")
    })
    @GetMapping("/{crewId}/members/regular")
    public ResponseEntity<List<CrewMemberResponse>> getRegularMembers(
            @AuthUser AuthenticatedUser user,
            @Parameter(description = "크루 ID") @PathVariable Long crewId) {

        // 크루 멤버만 조회 가능
        if (!crewMemberService.isCrewMember(crewId, user.getUserId())) {
            throw new com.waytoearth.exception.UnauthorizedAccessException("크루 멤버만 조회할 수 있습니다.");
        }

        List<CrewMemberEntity> members = crewMemberService.getRegularMembers(crewId);

        // 배치 조회로 N+1 문제 해결
        Map<Long, LocalDateTime> lastRunningDatesMap = fetchLastRunningDatesInBatch(members);

        List<CrewMemberResponse> response = members.stream()
                .map(member -> {
                    LocalDateTime lastRunningDate = lastRunningDatesMap.get(member.getUser().getId());
                    return CrewMemberResponse.from(member, fileService, lastRunningDate);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}