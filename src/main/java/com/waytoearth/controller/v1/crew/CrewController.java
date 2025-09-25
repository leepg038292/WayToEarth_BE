package com.waytoearth.controller.v1.crew;

import com.waytoearth.dto.response.common.ApiResponse;
import com.waytoearth.dto.response.common.PagedResponse;
import com.waytoearth.dto.response.crew.CrewRankingDto;
import com.waytoearth.dto.response.crew.CrewMemberRankingDto;
import com.waytoearth.entity.crew.CrewEntity;
import com.waytoearth.security.AuthUser;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.crew.CrewService;
import com.waytoearth.service.crew.CrewStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "크루 관리 API", description = "크루 생성, 조회, 랭킹 관련 API")
@RestController
@RequestMapping("/v1/crew")
@RequiredArgsConstructor
public class CrewController {

    private final CrewService crewService;
    private final CrewStatisticsService crewStatisticsService;

    @Operation(summary = "크루 목록 조회 (지역별 필터링 가능)")
    @GetMapping
    public ResponseEntity<PagedResponse<CrewEntity>> getCrews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String region) {

        Pageable pageable = PageRequest.of(page, size);
        Page<CrewEntity> crews;

        if (region != null && !region.isEmpty()) {
            crews = crewService.findCrewsByRegion(region, pageable);
        } else {
            crews = crewService.findAllActiveCrews(pageable);
        }

        return ResponseEntity.ok(PagedResponse.of(crews));
    }

    @Operation(summary = "크루 상세 정보 조회")
    @GetMapping("/{crewId}")
    public ResponseEntity<ApiResponse<CrewEntity>> getCrewDetail(@PathVariable Long crewId) {
        CrewEntity crew = crewService.getCrewById(crewId);
        return ResponseEntity.ok(ApiResponse.success(crew));
    }

    @Operation(summary = "크루 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<CrewEntity>> createCrew(
            @AuthenticatedUser AuthUser authUser,
            @RequestBody CrewEntity crewRequest) {

        CrewEntity createdCrew = crewService.createCrew(authUser.getId(), crewRequest);
        return ResponseEntity.ok(ApiResponse.success(createdCrew));
    }

    @Operation(summary = "크루 정보 수정")
    @PutMapping("/{crewId}")
    public ResponseEntity<ApiResponse<CrewEntity>> updateCrew(
            @AuthenticatedUser AuthUser authUser,
            @PathVariable Long crewId,
            @RequestBody CrewEntity updateRequest) {

        CrewEntity updatedCrew = crewService.updateCrew(authUser.getId(), crewId, updateRequest);
        return ResponseEntity.ok(ApiResponse.success(updatedCrew));
    }

    @Operation(summary = "크루 삭제")
    @DeleteMapping("/{crewId}")
    public ResponseEntity<ApiResponse<Void>> deleteCrew(
            @AuthenticatedUser AuthUser authUser,
            @PathVariable Long crewId) {

        crewService.deleteCrew(authUser.getId(), crewId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "크루 거리 랭킹 조회 (메인 페이지용)")
    @GetMapping("/ranking/distance")
    public ResponseEntity<ApiResponse<List<CrewRankingDto>>> getCrewDistanceRanking(
            @RequestParam String month,
            @RequestParam(defaultValue = "10") int limit) {

        List<CrewRankingDto> ranking = crewStatisticsService.getCrewRankingByDistance(month, limit);
        return ResponseEntity.ok(ApiResponse.success(ranking));
    }

    @Operation(summary = "크루 내 멤버 거리 랭킹 조회 (크루 페이지용)")
    @GetMapping("/{crewId}/members/ranking")
    public ResponseEntity<ApiResponse<List<CrewMemberRankingDto>>> getMemberRanking(
            @AuthenticatedUser AuthUser authUser,
            @PathVariable Long crewId,
            @RequestParam String month,
            @RequestParam(defaultValue = "10") int limit) {

        // 크루 멤버인지 확인
        crewService.validateCrewMembership(authUser.getId(), crewId);

        List<CrewMemberRankingDto> memberRanking = crewStatisticsService.getMemberRankingInCrew(crewId, month, limit);
        return ResponseEntity.ok(ApiResponse.success(memberRanking));
    }

    @Operation(summary = "크루 월간 MVP 조회")
    @GetMapping("/{crewId}/mvp")
    public ResponseEntity<ApiResponse<CrewMemberRankingDto>> getCrewMvp(
            @AuthenticatedUser AuthUser authUser,
            @PathVariable Long crewId,
            @RequestParam String month) {

        // 크루 멤버인지 확인
        crewService.validateCrewMembership(authUser.getId(), crewId);

        Optional<CrewMemberRankingDto> mvp = crewStatisticsService.getMvpInCrew(crewId, month);
        return ResponseEntity.ok(ApiResponse.success(mvp.orElse(null)));
    }
}