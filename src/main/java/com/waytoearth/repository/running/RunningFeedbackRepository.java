package com.waytoearth.repository.running;

import com.waytoearth.entity.running.RunningFeedback;
import com.waytoearth.entity.running.RunningRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RunningFeedbackRepository extends JpaRepository<RunningFeedback, Long> {

    /** 러닝 기록으로 피드백 조회 */
    Optional<RunningFeedback> findByRunningRecord(RunningRecord runningRecord);

    /** 러닝 기록 ID로 피드백 조회 */
    Optional<RunningFeedback> findByRunningRecordId(Long runningRecordId);

    /** 러닝 기록에 피드백 존재 여부 확인 */
    boolean existsByRunningRecord(RunningRecord runningRecord);
}
