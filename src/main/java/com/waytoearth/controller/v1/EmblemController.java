// com/waytoearth/controller/v1/EmblemController.java
package com.waytoearth.controller.v1;

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
    public ResponseEntity<EmblemSummaryResponse> summary(@AuthUser AuthenticatedUser me) {
        return ResponseEntity.ok(emblemService.summary(me.getUserId()));
    }

    @Operation(summary = "엠블럼 카탈로그(필터/커서 지원)")
    @GetMapping("/catalog")
    public ResponseEntity<List<EmblemCatalogItem>> catalog(
            @AuthUser AuthenticatedUser me,
            @RequestParam(defaultValue = "ALL") String filter,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long cursor
    ) {
        return ResponseEntity.ok(emblemService.catalog(me.getUserId(), filter, size, cursor));
    }

    @Operation(summary = "엠블럼 상세")
    @GetMapping("/{id}")
    public ResponseEntity<EmblemDetailResponse> detail(@AuthUser AuthenticatedUser me,
                                                       @PathVariable Long id) {
        return ResponseEntity.ok(emblemService.detail(me.getUserId(), id));
    }

    /* ===== 지급 ===== */

    @Operation(summary = "엠블럼 지급 시도(단건)", description = "조건 충족 시 해당 엠블럼을 지급합니다.")
    @PostMapping("/{id}/award")
    public ResponseEntity<EmblemAwardResult> awardOne(@AuthUser AuthenticatedUser me,
                                                      @PathVariable Long id) {
        boolean ok = emblemService.awardIfEligible(me.getUserId(), id);
        return ResponseEntity.ok(
                EmblemAwardResult.builder()
                        .awardedCount(ok ? 1 : 0)
                        .awardedEmblemIds(ok ? List.of(id) : List.of())
                        .build()
        );
    }

    @Operation(summary = "엠블럼 일괄 스캔 지급", description = "scope: DISTANCE | ALL (기본 DISTANCE)")
    @PostMapping("/award/scan")
    public ResponseEntity<EmblemAwardResult> scanAndAward(@AuthUser AuthenticatedUser me,
                                                          @RequestParam(defaultValue = "DISTANCE") String scope) {
        return ResponseEntity.ok(emblemService.scanAndAward(me.getUserId(), scope));
    }
}
