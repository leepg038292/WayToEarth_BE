# Journey Running 시스템 Migration 계획

## 📋 완료된 작업 (Phase 1-5)

### ✅ 새로운 엔티티 구현 완료
- `JourneyEntity`: 여행 정보
- `LandmarkEntity`: 랜드마크 정보
- `StoryCardEntity`: 스토리 카드
- `UserJourneyProgressEntity`: 사용자 여행 진행
- `StampEntity`: 스탬프 수집
- `GuestbookEntity`: 방명록

### ✅ Repository 구현 완료
- 모든 엔티티에 대한 Repository 인터페이스
- 복잡한 쿼리 메서드 정의
- 페이징 및 통계 기능 포함

### ✅ Service 및 Controller 구현 완료
- `JourneyService`: 여행 관리
- `LandmarkService`: 랜드마크 및 스토리
- `StampService`: 스탬프 수집
- `GuestbookService`: 방명록 및 소셜

### ✅ API 엔드포인트 구현 완료
- Journey Management API
- Journey Progress API
- Landmark & Story API
- Stamp Collection API
- Guestbook API

## 🗑️ 삭제 예정 파일 목록 (Phase 6)

### 완전 삭제 대상 엔티티
```
src/main/java/com/waytoearth/entity/VirtualRunning/
├── CourseSegmentEntity.java ❌
├── SegmentProgressEntity.java ❌
├── SegmentLandmarkEntity.java ❌
├── CustomCourseEntity.java ❌
└── ProgressUpdateLog.java ❌
```

### 완전 삭제 대상 Repository
```
src/main/java/com/waytoearth/repository/VirtualRunning/
├── CourseSegmentRepository.java ❌
├── SegmentProgressRepository.java ❌
├── SegmentLandmarkRepository.java ❌
├── CustomCourseRepository.java ❌
└── ProgressUpdateLogRepository.java ❌
```

### 완전 삭제 대상 Service
```
src/main/java/com/waytoearth/service/VirtualRunning/
├── CourseSegmentService.java ❌
├── SegmentLandmarkService.java ❌
├── SegmentEmblemService.java ❌
├── SegmentWeatherService.java ❌
├── CustomCourseService.java ❌
└── ProgressUpdateLogCleanupService.java ❌
```

### 수정/이름변경 대상
```
ThemeCourseEntity.java → JourneyEntity.java로 통합 ✅ (완료)
UserVirtualCourseService.java → UserJourneyService.java로 변경 예정
UserVirtualCourseServiceImpl.java → UserJourneyServiceImpl.java로 변경 예정
UserVirtualCourseRepository.java → UserJourneyProgressRepository.java로 통합 ✅ (완료)
```

## 🔄 Migration 실행 순서

### Phase 6: 기존 시스템 제거 및 정리
1. **Controller 레벨 제거**
   - UserVirtualCourseController.java 삭제
   - 관련 API 엔드포인트 제거

2. **Service 레벨 제거**
   - 위에 나열된 VirtualRunning Service 파일들 삭제
   - 관련 의존성 제거

3. **Repository 레벨 제거**
   - 위에 나열된 VirtualRunning Repository 파일들 삭제

4. **Entity 레벨 제거**
   - 위에 나열된 VirtualRunning Entity 파일들 삭제
   - ThemeCourseEntity.java는 보존 (Journey로 활용 가능)

5. **데이터베이스 Migration**
   ```sql
   -- 기존 테이블 삭제 (데이터 백업 후)
   DROP TABLE IF EXISTS course_segments;
   DROP TABLE IF EXISTS segment_progress;
   DROP TABLE IF EXISTS segment_landmark;
   DROP TABLE IF EXISTS custom_course;
   DROP TABLE IF EXISTS progress_update_log;

   -- 새 테이블들은 JPA가 자동 생성
   ```

## 🎯 새로운 시스템의 핵심 장점

### 1. 단순화된 구조
- 복잡한 세그먼트 기반 → 간단한 거리 기반 진행률
- 하나의 Progress 엔티티로 통합 관리

### 2. 스토리텔링 중심
- 랜드마크별 다양한 스토리 카드
- 오디오 가이드 지원
- 문화/역사/자연/팁 카테고리

### 3. 소셜 기능 강화
- 스탬프 수집 시스템
- 방명록 및 후기 공유
- 랜드마크별 커뮤니티

### 4. 성능 최적화
- 복잡한 세그먼트 계산 제거
- 단순한 거리 기반 계산
- 효율적인 쿼리 구조

## ⚠️ 주의사항

### 기존 데이터 처리
- ThemeCourse 데이터를 Journey로 이관 가능
- 사용자 진행률 데이터는 새로운 구조로 초기화
- 중요한 통계 데이터는 별도 백업

### 점진적 전환
- 새로운 API와 기존 API 병행 운영 가능
- 사용자별로 점진적 migration 가능
- Feature Flag 활용한 단계별 전환

## 🚀 배포 계획

1. **Stage 1**: 새로운 시스템 배포 (기존 시스템과 병행)
2. **Stage 2**: 사용자 데이터 migration
3. **Stage 3**: 기존 시스템 비활성화
4. **Stage 4**: 기존 코드 및 테이블 정리