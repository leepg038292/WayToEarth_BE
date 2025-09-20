package com.waytoearth.service.Journey;

import com.waytoearth.dto.Journey.request.JourneyStartRequest;
import com.waytoearth.dto.Journey.request.JourneyProgressUpdateRequest;
import com.waytoearth.dto.Journey.response.JourneySummaryResponse;
import com.waytoearth.dto.Journey.response.JourneyProgressResponse;
import com.waytoearth.dto.Journey.response.JourneyCompletionEstimateResponse;
import com.waytoearth.dto.Journey.response.LandmarkSummaryResponse;
import com.waytoearth.entity.Journey.JourneyEntity;
import com.waytoearth.entity.Journey.LandmarkEntity;
import com.waytoearth.entity.Journey.UserJourneyProgressEntity;
import com.waytoearth.entity.User.User;
import com.waytoearth.repository.Journey.JourneyRepository;
import com.waytoearth.repository.Journey.LandmarkRepository;
import com.waytoearth.repository.Journey.StampRepository;
import com.waytoearth.repository.Journey.UserJourneyProgressRepository;
import com.waytoearth.repository.User.UserRepository;
import com.waytoearth.service.running.RunningService;
import com.waytoearth.dto.request.running.RunningStartRequest;
import com.waytoearth.dto.request.running.RunningCompleteRequest;
import com.waytoearth.dto.response.running.RunningStartResponse;
import com.waytoearth.entity.enums.RunningType;
import com.waytoearth.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class JourneyServiceImpl implements JourneyService {

    private final JourneyRepository journeyRepository;
    private final UserJourneyProgressRepository progressRepository;
    private final LandmarkRepository landmarkRepository;
    private final StampRepository stampRepository;
    private final UserRepository userRepository;
    private final RunningService runningService;

    @Override
    public List<JourneySummaryResponse> getActiveJourneys() {
        List<JourneyEntity> journeys = journeyRepository.findByIsActiveTrueOrderByCreatedAtDesc();

        return journeys.stream()
                .map(journey -> {
                    Long landmarkCount = landmarkRepository.countLandmarksByJourneyId(journey.getId());
                    Long completedRunners = progressRepository.countCompletedRunnersByJourneyId(journey.getId());
                    return JourneySummaryResponse.from(journey, landmarkCount.intValue(), completedRunners);
                })
                .toList();
    }

    @Override
    public List<JourneySummaryResponse> getJourneysByCategory(JourneyEntity.Category category) {
        List<JourneyEntity> journeys = journeyRepository.findByIsActiveTrueAndCategoryOrderByCreatedAtDesc(category);

        return journeys.stream()
                .map(journey -> {
                    Long landmarkCount = landmarkRepository.countLandmarksByJourneyId(journey.getId());
                    Long completedRunners = progressRepository.countCompletedRunnersByJourneyId(journey.getId());
                    return JourneySummaryResponse.from(journey, landmarkCount.intValue(), completedRunners);
                })
                .toList();
    }

    @Override
    public JourneyEntity getJourneyById(Long journeyId) {
        return journeyRepository.findActiveJourneyWithLandmarks(journeyId)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 여행을 찾을 수 없습니다: " + journeyId));
    }

    @Override
    @Transactional
    public JourneyProgressResponse startJourney(JourneyStartRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + request.userId()));

        JourneyEntity journey = journeyRepository.findById(request.journeyId())
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없습니다: " + request.journeyId()));

        if (!journey.getIsActive()) {
            throw new IllegalArgumentException("비활성화된 여행입니다: " + request.journeyId());
        }

        // 이미 진행 중인 여행이 있는지 확인
        Optional<UserJourneyProgressEntity> existingProgress =
                progressRepository.findByUserIdAndJourneyId(request.userId(), request.journeyId());

        if (existingProgress.isPresent()) {
            UserJourneyProgressEntity progress = existingProgress.get();
            if (progress.getStatus() == UserJourneyProgressEntity.ProgressStatus.COMPLETED) {
                throw new IllegalArgumentException("이미 완료된 여행입니다.");
            }

            // 기존 진행 상태 반환
            return buildProgressResponse(progress);
        }

        // 새로운 여행 진행 생성
        UserJourneyProgressEntity newProgress = UserJourneyProgressEntity.builder()
                .user(user)
                .journey(journey)
                .build();

        UserJourneyProgressEntity savedProgress = progressRepository.save(newProgress);

        // 러닝 레코드 시작 (Journey Running)
        String sessionId = "journey-" + savedProgress.getId() + "-" + System.currentTimeMillis();
        RunningStartRequest runningRequest = RunningStartRequest.builder()
                .sessionId(sessionId)
                .runningType(RunningType.JOURNEY)
                .build();

        AuthenticatedUser authUser = new AuthenticatedUser(user.getId());
        RunningStartResponse runningResponse = runningService.startRunning(authUser, runningRequest);

        // sessionId 저장
        savedProgress.setSessionId(sessionId);
        savedProgress = progressRepository.save(savedProgress);

        log.info("새로운 여행 시작: userId={}, journeyId={}, progressId={}, sessionId={}",
                request.userId(), request.journeyId(), savedProgress.getId(), sessionId);

        return buildProgressResponse(savedProgress);
    }

    @Override
    @Transactional
    public JourneyProgressResponse updateProgress(Long progressId, JourneyProgressUpdateRequest request) {
        UserJourneyProgressEntity progress = progressRepository.findById(progressId)
                .orElseThrow(() -> new IllegalArgumentException("여행 진행을 찾을 수 없습니다: " + progressId));

        if (progress.getStatus() == UserJourneyProgressEntity.ProgressStatus.COMPLETED) {
            throw new IllegalArgumentException("이미 완료된 여행입니다.");
        }

        // 진행률 업데이트
        progress.updateProgress(request.distanceKm());
        progress.setSessionId(request.sessionId());

        // 러닝 레코드 완료 처리
        if (request.durationSeconds() != null && request.calories() != null) {
            try {
                RunningCompleteRequest runningCompleteRequest = RunningCompleteRequest.builder()
                        .sessionId(request.sessionId())
                        .distanceMeters((int) (request.distanceKm() * 1000)) // km -> meters
                        .durationSeconds(request.durationSeconds())
                        .averagePaceSeconds(request.averagePaceSeconds() != null ? request.averagePaceSeconds() : 0)
                        .calories(request.calories())
                        .build();

                AuthenticatedUser authUser = new AuthenticatedUser(progress.getUser().getId());
                runningService.completeRunning(authUser, runningCompleteRequest);

                log.info("러닝 레코드 완료: sessionId={}, 거리={}km, 시간={}초",
                        request.sessionId(), request.distanceKm(), request.durationSeconds());
            } catch (Exception e) {
                log.warn("러닝 레코드 완료 중 오류 발생: sessionId={}, error={}", request.sessionId(), e.getMessage());
            }
        }

        UserJourneyProgressEntity updatedProgress = progressRepository.save(progress);

        log.info("여행 진행률 업데이트: progressId={}, 추가거리={}km, 총거리={}km, 진행률={}%",
                progressId, request.distanceKm(), updatedProgress.getCurrentDistanceKm(),
                updatedProgress.getProgressPercent());

        return buildProgressResponse(updatedProgress);
    }

    @Override
    public JourneyProgressResponse getProgress(Long progressId) {
        UserJourneyProgressEntity progress = progressRepository.findById(progressId)
                .orElseThrow(() -> new IllegalArgumentException("여행 진행을 찾을 수 없습니다: " + progressId));

        return buildProgressResponse(progress);
    }

    @Override
    public List<JourneyProgressResponse> getUserJourneys(Long userId) {
        List<UserJourneyProgressEntity> progressList = progressRepository.findByUserIdWithJourney(userId);

        return progressList.stream()
                .map(this::buildProgressResponse)
                .toList();
    }

    @Override
    public List<JourneySummaryResponse> searchJourneysByTitle(String keyword) {
        List<JourneyEntity> journeys = journeyRepository.searchByTitle(keyword);

        return journeys.stream()
                .map(journey -> {
                    Long landmarkCount = landmarkRepository.countLandmarksByJourneyId(journey.getId());
                    Long completedRunners = progressRepository.countCompletedRunnersByJourneyId(journey.getId());
                    return JourneySummaryResponse.from(journey, landmarkCount.intValue(), completedRunners);
                })
                .toList();
    }

    @Override
    public JourneyCompletionEstimateResponse calculateCompletionEstimate(Long journeyId, Integer runsPerWeek, Double averageDistancePerRun) {
        JourneyEntity journey = journeyRepository.findById(journeyId)
                .orElseThrow(() -> new IllegalArgumentException("여행을 찾을 수 없습니다: " + journeyId));

        return JourneyCompletionEstimateResponse.calculate(
                journey.getTotalDistanceKm(),
                runsPerWeek,
                averageDistancePerRun
        );
    }

    private JourneyProgressResponse buildProgressResponse(UserJourneyProgressEntity progress) {
        // 다음 랜드마크 조회
        LandmarkSummaryResponse nextLandmark = landmarkRepository
                .findNextLandmarkByDistance(progress.getJourney().getId(), progress.getCurrentDistanceKm())
                .map(LandmarkSummaryResponse::from)
                .orElse(null);

        // 수집된 스탬프 수
        Long collectedStamps = stampRepository.countStampsByProgressId(progress.getId());

        // 총 랜드마크 수
        Long totalLandmarks = landmarkRepository.countLandmarksByJourneyId(progress.getJourney().getId());

        return JourneyProgressResponse.from(
                progress,
                nextLandmark,
                collectedStamps.intValue(),
                totalLandmarks.intValue()
        );
    }
}