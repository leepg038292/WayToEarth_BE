// com/waytoearth/controller/v1/EmblemController.java
package com.waytoearth.controller.v1;

import com.waytoearth.dto.response.common.ApiResponse;
import com.waytoearth.dto.response.emblem.EmblemAwardResult;
import com.waytoearth.dto.response.emblem.EmblemCatalogItem;
import com.waytoearth.dto.response.emblem.EmblemDetailResponse;
import com.waytoearth.dto.response.emblem.EmblemSummaryResponse;
import com.waytoearth.security.AuthUser;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.emblem.EmblemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Emblem API", description = "엠블럼 조회/지급 API")
@RestController
@RequestMapping("/v1/emblems")
@RequiredArgsConstructor
public class EmblemController {

    private final EmblemService emblemService;

    /* ===== 조회 ===== */

    @Operation(summary = "내 엠블럼 요약")
    @GetMapping("/me/summary")
    public ResponseEntity<ApiResponse<EmblemSummaryResponse>> summary(@AuthUser AuthenticatedUser me) {
        EmblemSummaryResponse response = emblemService.summary(me.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response, "엠블럼 요약 정보를 성공적으로 조회했습니다."));
    }

    @Operation(summary = "엠블럼 카탈로그(필터/커서 지원)")
    @GetMapping("/catalog")
    public ResponseEntity<ApiResponse<List<EmblemCatalogItem>>> catalog(
            @AuthUser AuthenticatedUser me,
            @RequestParam(defaultValue = "ALL") String filter,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long cursor
    ) {
        List<EmblemCatalogItem> catalogs = emblemService.catalog(me.getUserId(), filter, size, cursor);
        return ResponseEntity.ok(ApiResponse.success(catalogs, "엠블럼 카탈로그를 성공적으로 조회했습니다."));
    }

    @Operation(summary = "엠블럼 상세")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmblemDetailResponse>> detail(@AuthUser AuthenticatedUser me,
                                                                     @PathVariable Long id) {
        EmblemDetailResponse response = emblemService.detail(me.getUserId(), id);
        return ResponseEntity.ok(ApiResponse.success(response, "엠블럼 상세 정보를 성공적으로 조회했습니다."));
    }

    /* ===== 지급 ===== */

    @Operation(summary = "엠블럼 지급 시도(단건)", description = "조건 충족 시 해당 엠블럼을 지급합니다.")
    @PostMapping("/{id}/award")
    public ResponseEntity<ApiResponse<EmblemAwardResult>> awardOne(@AuthUser AuthenticatedUser me,
                                                                   @PathVariable Long id) {
        boolean ok = emblemService.awardIfEligible(me.getUserId(), id);
        EmblemAwardResult result = EmblemAwardResult.builder()
                .awardedCount(ok ? 1 : 0)
                .awardedEmblemIds(ok ? List.of(id) : List.of())
                .build();
        String message = ok ? "엠블럼이 성공적으로 지급되었습니다." : "엠블럼 지급 조건을 만족하지 않습니다.";
        return ResponseEntity.ok(ApiResponse.success(result, message));
    }

    @Operation(summary = "엠블럼 일괄 스캔 지급", description = "scope: DISTANCE | ALL (기본 DISTANCE)")
    @PostMapping("/award/scan")
    public ResponseEntity<ApiResponse<EmblemAwardResult>> scanAndAward(@AuthUser AuthenticatedUser me,
                                                                       @RequestParam(defaultValue = "DISTANCE") String scope) {
        EmblemAwardResult result = emblemService.scanAndAward(me.getUserId(), scope);
        String message = result.getAwardedCount() > 0 
            ? result.getAwardedCount() + "개의 엠블럼이 새로 지급되었습니다." 
            : "새로 지급될 엠블럼이 없습니다.";
        return ResponseEntity.ok(ApiResponse.success(result, message));
    }
}
