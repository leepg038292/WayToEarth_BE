package com.waytoearth.repository.emblem;

import com.waytoearth.entity.emblem.Emblem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmblemRepository extends JpaRepository<Emblem, Long> {
    List<Emblem> findAllByOrderByIdDesc(Pageable pageable);
    List<Emblem> findByIdLessThanOrderByIdDesc(Long cursor, Pageable pageable);

    // 지급 스캔용: 조건 타입별 조회
    List<Emblem> findByConditionTypeIgnoreCase(String conditionType);
}
