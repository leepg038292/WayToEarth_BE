# Journey Running API Postman 테스트 가이드

## 📋 **파일 설명**

- `Journey_Running_API_Postman_Collection.json` - 28개 API 엔드포인트 테스트 컬렉션 (Mock 로그인 포함)
- `Journey_Running_Environment.postman_environment.json` - 환경 변수 설정
- `POSTMAN_TEST_GUIDE.md` - 이 가이드 파일

## 🔐 **NEW! 인증 문제 해결**

### **Mock 로그인 API 추가**
403 Forbidden 오류 해결을 위해 테스트용 Mock 로그인 API가 추가되었습니다.

**엔드포인트**: `POST /v1/auth/mock-login`
**요청 예시**:
```json
{
  "userId": 1
}
```

**응답 예시**:
```json
{
  "success": true,
  "message": "Mock 로그인에 성공했습니다.",
  "data": {
    "userId": 1,
    "jwtToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "isNewUser": false,
    "isOnboardingCompleted": true
  }
}
```

## 🚀 **Postman 설정 방법**

### 1. Collection Import
1. Postman 열기
2. **Import** 버튼 클릭
3. `Journey_Running_API_Postman_Collection.json` 파일 선택
4. Import 완료

### 2. Environment Import
1. Postman에서 **Environments** 탭 클릭
2. **Import** 버튼 클릭
3. `Journey_Running_Environment.postman_environment.json` 파일 선택
4. Import 완료
5. **Journey Running Environment** 환경 선택

### 3. 환경 변수 설정
기본 설정된 환경 변수들:
```
baseUrl = http://localhost:8080
userId = 1
journeyId = 1
landmarkId = 1
storyCardId = 1
progressId = (자동 설정됨)
```

## 🎯 **테스트 시나리오**

### **시나리오 0: 인증 토큰 획득 (필수!)**
```
1. Mock Login - JWT 토큰 획득 (authToken 자동 저장됨)
   ⚠️ 모든 테스트 전에 반드시 먼저 실행!
```

### **시나리오 1: 새로운 여정 시작하기**
```
1. Get All Journeys - 여정 목록 확인
2. Get Journey Detail - 특정 여정 상세 보기
3. Get Completion Estimate - 완주 예상 기간 계산
4. Start Journey - 여정 시작 (progressId 자동 저장됨)
   ✅ 이제 RunningRecord도 함께 생성됨 (JOURNEY 타입)
```

### **시나리오 2: 러닝 후 진행률 업데이트**
```
1. Update Progress - 러닝 완료 후 진행률 업데이트
   ✅ 여정 진행률 + RunningRecord 완료 처리 동시 진행
2. Get Current Progress - 현재 진행률 확인
3. Get Journey Landmarks - 다음 랜드마크 확인
```

### **시나리오 3: 랜드마크 도달 및 스토리 경험**
```
1. Get Landmark Detail - 랜드마크 상세 정보
2. Get Landmark Stories - 랜드마크 스토리 카드 목록
3. Get Story Card Detail - 개별 스토리 상세
```

### **시나리오 4: 스탬프 수집**
```
1. Check Collection Availability - 수집 가능 여부 확인
2. Collect Stamp - 스탬프 수집
3. Get Progress Stamps - 수집된 스탬프 확인
4. Get Stamp Statistics - 스탬프 통계 확인
```

### **시나리오 5: 방명록 작성 및 소셜**
```
1. Create Guestbook - 방명록 작성
2. Get Landmark Guestbook - 랜드마크 방명록 조회
3. Get Landmark Statistics - 랜드마크 통계 확인
```

## 📊 **포함된 API 목록 (28개)**

### **00. Authentication (1개)**
- `POST /v1/auth/mock-login` - Mock 로그인 (JWT 토큰 획득)

### **01. Journey Management (6개)**
- `GET /v1/journeys` - 여정 목록 조회
- `GET /v1/journeys?category=DOMESTIC` - 카테고리별 여정 조회
- `GET /v1/journeys/{id}` - 여정 상세 조회
- `POST /v1/journeys/{id}/start` - 여정 시작 ⭐ **RunningRecord 자동 생성**
- `GET /v1/journeys/search` - 여정 검색
- `GET /v1/journeys/{id}/completion-estimate` - 완주 예상 기간

