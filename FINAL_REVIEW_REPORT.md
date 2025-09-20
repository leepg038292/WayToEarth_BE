# Journey Running 시스템 최종 검토 보고서

## **구현 완성도 종합 평가**



| 영역 | 구현 항목 | 상태 |
|------|-----------|-----|
| **엔티티 설계** | 8개 핵심 엔티티 |  완료 |
| **데이터 계층** | 6개 Repository + 30+ 쿼리 메서드 |  완료 |
| **비즈니스 계층** | 4개 Service (Interface + Impl) |  완료 |
| **API 계층** | 4개 Controller + 26개 엔드포인트 |  완료 |
| **DTO 계층** | Request/Response DTO 완비 |  완료 |

###  **핵심 기능 구현 현황**

#### A. 여정 탐색 및 선택
-  여정 목록 조회 (카테고리별, 난이도별)
-  완주자 통계 표시 ("이 여정을 완주한 러너 1,234명")
-  예상 완주 기간 계산 (주 3회 5km 기준)
-  제목 검색 및 거리 범위 필터링
-  랜드마크 개수 및 하이라이트 정보

#### B. 진행 중 경험 
-  러닝 완료 후 진행률 업데이트
-  "오늘 5km를 뛰어 총 47km 진행" 메시지
-  "다음 랜드마크까지 3km 남음" 정보
-  거리 기반 자동 진행률 계산
-  지도 시각화 (Frontend 영역)

#### C. 랜드마크에서의 활동 
-  스토리 카드 읽기 (역사/문화/자연/팁)
-  오디오 가이드 지원 (URL 제공)
-  랜드마크 상세 정보 및 이미지
-  순서대로 정렬된 스토리 표시

#### D. 소셜 및 수집 요소 
-  스탬프 수집 시스템 (위치 기반)
-  특별 스탬프 (첫/마지막 랜드마크, 첫 수집)
-  스탬프 등급 시스템 (BRONZE/SILVER/GOLD/DIAMOND)
-  방명록 작성 (기분/평점/사진 포함)
-  랜드마크별 통계 (평점, 방문자 수)
-  방명록 좋아요/신고 (엔티티 완료, 서비스 로직 추가 필요)

## ️ **구현된 아키텍처**

### 엔티티 관계도
```
JourneyEntity (여정)
├── LandmarkEntity (랜드마크)
│   ├── StoryCardEntity (스토리 카드)
│   ├── StampEntity (스탬프)
│   └── GuestbookEntity (방명록)
│       ├── GuestbookLikeEntity (좋아요)
│       └── GuestbookReportEntity (신고)
└── UserJourneyProgressEntity (진행률)
    └── StampEntity (수집한 스탬프)
```

### API 엔드포인트 구조
```
/api/v1/journeys
├── GET / (목록 조회)
├── GET /{id} (상세 조회)
├── POST /{id}/start (시작)
├── GET /search (검색)
└── GET /{id}/completion-estimate (예상 기간)

/api/v1/journey-progress
├── PUT /{id} (진행률 업데이트)
├── GET /{id} (현재 진행률)
└── GET /user/{userId} (사용자 여정 목록)

/api/v1/landmarks
├── GET /{id} (상세 정보)
├── GET /{id}/stories (스토리 카드)
└── GET /journey/{journeyId} (여정의 랜드마크)

/api/v1/stamps
├── POST /collect (스탬프 수집)
├── GET /users/{userId} (사용자 스탬프)
├── GET /progress/{progressId} (여정별 스탬프)
├── GET /users/{userId}/statistics (통계)
└── GET /check-collection (수집 가능 여부)

/api/v1/guestbook
├── POST / (방명록 작성)
├── GET /landmarks/{landmarkId} (랜드마크 방명록)
├── GET /users/{userId} (내 방명록)
├── GET /recent (최근 방명록)
└── GET /landmarks/{landmarkId}/statistics (통계)
```

##  **핵심 비즈니스 로직**

### 1. 진행률 계산 시스템
```java
// 거리 기반 자동 계산
progressPercent = (currentDistance / totalDistance) * 100
// 100% 달성 시 자동 완료 처리
```

### 2. 스탬프 수집 검증
```java
// 위치 기반 검증 (500m 반경)
// 진행률 기반 검증 (랜드마크 도달 확인)
// 중복 수집 방지
```

### 3. 특별 스탬프 판정
```java
// 첫 번째 랜드마크, 마지막 랜드마크
// 사용자 첫 스탬프
// 향후 확장: 시즌 스탬프, 달성 조건별
```

##  **성능 최적화 포인트**

### 쿼리 최적화
- Fetch Join으로 N+1 문제 해결
- 인덱스 활용한 효율적인 검색
- 페이징 처리로 대용량 데이터 대응

### 확장성 고려
- Enum 기반 확장 가능한 분류 체계
- Repository 패턴으로 데이터 계층 추상화
- Interface 기반 서비스 계층 설계

## **남은 작업 및 개선 사항**

### 즉시 보완 필요 (Backend)
1. **방명록 좋아요/신고 서비스 로직** 구현
2. **스팸 필터링 로직** 추가 (욕설 필터, 신고 처리)
3. **시즌 스탬프 이벤트 시스템** 구현
4. **컬렉션 완성 보상 시스템** 구현

### Frontend 협업 필요
1. **지도 시각화** (Google Maps API)
2. **실시간 애니메이션** (진행률, 스탬프 수집)
3. **경로 미리보기** (전체 여정 지도)
4. **푸시 알림** (랜드마크 도달, 스탬프 수집)

### 운영 도구 필요
1. **여정 콘텐츠 관리 시스템** (어드민)
2. **스토리 카드 에디터** (어드민)
3. **신고 처리 시스템** (어드민)
4. **통계 대시보드** (어드민)

