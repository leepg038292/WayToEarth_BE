#  Virtual Running → Journey Running 시스템 전면 개편

##  개요
기존의 복잡한 세그먼트 기반 가상러닝 시스템을 **스토리텔링 중심의 여행 경험 시스템**으로 전면 개편

###  핵심 변경사항
-  **제거**: 복잡한 세그먼트별 진행률 추적
-  **추가**: 랜드마크 중심의 스토리 카드 시스템
-  **추가**: 스탬프 수집 및 방명록 기능
-  **추가**: 소셜 경험 및 감성적 스토리텔링

---

## 엔티티 구조(변동 사항 있음)

### 1. JourneyEntity (여행 엔티티)
```java
@Entity
@Table(name = "journeys")
public class JourneyEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;              // 여행 제목
    private String description;        // 여행 설명
    private String thumbnailUrl;       // 썸네일 이미지
    private Double totalDistanceKm;    // 총 거리
    private String difficulty;         // 난이도 (EASY, MEDIUM, HARD)
    private String category;           // 카테고리 (DOMESTIC, INTERNATIONAL)
    private Integer estimatedDays;     // 예상 완주 기간
    private Boolean isActive;          // 활성화 상태

    @OneToMany(mappedBy = "journey", cascade = CascadeType.ALL)
    private List<LandmarkEntity> landmarks = new ArrayList<>();
}
```

### 2. LandmarkEntity (랜드마크 엔티티)
```java
@Entity
@Table(name = "landmarks")
public class LandmarkEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_id")
    private JourneyEntity journey;

    private String name;               // 랜드마크 이름
    private String description;        // 설명
    private Double latitude;           // 위도
    private Double longitude;          // 경도
    private Double distanceFromStart;  // 시작점으로부터 거리
    private Integer orderIndex;        // 순서
    private String imageUrl;           // 랜드마크 이미지
    private String countryCode;        // 국가 코드
    private String cityName;           // 도시명

    @OneToMany(mappedBy = "landmark", cascade = CascadeType.ALL)
    private List<StoryCardEntity> storyCards = new ArrayList<>();
}
```

### 3. StoryCardEntity (스토리 카드 엔티티)
```java
@Entity
@Table(name = "story_cards")
public class StoryCardEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landmark_id")
    private LandmarkEntity landmark;

    private String title;              // 스토리 제목
    private String content;            // 스토리 내용
    private String imageUrl;           // 스토리 이미지
    private String audioUrl;           // 오디오 URL (선택)
    private String type;               // HISTORY, CULTURE, NATURE, LOCAL_TIP
    private Integer orderIndex;        // 표시 순서
}
```

### 4. UserJourneyProgressEntity (사용자 여행 진행 엔티티)
```java
@Entity
@Table(name = "user_journey_progress")
public class UserJourneyProgressEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_id")
    private JourneyEntity journey;

    private Double currentDistanceKm;   // 현재 누적 거리
    private Double progressPercent;     // 진행률 (%)
    private String status;              // ACTIVE, COMPLETED, PAUSED
    private LocalDateTime startedAt;    // 시작 시간
    private LocalDateTime completedAt;  // 완료 시간
    private String sessionId;           // 현재 러닝 세션 ID

    @OneToMany(mappedBy = "userJourneyProgress", cascade = CascadeType.ALL)
    private List<StampEntity> collectedStamps = new ArrayList<>();
}
```

### 5. StampEntity (스탬프 엔티티)
```java
@Entity
@Table(name = "stamps")
public class StampEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_journey_progress_id")
    private UserJourneyProgressEntity userJourneyProgress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landmark_id")
    private LandmarkEntity landmark;

    private LocalDateTime collectedAt;  // 수집 시간
    private String stampImageUrl;       // 스탬프 이미지
    private Boolean isSpecial;          // 특별 스탬프 여부
}
```

### 6. GuestbookEntity (방명록 엔티티)
```java
@Entity
@Table(name = "guestbook")
public class GuestbookEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landmark_id")
    private LandmarkEntity landmark;

    private String message;             // 방명록 메시지
    private String photoUrl;            // 사진 URL (선택)
    private String mood;                // 기분 (HAPPY, EXCITED, TIRED, AMAZED)
    private Integer rating;             // 평점 (1-5)
    private Boolean isPublic;           // 공개 여부
}
```

---

##  새로운 API 엔드포인트(변동 사항 있음)

### 1. Journey Management API
```http
# 여행 목록 조회
GET /api/v1/journeys
GET /api/v1/journeys/{journeyId}

# 여행 시작
POST /api/v1/journeys/{journeyId}/start
```

### 2. Journey Progress API
```http
# 진행률 업데이트
PUT /api/v1/journey-progress/{progressId}
Body: {
  "distanceKm": 2.5,
  "sessionId": "uuid",
  "currentLocation": {
    "latitude": 37.5665,
    "longitude": 126.9780
  }
}

# 현재 진행률 조회
GET /api/v1/journey-progress/{progressId}

# 다음 랜드마크 정보 조회
GET /api/v1/journey-progress/{progressId}/next-landmark
```

### 3. Landmark & Story API
```http
# 랜드마크 상세 정보
GET /api/v1/landmarks/{landmarkId}

# 랜드마크의 스토리 카드 목록
GET /api/v1/landmarks/{landmarkId}/stories

# 스토리 카드 상세
GET /api/v1/story-cards/{storyCardId}
```

### 4. Stamp Collection API
```http
# 스탬프 수집
POST /api/v1/stamps/collect
Body: {
  "progressId": 123,
  "landmarkId": 456,
  "collectionLocation": {
    "latitude": 37.5665,
    "longitude": 126.9780
  }
}

# 수집한 스탬프 목록
GET /api/v1/users/{userId}/stamps
GET /api/v1/journey-progress/{progressId}/stamps
```