### **02. Journey Progress (3개)**
- `PUT /v1/journey-progress/{id}` - 진행률 업데이트 ⭐ **RunningRecord 완료 처리**
- `GET /v1/journey-progress/{id}` - 진행률 조회
- `GET /v1/journey-progress/user/{userId}` - 사용자 여정 목록

### **03. Landmarks (4개)**
- `GET /v1/landmarks/{id}` - 랜드마크 상세
- `GET /v1/landmarks/{id}/stories` - 랜드마크 스토리
- `GET /v1/landmarks/journey/{journeyId}` - 여정의 랜드마크 목록
- `GET /v1/story-cards/{id}` - 스토리 카드 상세

### **04. Stamps (6개)**
- `GET /v1/stamps/check-collection` - 수집 가능 여부 확인
- `POST /v1/stamps/collect` - 스탬프 수집
- `GET /v1/stamps/users/{userId}` - 사용자 스탬프
- `GET /v1/stamps/progress/{progressId}` - 여정별 스탬프
- `GET /v1/stamps/progress/{progressId}/special` - 특별 스탬프
- `GET /v1/stamps/users/{userId}/statistics` - 스탬프 통계

### **05. Guestbook (8개)**
- `POST /v1/guestbook` - 방명록 작성
- `GET /v1/guestbook/landmarks/{landmarkId}` - 랜드마크 방명록
- `GET /v1/guestbook/landmarks/{landmarkId}` (mood 필터) - 기분별 방명록
- `GET /v1/guestbook/landmarks/{landmarkId}` (rating 필터) - 평점별 방명록
- `GET /v1/guestbook/users/{userId}` - 사용자 방명록
- `GET /v1/guestbook/recent` - 최근 방명록
- `GET /v1/guestbook/landmarks/{landmarkId}/statistics` - 랜드마크 통계

## 🔧 **자동 테스트 기능**

### **Pre-request Script**
- 환경 변수 자동 초기화
- 기본값 설정

### **Test Script**
- 응답 상태 코드 검증 (200, 201, 204)
- JSON 형식 검증
- 응답 시간 검증 (2초 이내)
- API 응답에서 ID 값 자동 추출 및 환경 변수 저장

### **환경 변수 자동 업데이트**
- `progressId`: Journey 시작 시 자동 저장
- `journeyId`, `landmarkId`, `storyCardId`: 응답에서 자동 추출

## 📝 **테스트 데이터 예시**

### **Journey Start Request**
```json
{
  "userId": 1,
  "journeyId": 1
}
```

### **Progress Update Request**
```json
{
  "sessionId": "session-uuid-123",
  "distanceKm": 5.2,
  "currentLocation": {
    "latitude": 37.5665,
    "longitude": 126.9780
  },
  "durationSeconds": 1800,
  "calories": 250,
  "averagePaceSeconds": 360
}
```

### **Stamp Collection Request**
```json
{
  "progressId": 1,
  "landmarkId": 1,
  "collectionLocation": {
    "latitude": 37.5665,
    "longitude": 126.9780
  }
}
```

### **Guestbook Create Request**
```json
{
  "landmarkId": 1,
  "message": "정말 아름다운 곳이에요! 다시 오고 싶습니다.",
  "photoUrl": "https://example.com/photo.jpg",
  "mood": "AMAZED",
  "rating": 5,
  "isPublic": true
}
```

## 🎉 **사용 팁**

1. **⚠️ 필수 첫 단계**: Mock Login으로 JWT 토큰 획득 후 테스트 시작
2. **순서대로 테스트**: Mock Login → 여정 시작 → 진행률 업데이트 → 스탬프 수집 → 방명록 작성
3. **환경 변수 활용**: {{변수명}} 형태로 동적 데이터 사용
4. **자동 테스트**: Collection Runner로 전체 API 일괄 테스트 가능
5. **Mock 데이터**: 실제 DB 데이터가 없어도 API 구조 확인 가능

## 🔧 **NEW! 러닝 기록 연동**

여정 러닝 시 다음과 같이 이중으로 기록됩니다:
- **UserJourneyProgress**: 여정 진행률, 스탬프 수집 상태
- **RunningRecord**: 상세 러닝 기록 (거리, 시간, 칼로리, 경로) - `JOURNEY` 타입

**연결 방식**: sessionId로 두 테이블이 연결되어 완전한 러닝 데이터 저장

이제 Journey Running API의 모든 기능을 Postman에서 체계적으로 테스트할 수 있습니다! 🚀