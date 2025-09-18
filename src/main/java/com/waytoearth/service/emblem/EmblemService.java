// com/waytoearth/service/emblem/EmblemService.java
package com.waytoearth.service.emblem;

import com.waytoearth.dto.response.emblem.EmblemAwardResult;
import com.waytoearth.dto.response.emblem.EmblemCatalogItem;
import com.waytoearth.dto.response.emblem.EmblemDetailResponse;
import com.waytoearth.dto.response.emblem.EmblemSummaryResponse;
import com.waytoearth.entity.User.User;
import com.waytoearth.entity.emblem.Emblem;
import com.waytoearth.entity.emblem.UserEmblem;
import com.waytoearth.repository.Emblem.EmblemRepository;
import com.waytoearth.repository.Emblem.UserEmblemRepository;
import com.waytoearth.repository.User.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
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
        int owned = (int) userEmblemRepository.countByUserId(userId);
        int total = (int) emblemRepository.count();
        double completion = total == 0 ? 0.0 : (owned * 1.0 / total);

        return EmblemSummaryResponse.builder()
                .owned(owned)
                .total(total)
                .completionRate(completion)
                .build();
    }

    /**
     * 카탈로그(필터/커서/사이즈) - 최적화된 페이징 처리
     */
    public List<EmblemCatalogItem> catalog(Long userId, String filter, int size, Long cursor) {
        List<Emblem> emblems;
        if (cursor != null) {
            emblems = emblemRepository.findByIdLessThanOrderByIdDesc(cursor, Pageable.ofSize(size));
        } else {
            emblems = emblemRepository.findAllByOrderByIdDesc(Pageable.ofSize(size));
        }

        Set<Long> ownedIds = userEmblemRepository.findByUserId(userId).stream()
                .map(ue -> ue.getEmblem().getId())
                .collect(Collectors.toSet());

        String f = (filter == null ? "ALL" : filter.toUpperCase());
        List<EmblemCatalogItem> result = new ArrayList<>();

        for (Emblem e : emblems) {
            boolean owned = ownedIds.contains(e.getId());
            if ("OWNED".equals(f) && !owned) continue;
            if ("MISSING".equals(f) && owned) continue;

            result.add(EmblemCatalogItem.builder()
                    .emblemId(e.getId())
                    .name(e.getName())
                    .description(e.getDescription())
                    .imageUrl(e.getImageUrl())
                    .rarity(e.getRarity())
                    .owned(owned)
                    .earnedAt(null)
                    .build());
        }
        return result;
    }

    public EmblemDetailResponse detail(Long userId, Long emblemId) {
        Emblem e = emblemRepository.findById(emblemId)
                .orElseThrow(() -> new NoSuchElementException("Emblem not found: " + emblemId));

        Optional<UserEmblem> userEmblem = userEmblemRepository.findByUserIdAndEmblemId(userId, emblemId);

        boolean owned = userEmblem.isPresent();
        Instant earnedAt = userEmblem.map(UserEmblem::getAcquiredAt).orElse(null);

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
                            .acquiredAt(Instant.now()) // ✅ null 방지
                            .build()
            );
            return true;
        } catch (DataIntegrityViolationException dup) {
            return false;
        }
    }

    /**
     * 일괄 스캔 지급
     */
    @Transactional
    public EmblemAwardResult scanAndAward(Long userId, String scope) {
        String type = scope == null ? "DISTANCE" : scope.toUpperCase();

        List<Emblem> candidates;
        if ("ALL".equals(type)) {
            candidates = emblemRepository.findAll();
        } else {
            candidates = emblemRepository.findByConditionTypeIgnoreCase(type);
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
       조건 평가
       ================== */
    private boolean meetsCondition(User user, Emblem emblem) {
        String type = nullToEmpty(emblem.getConditionType()).toUpperCase();
        BigDecimal target = emblem.getConditionValue();

        return switch (type) {
            case "DISTANCE" -> {
                if (user.getTotalDistance() == null || target == null) yield false;
                yield user.getTotalDistance().compareTo(target) >= 0;
            }
            default -> false;
        };
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