### 5. Guestbook API
```http
# 방명록 작성
POST /api/v1/guestbook
Body: {
  "landmarkId": 123,
  "message": "정말 아름다운 곳이에요!",
  "photoUrl": "https://...",
  "mood": "AMAZED",
  "rating": 5,
  "isPublic": true
}

# 랜드마크별 방명록 조회
GET /api/v1/landmarks/{landmarkId}/guestbook

# 내 방명록 목록
GET /api/v1/users/{userId}/guestbook
```

---

## 🗑 제거할 기존 클래스 및 파일

### 완전 제거 대상
```
- CourseSegmentEntity.java
- CourseSegmentService.java
- CourseSegmentServiceImpl.java
- CourseSegmentRepository.java
- CourseSegmentController.java
- SegmentProgressEntity.java
- SegmentProgressRepository.java
- ProgressUpdateLog.java
- ProgressUpdateLogRepository.java
- UserVirtualCourseEntity.java (Journey 관련으로 대체)
- VirtualCourseProgressUpdateRequest.java
- SegmentProgressResponse.java
```

### 수정 대상
```
- UserVirtualCourseService.java → UserJourneyService.java
- UserVirtualCourseServiceImpl.java → UserJourneyServiceImpl.java
- UserVirtualCourseRepository.java → UserJourneyProgressRepository.java
- ThemeCourseEntity.java → JourneyEntity.java (통합)
- CustomCourseEntity.java → 제거 (Journey로 통합)
```

---

##  새로운 DTO 클래스 (변동 사항 있음)

### Request DTOs
```java
// 여행 시작 요청
public record JourneyStartRequest(
    Long userId,
    Long journeyId
) {}

// 진행률 업데이트 요청
public record JourneyProgressUpdateRequest(
    String sessionId,
    Double distanceKm,
    LocationPoint currentLocation,
    Integer durationSeconds,
    Integer calories,
    Integer averagePaceSeconds
) {}

// 스탬프 수집 요청
public record StampCollectRequest(
    Long progressId,
    Long landmarkId,
    LocationPoint collectionLocation
) {}

// 방명록 작성 요청
public record GuestbookCreateRequest(
    Long landmarkId,
    String message,
    String photoUrl,
    String mood,
    Integer rating,
    Boolean isPublic
) {}
```

### Response DTOs
```java
// 여행 요약 응답
public record JourneySummaryResponse(
    Long id,
    String title,
    String description,
    String thumbnailUrl,
    Double totalDistanceKm,
    String difficulty,
    String category,
    Integer estimatedDays,
    Integer landmarkCount
) {}

// 여행 진행률 응답
public record JourneyProgressResponse(
    Long progressId,
    Double currentDistanceKm,
    Double progressPercent,
    String status,
    LandmarkSummaryResponse nextLandmark,
    Integer collectedStamps,
    Integer totalLandmarks
) {}

// 랜드마크 상세 응답
public record LandmarkDetailResponse(
    Long id,
    String name,
    String description,
    Double latitude,
    Double longitude,
    Double distanceFromStart,
    String imageUrl,
    String countryCode,
    String cityName,
    List<StoryCardResponse> storyCards,
    Boolean hasStamp
) {}

// 스탬프 응답
public record StampResponse(
    Long id,
    LandmarkSummaryResponse landmark,
    LocalDateTime collectedAt,
    String stampImageUrl,
    Boolean isSpecial
) {}

// 방명록 응답
public record GuestbookResponse(
    Long id,
    UserSummaryResponse user,
    String message,
    String photoUrl,
    String mood,
    Integer rating,
    LocalDateTime createdAt
) {}
```

---

##  Migration 전략

### 1. 데이터베이스 마이그레이션
```sql
-- 새 테이블 생성
CREATE TABLE journeys (...);
CREATE TABLE landmarks (...);
CREATE TABLE story_cards (...);
CREATE TABLE user_journey_progress (...);
CREATE TABLE stamps (...);
CREATE TABLE guestbook (...);

-- 기존 데이터 이관 (가능한 것만)
INSERT INTO journeys SELECT ... FROM theme_courses;

-- 기존 테이블 제거
DROP TABLE course_segments;
DROP TABLE segment_progress;
DROP TABLE progress_update_logs;
```

### 2. 서비스 전환 순서
1. 새로운 엔티티 및 Repository 생성
2. Journey 관련 Service 구현
3. API Controller 구현
4. 기존 Virtual Running 관련 코드 제거
5. 테스트 및 검증

---

##  핵심 기능 명세

### 1. 진행률 시스템
- 거리 기반 진행률 계산
- 랜드마크 도달 감지
- 자동 스탬프 수집 제안

### 2. 스토리텔링
- 랜드마크 도달 시 스토리 카드 표시
- 역사, 문화, 자연, 팁 등 다양한 콘텐츠
- 오디오 가이드 지원

### 3. 소셜 기능
- 방명록 작성 및 공유
- 다른 사용자 방명록 조회
- 랜드마크별 커뮤니티

### 4. 수집 요소
- 랜드마크 스탬프 수집
- 특별 스탬프 (조건 달성)
- 수집 현황 및 통계

---

## ⚡ 우선순위 구현 순서

1. **Phase 1**: 핵심 엔티티 및 Repository
2. **Phase 2**: Journey 시작/진행 API
3. **Phase 3**: Landmark 및 Story 시스템
4. **Phase 4**: 스탬프 수집 기능
5. **Phase 5**: 방명록 및 소셜 기능
6. **Phase 6**: 기존 시스템 제거 및 정리

