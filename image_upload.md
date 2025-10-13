# 이미지 업로드 시스템 (CloudFront CDN 연동)

## 1. 현재 구조 분석

### CloudFront CDN 적용 (2025-01)
- **문제**: S3 presigned URL 방식으로 5분 후 이미지 만료
- **해결**: CloudFront CDN 연동으로 영구 URL 제공
- **적용 범위**: 운영 환경(prod)만 CloudFront 사용, 개발 환경(dev)은 presigned URL 유지

### 업로드/조회 분리 구조
```
업로드: S3 presigned PUT URL (5분 만료) → 즉시 완료되므로 문제 없음
조회: CloudFront URL (만료 없음) → DB 저장 및 영구 사용
```

### 엔티티 관계
```
JourneyEntity (여행)
  └── LandmarkEntity (랜드마크) - Line 67: imageUrl
      └── StoryCardEntity (스토리카드) - Line 38: imageUrl
```

### 현재 Presign 구조
```java
// 프로필 이미지 (고정 - OK)
profiles/{userId}/profile.{ext}

// 피드 이미지 (개별 특성 - OK)
feeds/{날짜}/{userId}/{UUID}

// 랜드마크 이미지 (문제: 그룹핑 없음)
journeys/landmarks/{날짜}/{userId}/{UUID}

// 스토리 이미지 (문제: 그룹핑 없음)
journeys/stories/{날짜}/{userId}/{UUID}
```

### CloudFront 적용 전 문제점
1. ~~**URL 만료**: 5분 후 이미지 접근 불가 (403 에러)~~ → CloudFront로 해결 ✅
2. **랜드마크**: 한 랜드마크에 여러 이미지 시 관리 어려움
3. **스토리**: 스토리별 그룹핑 없어 개별 관리만 가능
4. **중복 저장**: 동일 이미지 여러 번 저장
5. **삭제 비효율**: 개별 삭제만 가능

## 2. 개선된 구조 설계

### A. 추천 키 구조
```java
// 랜드마크 이미지 (1개 또는 여러개)
journeys/landmarks/{landmarkId}/{sequence}_{timestamp}.{ext}

// 스토리 이미지 (각 스토리당 1개씩)
journeys/landmarks/{landmarkId}/stories/{storyId}/image.{ext}

// 대안: 계층 구조 단순화
journeys/stories/{storyId}/image.{ext}
```

### B. 상세 설계

#### 랜드마크 이미지 (다중 이미지 지원)
```java
public PresignResponse presignLandmark(Long landmarkId, Integer sequence, PresignRequest req) {
    // 시퀀스가 없으면 자동 할당 (다음 번호)
    if (sequence == null) {
        sequence = getNextLandmarkImageSequence(landmarkId);
    }

    String ext = getFileExtension(req.getContentType());
    String key = String.format("journeys/landmarks/%d/%03d_%d.%s",
        landmarkId, sequence, System.currentTimeMillis(), ext);

    return createPresignResponse(key, req.getContentType());
}
```

#### 스토리 이미지 (단일 이미지)
```java
public PresignResponse presignStory(Long storyId, PresignRequest req) {
    String ext = getFileExtension(req.getContentType());
    String key = String.format("journeys/stories/%d/image.%s", storyId, ext);

    // 기존 이미지가 있다면 교체
    deleteExistingStoryImage(storyId);

    return createPresignResponse(key, req.getContentType());
}
```

## 3. 데이터베이스 구조 개선

### A. 랜드마크 이미지 메타데이터 테이블
```sql
CREATE TABLE landmark_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    landmark_id BIGINT NOT NULL,
    s3_key VARCHAR(500) NOT NULL UNIQUE,
    sequence_order INT NOT NULL,
    original_filename VARCHAR(255),
    file_size BIGINT,
    content_type VARCHAR(100),
    is_primary BOOLEAN DEFAULT FALSE, -- 대표 이미지 여부
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (landmark_id) REFERENCES landmarks(id) ON DELETE CASCADE,
    UNIQUE KEY uk_landmark_sequence (landmark_id, sequence_order),
    INDEX idx_landmark_primary (landmark_id, is_primary)
);
```

