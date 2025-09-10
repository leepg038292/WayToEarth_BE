package com.waytoearth.service.VirtualRunning;

import com.waytoearth.repository.VirtualRunning.ProgressUpdateLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressUpdateLogCleanupService {

    private final ProgressUpdateLogRepository progressUpdateLogRepository;

    /**
     * 매일 새벽 2시에 1일 이상 된 중복 방지 로그 삭제
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOldLogs() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(1);
        
        try {
            progressUpdateLogRepository.deleteByCreatedAtBefore(cutoff);
            log.info("오래된 진행률 업데이트 로그 정리 완료 - 기준: {}", cutoff);
        } catch (Exception e) {
            log.error("진행률 업데이트 로그 정리 중 오류 발생", e);
        }
    }
}
