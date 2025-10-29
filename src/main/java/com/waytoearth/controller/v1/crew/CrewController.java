package com.waytoearth.controller.v1.crew;

import com.waytoearth.dto.request.crew.CrewCreateRequest;
import com.waytoearth.dto.request.crew.CrewUpdateRequest;
import com.waytoearth.dto.response.crew.CrewDetailResponse;
import com.waytoearth.dto.response.crew.CrewListResponse;
import com.waytoearth.entity.crew.CrewEntity;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.crew.CrewJoinService;
import com.waytoearth.service.crew.CrewService;
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

@RestController
@RequestMapping("/v1/crews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Crew Management", description = "크루 기본 관리 API")
public class CrewController {

    private final CrewService crewService;
    private final CrewJoinService crewJoinService;
    private final FileService fileService;

    @Operation(summary = "크루 생성", description = "새로운 크루를 생성합니다. 생성자가 자동으로 크루장이 됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "크루 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping
    public ResponseEntity<CrewDetailResponse> createCrew(
            @AuthUser AuthenticatedUser user,
            @Valid @RequestBody CrewCreateRequest request) {

        log.info("크루 생성 요청 - userId: {}, name: {}", user.getUserId(), request.getName());

        CrewEntity crew = crewService.createCrew(
                user,
                request.getName(),
                request.getDescription(),
                request.getMaxMembers(),
                request.getProfileImageUrl()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CrewDetailResponse.from(crew, fileService));
    }

    @Operation(summary = "크루 상세 조회", description = "특정 크루의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "크루를 찾을 수 없음")
    })
    @GetMapping("/{crewId}")
    public ResponseEntity<CrewDetailResponse> getCrewDetail(
            @Parameter(description = "크루 ID") @PathVariable Long crewId) {

        CrewEntity crew = crewService.getCrewById(crewId);
        return ResponseEntity.ok(CrewDetailResponse.from(crew, fileService));
    }

    @Operation(summary = "크루 목록 조회", description = "활성화된 모든 크루 목록을 페이징하여 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<Page<CrewListResponse>> getCrews(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "desc") String direction,
            @AuthUser AuthenticatedUser user) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<CrewEntity> crews = crewService.findAllActiveCrews(pageable);

        Page<CrewListResponse> response = crews.map(crew -> {
            Boolean canJoin = null;
            if (user != null) {
                canJoin = crewJoinService.canJoinCrew(user, crew.getId());
            }
            return CrewListResponse.from(crew, canJoin, fileService);
        });

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "크루 검색", description = "크루 이름으로 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<CrewListResponse>> searchCrews(
            @Parameter(description = "검색 키워드") @RequestParam String keyword,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @AuthUser AuthenticatedUser user) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CrewEntity> crews = crewService.searchCrewsByName(keyword, pageable);

        Page<CrewListResponse> response = crews.map(crew -> {
            Boolean canJoin = null;
            if (user != null) {
                canJoin = crewJoinService.canJoinCrew(user, crew.getId());
            }
            return CrewListResponse.from(crew, canJoin, fileService);
        });

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내가 속한 크루 목록", description = "현재 사용자가 속한 크루들을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/my")
    public ResponseEntity<Page<CrewListResponse>> getMyCrews(
            @AuthUser AuthenticatedUser user,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CrewEntity> crews = crewService.getUserCrews(user, pageable);

        Page<CrewListResponse> response = crews.map(crew -> CrewListResponse.from(crew, false, fileService));

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "크루 정보 수정", description = "크루 정보를 수정합니다. 크루장만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (크루장이 아님)"),
            @ApiResponse(responseCode = "404", description = "크루를 찾을 수 없음")
    })
    @PutMapping("/{crewId}")
    public ResponseEntity<CrewDetailResponse> updateCrew(
            @Parameter(description = "크루 ID") @PathVariable Long crewId,
            @AuthUser AuthenticatedUser user,
            @Valid @RequestBody CrewUpdateRequest request) {

        log.info("크루 정보 수정 요청 - crewId: {}, userId: {}", crewId, user.getUserId());

        CrewEntity crew = crewService.updateCrew(
                user,
                crewId,
                request.getName(),
                request.getDescription(),
                request.getMaxMembers(),
                request.getProfileImageUrl(),
                request.getProfileImageKey()
        );

        return ResponseEntity.ok(CrewDetailResponse.from(crew, fileService));
    }

    @Operation(summary = "크루 삭제", description = "크루를 삭제합니다. 크루장만 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (크루장이 아님)"),
            @ApiResponse(responseCode = "404", description = "크루를 찾을 수 없음")
    })
    @DeleteMapping("/{crewId}")
    public ResponseEntity<Void> deleteCrew(
            @Parameter(description = "크루 ID") @PathVariable Long crewId,
            @AuthUser AuthenticatedUser user) {

        log.info("크루 삭제 요청 - crewId: {}, userId: {}", crewId, user.getUserId());

        crewService.deleteCrew(user.getUserId(), crewId);

        return ResponseEntity.noContent().build();
    }
}