### B. 기존 엔티티 수정
```java
// LandmarkEntity.java
@Entity
public class LandmarkEntity extends BaseTimeEntity {
    // ... 기존 필드들

    // 단일 imageUrl 대신 이미지 리스트로 변경
    @OneToMany(mappedBy = "landmark", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequenceOrder ASC")
    private List<LandmarkImage> images = new ArrayList<>();

    // 대표 이미지 URL 반환 (호환성)
    public String getImageUrl() {
        return images.stream()
            .filter(LandmarkImage::getIsPrimary)
            .findFirst()
            .or(() -> images.stream().findFirst())
            .map(LandmarkImage::getS3Url)
            .orElse(null);
    }
}

// StoryCardEntity는 단일 이미지 유지 (imageUrl 필드 그대로)
```

## 4. API 설계

### A. 랜드마크 이미지 업로드
```java
// 1. Presign 요청
POST /api/v1/files/presign/landmark/{landmarkId}
{
    "contentType": "image/jpeg",
    "size": 2048000,
    "sequence": 1  // 선택적, 없으면 자동 할당
}

// 2. 업로드 완료 확인
POST /api/v1/files/confirm/landmark/{landmarkId}
{
    "s3Key": "journeys/landmarks/123/001_1632468000000.jpg",
    "isPrimary": true  // 대표 이미지 설정
}

// 3. 이미지 목록 조회
GET /api/v1/landmarks/{landmarkId}/images

// 4. 이미지 삭제
DELETE /api/v1/landmarks/{landmarkId}/images/{sequence}
```

### B. 스토리 이미지 업로드
```java
// 1. Presign 요청 (기존 이미지 자동 교체)
POST /api/v1/files/presign/story/{storyId}
{
    "contentType": "image/jpeg",
    "size": 2048000
}

// 2. 업로드 완료 확인
POST /api/v1/files/confirm/story/{storyId}
{
    "s3Key": "journeys/stories/456/image.jpg"
}
```

## 5. 서비스 레이어 구현

### A. LandmarkImageService
```java
@Service
@RequiredArgsConstructor
public class LandmarkImageService {

    private final FileService fileService;
    private final LandmarkImageRepository landmarkImageRepository;

    public PresignResponse generatePresign(Long landmarkId, PresignRequest request) {
        // 1. 다음 시퀀스 조회
        Integer nextSequence = getNextSequence(landmarkId);

        // 2. S3 키 생성
        String s3Key = generateS3Key(landmarkId, nextSequence);

        // 3. 메타데이터 임시 저장
        createPendingImage(landmarkId, s3Key, nextSequence, request);

        // 4. Presign URL 생성
        return fileService.createPresignedUrls(s3Key, request.getContentType());
    }

    public void confirmUpload(Long landmarkId, String s3Key, boolean isPrimary) {
        LandmarkImage image = landmarkImageRepository.findByS3Key(s3Key)
            .orElseThrow(() -> new RuntimeException("Image not found"));

        // 대표 이미지 설정 시 기존 대표 이미지 해제
        if (isPrimary) {
            landmarkImageRepository.clearPrimaryFlag(landmarkId);
        }

        image.setUploaded(true);
        image.setIsPrimary(isPrimary);
        landmarkImageRepository.save(image);
    }

    public void deleteImage(Long landmarkId, Integer sequence) {
        LandmarkImage image = landmarkImageRepository
            .findByLandmarkIdAndSequenceOrder(landmarkId, sequence)
            .orElseThrow(() -> new RuntimeException("Image not found"));

        // S3에서 삭제
        fileService.deleteObject(image.getS3Key());

        // DB에서 삭제
        landmarkImageRepository.delete(image);

        // 대표 이미지였다면 다른 이미지를 대표로 설정
        if (image.getIsPrimary()) {
            setNextImageAsPrimary(landmarkId);
        }
    }
}
```

