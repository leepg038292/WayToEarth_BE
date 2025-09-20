package com.waytoearth.service.journey;

import com.waytoearth.dto.request.journey.StampCollectRequest;
import com.waytoearth.dto.response.journey.StampResponse;
import com.waytoearth.entity.journey.LandmarkEntity;
import com.waytoearth.entity.journey.StampEntity;
import com.waytoearth.entity.journey.UserJourneyProgressEntity;
import com.waytoearth.repository.journey.LandmarkRepository;
import com.waytoearth.repository.journey.StampRepository;
import com.waytoearth.repository.journey.UserJourneyProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StampServiceImpl implements StampService {

    private final StampRepository stampRepository;
    private final UserJourneyProgressRepository progressRepository;
    private final LandmarkRepository landmarkRepository;

    private static final double COLLECTION_RADIUS_KM = 0.5; // 500m 반경 내에서 수집 가능

    @Override
    @Transactional
    public StampResponse collectStamp(StampCollectRequest request) {
        UserJourneyProgressEntity progress = progressRepository.findById(request.progressId())
                .orElseThrow(() -> new IllegalArgumentException("여행 진행을 찾을 수 없습니다: " + request.progressId()));

        LandmarkEntity landmark = landmarkRepository.findById(request.landmarkId())
                .orElseThrow(() -> new IllegalArgumentException("랜드마크를 찾을 수 없습니다: " + request.landmarkId()));

        // 이미 수집한 스탬프인지 확인
        if (stampRepository.findByUserJourneyProgressIdAndLandmarkId(request.progressId(), request.landmarkId()).isPresent()) {
            throw new IllegalArgumentException("이미 수집한 스탬프입니다.");
        }

        // 수집 가능 거리 확인
        if (!canCollectStamp(request.progressId(), request.landmarkId(),
                request.collectionLocation().latitude(), request.collectionLocation().longitude())) {
            throw new IllegalArgumentException("랜드마크 근처에 있어야 스탬프를 수집할 수 있습니다.");
        }

        // 진행률 확인 (랜드마크에 도달했는지)
        if (progress.getCurrentDistanceKm() < landmark.getDistanceFromStart()) {
            throw new IllegalArgumentException("아직 이 랜드마크에 도달하지 않았습니다.");
        }

        // 스탬프 생성
        StampEntity stamp = StampEntity.builder()
                .userJourneyProgress(progress)
                .landmark(landmark)
                .stampImageUrl(generateStampImageUrl(landmark))
                .build();

        StampEntity savedStamp = stampRepository.save(stamp);

        log.info("스탬프 수집 완료: userId={}, landmarkId={}",
                progress.getUser().getId(), landmark.getId());

        return StampResponse.from(savedStamp);
    }

    @Override
    public List<StampResponse> getStampsByUserId(Long userId) {
        List<StampEntity> stamps = stampRepository.findByUserIdWithLandmark(userId);

        return stamps.stream()
                .map(StampResponse::from)
                .toList();
    }

    @Override
    public List<StampResponse> getStampsByProgressId(Long progressId) {
        List<StampEntity> stamps = stampRepository.findByUserJourneyProgressIdWithLandmark(progressId);

        return stamps.stream()
                .map(StampResponse::from)
                .toList();
    }


    @Override
    public boolean canCollectStamp(Long progressId, Long landmarkId, Double userLatitude, Double userLongitude) {
        LandmarkEntity landmark = landmarkRepository.findById(landmarkId)
                .orElseThrow(() -> new IllegalArgumentException("랜드마크를 찾을 수 없습니다: " + landmarkId));

        double distance = calculateDistance(
                userLatitude, userLongitude,
                landmark.getLatitude(), landmark.getLongitude()
        );

        return distance <= COLLECTION_RADIUS_KM;
    }

    @Override
    public StampStatistics getStampStatistics(Long userId) {
        Long totalStamps = stampRepository.countStampsByUserId(userId);
        Long completedJourneys = progressRepository.countCompletedJourneysByUserId(userId);

        return new StampStatistics(totalStamps, completedJourneys);
    }


    /**
     * 스탬프 이미지 URL 생성
     */
    private String generateStampImageUrl(LandmarkEntity landmark) {
        // 실제로는 랜드마크에 따른 고유한 스탬프 이미지를 반환
        return String.format("https://waytoearth.com/stamps/landmark_%d.png", landmark.getId());
    }

    /**
     * 두 지점 간의 거리 계산 (Haversine formula)
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 지구 반지름 (km)

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}