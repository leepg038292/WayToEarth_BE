// com/waytoearth/service/emblem/EmblemService.java
package com.waytoearth.service.emblem;

import com.waytoearth.dto.response.emblem.EmblemAwardResult;
import com.waytoearth.dto.response.emblem.EmblemCatalogItem;
import com.waytoearth.dto.response.emblem.EmblemDetailResponse;
import com.waytoearth.dto.response.emblem.EmblemSummaryResponse;
import com.waytoearth.entity.Emblem;
import com.waytoearth.entity.User;
import com.waytoearth.entity.UserEmblem;
import com.waytoearth.repository.EmblemRepository;
import com.waytoearth.repository.UserEmblemRepository;
import com.waytoearth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmblemService {

    private final EmblemRepository emblemRepository;
    private final UserRepository userRepository;
    private final UserEmblemRepository userEmblemRepository;

    /* =========================
       조회 (요약/카탈로그/상세)
       ========================= */

    public EmblemSummaryResponse summary(Long userId) {
        // 리포에 countByUserId가 없어도 동작하게 리스트 사이즈로 처리
        int owned = userEmblemRepository.findByUserId(userId).size();
        int total = (int) emblemRepository.count();
        double completion = total == 0 ? 0.0 : (owned * 1.0 / total);

        return EmblemSummaryResponse.builder()
                .owned(owned)
                .total(total)
                .completionRate(completion)
                .build();
    }

    /**
     * 카탈로그(필터/커서/사이즈)
     * - 리포에 커서/페이지 쿼리가 없어도 일단 동작하도록 메모리 필터링
     *   (추후 findAllByOrderByIdDesc, findByIdLessThanOrderByIdDesc로 최적화 가능)
     */
    public List<EmblemCatalogItem> catalog(Long userId, String filter, int size, Long cursor) {
        // 전체 엠블럼 로드 후 id desc 정렬
        List<Emblem> all = emblemRepository.findAll();
        all.sort(Comparator.comparing(Emblem::getId).reversed());

        // 커서 적용: id < cursor
        if (cursor != null) {
            all = all.stream().filter(e -> e.getId() < cursor).collect(Collectors.toList());
        }

        // 보유 목록 한 번만 로드
        Set<Long> ownedIds = userEmblemRepository.findByUserId(userId).stream()
                .map(ue -> ue.getEmblem().getId())
                .collect(Collectors.toSet());

        String f = (filter == null ? "ALL" : filter.toUpperCase());
        List<EmblemCatalogItem> mapped = new ArrayList<>();

        for (Emblem e : all) {
            boolean owned = ownedIds.contains(e.getId());
            if ("OWNED".equals(f) && !owned) continue;
            if ("MISSING".equals(f) && owned) continue;

            mapped.add(EmblemCatalogItem.builder()
                    .emblemId(e.getId())
                    .name(e.getName())
                    .description(e.getDescription())
                    .imageUrl(e.getImageUrl())
                    .rarity(e.getRarity())
                    .owned(owned)
                    .earnedAt(null) // 필요하면 UserEmblem에서 조회 확장
                    .build());

            if (mapped.size() >= size) break;
        }
        return mapped;
    }

    public EmblemDetailResponse detail(Long userId, Long emblemId) {
        Emblem e = emblemRepository.findById(emblemId)
                .orElseThrow(() -> new NoSuchElementException("Emblem not found: " + emblemId));

        // 보유여부/획득일 조회 (리포에 전용 메서드 없어도 동작하도록 전체에서 검색)
        Instant earnedAt = null;
        boolean owned = false;
        for (UserEmblem ue : userEmblemRepository.findByUserId(userId)) {
            if (ue.getEmblem().getId().equals(emblemId)) {
                owned = true;
                earnedAt = ue.getAcquiredAt(); // 필드명이 earnedAt/acquiredAt 프로젝트 기준에 맞게
                break;
            }
        }

        return EmblemDetailResponse.builder()
                .emblemId(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .imageUrl(e.getImageUrl())
                .rarity(e.getRarity())
                .conditionType(e.getConditionType())
                .conditionValue(e.getConditionValue())
                .owned(owned)
                .earnedAt(earnedAt)
                .build();
    }

    /* =============
       지급(발급)
       ============= */

    /**
     * 단건 지급 시도 (멱등)
     * - 이미 보유 시 false
     * - 조건 미충족 시 false
     * - 지급 성공 시 true
     */
    @Transactional
    public boolean awardIfEligible(Long userId, Long emblemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
        Emblem emblem = emblemRepository.findById(emblemId)
                .orElseThrow(() -> new NoSuchElementException("Emblem not found: " + emblemId));

        if (userEmblemRepository.existsByUserAndEmblem(user, emblem)) {
            return false; // 이미 보유
        }
        if (!meetsCondition(user, emblem)) {
            return false; // 조건 미충족
        }

        try {
            userEmblemRepository.save(
                    UserEmblem.builder()
                            .user(user)
                            .emblem(emblem)
                            .acquiredAt(Instant.now())
                            .build()
            );
            return true;
        } catch (DataIntegrityViolationException dup) {
            // 동시성 등으로 인한 UNIQUE 충돌 → 멱등 처리
            return false;
        }
    }

    /**
     * 일괄 스캔 지급
     * - scope: DISTANCE | ALL
     * - 리포에 conditionType별 조회가 없어도 ALL 불러와 필터링
     */
    @Transactional
    public EmblemAwardResult scanAndAward(Long userId, String scope) {
        String type = scope == null ? "DISTANCE" : scope.toUpperCase();
        List<Emblem> candidates = emblemRepository.findAll();

        if (!"ALL".equals(type)) {
            candidates = candidates.stream()
                    .filter(e -> type.equalsIgnoreCase(nullToEmpty(e.getConditionType())))
                    .collect(Collectors.toList());
        }

        List<Long> awardedIds = new ArrayList<>();
        for (Emblem e : candidates) {
            if (awardIfEligible(userId, e.getId())) {
                awardedIds.add(e.getId());
            }
        }

        return EmblemAwardResult.builder()
                .awardedCount(awardedIds.size())
                .awardedEmblemIds(awardedIds)
                .build();
    }

    /* ==================
       조건 평가 (간단판)
       ================== */
    private boolean meetsCondition(User user, Emblem emblem) {
        String type = nullToEmpty(emblem.getConditionType()).toUpperCase();
        BigDecimal target = emblem.getConditionValue();

        return switch (type) {
            case "DISTANCE" -> {
                if (user.getTotalDistance() == null || target == null) yield false;
                yield user.getTotalDistance().compareTo(target) >= 0;
            }
            // TODO: WEEKLY_GOAL, COURSE_COMPLETE 등 필요 시 추가
            default -> false;
        };
    }

    private String nullToEmpty(String s) { return s == null ? "" : s; }
}