### B. StoryImageService
```java
@Service
@RequiredArgsConstructor
public class StoryImageService {

    public PresignResponse generatePresign(Long storyId, PresignRequest request) {
        // 기존 이미지 삭제 (스토리당 1개만 허용)
        deleteExistingImage(storyId);

        String s3Key = String.format("journeys/stories/%d/image.%s",
            storyId, getFileExtension(request.getContentType()));

        return fileService.createPresignedUrls(s3Key, request.getContentType());
    }

    public void confirmUpload(Long storyId, String s3Key) {
        // 스토리 엔티티의 imageUrl 업데이트
        StoryCardEntity story = storyRepository.findById(storyId)
            .orElseThrow(() -> new RuntimeException("Story not found"));

        story.setImageUrl(fileService.createPresignedGetUrl(s3Key));
        storyRepository.save(story);
    }
}
```

## 6. 마이그레이션 전략

### A. 기존 데이터 처리
```java
@Component
public class ImageMigrationService {

    public void migrateLandmarkImages() {
        List<LandmarkEntity> landmarks = landmarkRepository.findAll();

        for (LandmarkEntity landmark : landmarks) {
            if (landmark.getImageUrl() != null) {
                // 기존 URL에서 S3 키 추출
                String oldS3Key = extractS3KeyFromUrl(landmark.getImageUrl());

                // 새로운 키 생성
                String newS3Key = String.format("journeys/landmarks/%d/001_%d.jpg",
                    landmark.getId(), System.currentTimeMillis());

                // S3에서 복사
                s3Client.copyObject(oldS3Key, newS3Key);

                // 메타데이터 생성
                LandmarkImage image = LandmarkImage.builder()
                    .landmark(landmark)
                    .s3Key(newS3Key)
                    .sequenceOrder(1)
                    .isPrimary(true)
                    .build();
                landmarkImageRepository.save(image);

                // 기존 파일 삭제 (검증 후)
                deleteOldFile(oldS3Key);
            }
        }
    }
}
```

### B. 점진적 배포
1. **Phase 1**: 새로운 구조 API 추가 (기존 API 유지)
2. **Phase 2**: 백그라운드 마이그레이션 실행
3. **Phase 3**: 프론트엔드 새 API 적용
4. **Phase 4**: 기존 API 제거

## 7. 구현 우선순위

### 즉시 구현 (1주)
1. **랜드마크 다중 이미지**: `LandmarkImage` 엔티티 추가
2. **API 개선**: landmarkId, storyId 기반 presign
3. **기본 CRUD**: 업로드/조회/삭제

### 단기 구현 (2주)
1. **메타데이터 관리**: 순서, 대표 이미지 설정
2. **일괄 삭제**: 랜드마크/스토리별 이미지 일괄 관리
3. **에러 처리**: 업로드 실패 시 정리

### 중기 구현 (1개월)
1. **중복 방지**: 파일 해시 기반 중복 체크
2. **최적화**: 썸네일 생성, 리사이징
3. **모니터링**: 업로드 성공률, 용량 추적

## 8. 예상 효과

### 개선 효과
1. **관리 효율성**: 랜드마크/스토리별 그룹 관리
2. **사용자 경험**: 다중 이미지 업로드 지원
3. **비용 절감**: 중복 방지로 스토리지 비용 절약
4. **확장성**: 향후 기능 확장 용이

### 호환성
1. **기존 API**: 점진적 마이그레이션으로 호환성 유지
2. **데이터**: 무손실 마이그레이션
3. **프론트엔드**: 기존 코드 점진적 업데이트

이 구조로 개선하면 랜드마크 여러 이미지와 스토리별 그룹핑이 모두 해결됩니다.