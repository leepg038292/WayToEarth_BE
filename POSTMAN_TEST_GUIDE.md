# 🌍 WayToEarth API Postman 테스트 가이드

## 📋 **개요**

WayToEarth 백엔드 API (총 55개 엔드포인트)의 완전한 Postman 테스트 가이드입니다.

## 🔐 **인증 설정**

### **Mock 로그인 API**
개발/테스트용 Mock 로그인 API를 제공합니다.

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
    "isNewUser": false
  }
}
```

### **환경 변수 설정**
```json
{
  "baseUrl": "http://localhost:8080",
  "jwtToken": "{{동적으로 설정됨}}",
  "userId": "1",
  "journeyId": "{{동적으로 설정됨}}",
  "progressId": "{{동적으로 설정됨}}",
  "landmarkId": "{{동적으로 설정됨}}"
}
```

## 📊 **API 카테고리별 테스트**

### **🔐 인증 APIs (3개)**
1. `POST /v1/auth/kakao` - 카카오 로그인
2. `POST /v1/auth/onboarding` - 온보딩 완료
3. `POST /v1/auth/mock-login` - Mock 로그인 (테스트용)

### **👤 사용자 APIs (3개)**
1. `GET /v1/users/me` - 내 정보 조회
2. `GET /v1/users/me/summary` - 내 정보 요약
3. `PUT /v1/users/me` - 내 정보 수정

### **🏃‍♂️ 러닝 APIs (7개)**
1. `POST /v1/running/start` - 러닝 시작 (SINGLE/JOURNEY)
2. `POST /v1/running/update` - 러닝 업데이트
3. `POST /v1/running/pause` - 러닝 일시정지
4. `POST /v1/running/resume` - 러닝 재개
5. `POST /v1/running/complete` - 러닝 완료
6. `GET /v1/running/{recordId}` - 러닝 기록 상세
7. `GET /v1/running/records` - 러닝 기록 목록

### **🗺️ 여정 APIs (5개)**
1. `GET /v1/journeys` - 여정 목록 조회
2. `GET /v1/journeys/{journeyId}` - 여정 상세 조회
3. `POST /v1/journeys/{journeyId}/start` - 여정 시작
4. `GET /v1/journeys/search` - 여정 검색
5. `GET /v1/journeys/{journeyId}/completion-estimate` - 완주 예상 기간

### **📍 랜드마크 & 스토리 APIs (4개)**
1. `GET /v1/landmarks/{landmarkId}` - 랜드마크 상세
2. `GET /v1/landmarks/{landmarkId}/stories` - 스토리 카드 목록
3. `GET /v1/landmarks/journey/{journeyId}` - 여정별 랜드마크
4. `GET /v1/story-cards/{storyCardId}` - 스토리 카드 상세

### **🛤️ 여정 진행 APIs (3개)**
1. `PUT /v1/journey-progress/{progressId}` - 진행률 업데이트
2. `GET /v1/journey-progress/{progressId}` - 진행 상세
3. `GET /v1/journey-progress/user/{userId}` - 사용자 여정 목록

### **🎯 스탬프 APIs (6개)**
1. `POST /v1/stamps/collect` - 스탬프 수집
2. `GET /v1/stamps/users/{userId}` - 사용자 스탬프 목록
3. `GET /v1/stamps/progress/{progressId}` - 진행별 스탬프
4. `GET /v1/stamps/users/{userId}/statistics` - 스탬프 통계
5. `GET /v1/stamps/check-collection` - 수집 여부 확인

### **📝 방명록 APIs (6개)**
1. `POST /v1/guestbook` - 방명록 작성
2. `GET /v1/guestbook/landmarks/{landmarkId}` - 랜드마크별 방명록
3. `GET /v1/guestbook/users/{userId}` - 사용자별 방명록
4. `GET /v1/guestbook/recent` - 최근 방명록
5. `GET /v1/guestbook/landmarks/{landmarkId}/statistics` - 방명록 통계

### **📱 피드 APIs (6개)**
1. `POST /v1/feeds` - 피드 작성
2. `GET /v1/feeds` - 피드 목록
3. `GET /v1/feeds/{feedId}` - 피드 상세
4. `DELETE /v1/feeds/{feedId}` - 피드 삭제
5. `POST /v1/feeds/{feedId}/like` - 피드 좋아요
6. `POST /v1/feeds/{feedId}/image/presign` - 피드 이미지 업로드 URL

### **📁 파일 APIs (3개)**
1. `POST /v1/files/presign/profile` - 프로필 이미지 업로드 URL
2. `POST /v1/files/presign/feed` - 피드 이미지 업로드 URL
3. `DELETE /v1/files/profile` - 프로필 이미지 삭제

### **🏆 엠블럼 APIs (6개)**
1. `GET /v1/emblems/me/summary` - 내 엠블럼 요약
2. `GET /v1/emblems/catalog` - 엠블럼 카탈로그
3. `GET /v1/emblems/{id}` - 엠블럼 상세
4. `POST /v1/emblems/{id}/award` - 엠블럼 수여
5. `POST /v1/emblems/award/scan` - 엠블럼 스캔 수여

### **기타 APIs (3개)**
1. `GET /v1/statistics/weekly` - 주간 통계
2. `GET /v1/weather/current` - 현재 날씨
3. `GET /` - 루트 페이지

## 🚀 **주요 테스트 시나리오**

### **시나리오 1: 여정 시작부터 완료까지**
```
1. Mock 로그인 → JWT 토큰 획득
2. 여정 목록 조회 → 여정 선택
3. 여정 시작 → progress ID 획득
4. 러닝 시작 (JOURNEY 타입)
5. 진행률 업데이트 → 거리 누적
6. 랜드마크 도달 시 스탬프 수집
7. 방명록 작성
8. 여정 완료 확인
```

### **시나리오 2: 일반 러닝**
```
1. Mock 로그인
2. 러닝 시작 (SINGLE 타입)
3. 러닝 업데이트 (거리, 속도 등)
4. 러닝 완료
5. 러닝 기록 조회
```

### **시나리오 3: 소셜 기능**
```
1. Mock 로그인
2. 피드 작성 (러닝 기록 공유)
3. 피드 목록 조회
4. 피드 좋아요
5. 방명록 작성
6. 최근 방명록 조회
```

## 🔧 **Postman Collection 설정**

### **Pre-request Script 예시**
```javascript
// Mock 로그인 자동 실행
if (!pm.environment.get("jwtToken")) {
    pm.sendRequest({
        url: pm.environment.get("baseUrl") + "/v1/auth/mock-login",
        method: 'POST',
        header: {
            'Content-Type': 'application/json'
        },
        body: {
            mode: 'raw',
            raw: JSON.stringify({
                "userId": 1
            })
        }
    }, function (err, res) {
        if (!err && res.json().success) {
            pm.environment.set("jwtToken", res.json().data.jwtToken);
            pm.environment.set("userId", res.json().data.userId);
        }
    });
}
```

### **Authorization 설정**
```
Type: Bearer Token
Token: {{jwtToken}}
```

### **Tests Script 예시**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has success field", function () {
    const jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('success');
    pm.expect(jsonData.success).to.be.true;
});

// ID 자동 추출 및 환경변수 설정
if (pm.response.json().data && pm.response.json().data.id) {
    pm.environment.set("extractedId", pm.response.json().data.id);
}
```

## ⚠️ **주의사항**

1. **환경 설정**: 로컬 서버 (localhost:8080) 기준으로 설정
2. **인증 토큰**: Mock 로그인으로 JWT 토큰 자동 획득
3. **데이터 종속성**: 일부 API는 기존 데이터(여정, 랜드마크 등)가 필요
4. **오류 처리**: 403 오류 시 JWT 토큰 재발급 필요

## 📝 **API 문서 연동**

Swagger UI: `http://localhost:8080/swagger-ui.html`

---

**총 55개 API 엔드포인트**를 통해 완전한 가상 여행 러닝 플랫폼 테스트가 가능합니다! 🌍🏃‍♂️