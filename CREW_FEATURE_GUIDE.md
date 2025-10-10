# 크루 기능 구현 가이드

## 📋 목차
1. [개요](#개요)
2. [데이터 구조](#데이터-구조)
3. [API 엔드포인트](#api-엔드포인트)
4. [기능별 상세 설명](#기능별-상세-설명)
5. [프론트엔드 구현 예시](#프론트엔드-구현-예시)
6. [주요 비즈니스 로직](#주요-비즈니스-로직)

---

## 개요

크루 기능은 사용자들이 함께 러닝하고, 소통하며, 경쟁할 수 있는 소셜 러닝 커뮤니티 시스템입니다.

### 핵심 개념
- **Crew (크루)**: 러닝 그룹 (최대 인원 설정 가능)
- **Crew Member (크루 멤버)**: 크루에 속한 사용자
- **Crew Role (역할)**: OWNER (크루장), MEMBER (일반 멤버)
- **Join Request (가입 신청)**: 크루 가입 요청 및 승인/거부 시스템
- **Crew Statistics (크루 통계)**: 월별 러닝 기록 통계 및 랭킹
- **Crew Chat (크루 채팅)**: 크루 내 실시간 채팅
- **MVP**: 월간 최고 기여자 (거리 기준)

---

## 데이터 구조

### 1. Entity 관계도

```
User (사용자)
  ↓
CrewEntity (크루) ← owner (크루장)
  ↓ 1:N
  ├── CrewMemberEntity (크루 멤버)
  │     ↓ N:1
  │     User (사용자)
  │
  ├── CrewJoinRequestEntity (가입 신청)
  │     ↓ N:1
  │     User (신청자)
  │
  ├── CrewStatisticsEntity (월별 통계)
  │     ↓ N:1
  │     User (MVP 사용자)
  │
  └── CrewChatEntity (채팅 메시지)
        ↓ N:1
        User (발신자)
        ↓ 1:N
        CrewChatReadStatusEntity (읽음 상태)
```

### 2. CrewEntity (크루)

**위치**: `com.waytoearth.entity.crew.CrewEntity`

**주요 필드**:
```java
- id: Long                      // 크루 ID
- name: String                  // 크루 이름 (unique, 최대 50자)
- description: String           // 크루 소개 (최대 500자)
- maxMembers: Integer           // 최대 인원 (기본값: 50, 1~1000)
- currentMembers: Integer       // 현재 멤버 수
- profileImageUrl: String       // 프로필 이미지 URL
- isActive: Boolean             // 활성화 상태
- owner: User                   // 크루장
- version: Long                 // 낙관적 잠금용 버전
- members: List<CrewMemberEntity>         // 멤버 목록
- joinRequests: List<CrewJoinRequestEntity> // 가입 신청 목록
```

**비즈니스 메서드**:
```java
- isFull(): boolean                    // 정원 초과 여부
- isOwner(User): boolean               // 크루장 여부 확인
- getCurrentMemberCount(): int         // 활성 멤버 수
- incrementMemberCount(): void         // 멤버 수 증가
- decrementMemberCount(): void         // 멤버 수 감소
```

### 3. CrewMemberEntity (크루 멤버)

**위치**: `com.waytoearth.entity.crew.CrewMemberEntity`

**주요 필드**:
```java
- id: Long                      // 멤버십 ID
- crew: CrewEntity              // 소속 크루
- user: User                    // 사용자
- role: CrewRole                // 역할 (OWNER/MEMBER)
- joinedAt: LocalDateTime       // 가입일
- isActive: Boolean             // 활성 상태
```

**역할 (CrewRole Enum)**:
```java
public enum CrewRole {
    OWNER("OWNER", "크루장"),
    MEMBER("MEMBER", "일반 멤버")
}
```

### 4. CrewJoinRequestEntity (가입 신청)

**위치**: `com.waytoearth.entity.crew.CrewJoinRequestEntity`

**주요 필드**:
```java
- id: Long                      // 신청 ID
- crew: CrewEntity              // 대상 크루
- user: User                    // 신청자
- message: String               // 신청 메시지 (최대 500자)
- status: JoinRequestStatus     // 신청 상태
- processedAt: LocalDateTime    // 처리일
- processedBy: User             // 처리자 (크루장)
- processingNote: String        // 처리 메모 (최대 500자)
```

**신청 상태 (JoinRequestStatus Enum)**:
```java
public enum JoinRequestStatus {
    PENDING("PENDING", "대기중"),
    APPROVED("APPROVED", "승인됨"),
    REJECTED("REJECTED", "거부됨"),
    CANCELLED("CANCELLED", "취소됨")
}
```

### 5. CrewStatisticsEntity (월별 통계)

**위치**: `com.waytoearth.entity.crew.CrewStatisticsEntity`

**주요 필드**:
```java
- id: Long                      // 통계 ID
- crew: CrewEntity              // 크루
- month: String                 // 통계 년월 (YYYYMM, 예: "202412")
- runCount: Integer             // 해당 월 러닝 횟수
- totalDistance: BigDecimal     // 해당 월 총 거리 (km)
- activeMembers: Integer        // 참여한 고유 멤버 수
- avgPaceSeconds: BigDecimal    // 평균 페이스 (초)
- mvpUser: User                 // 월간 MVP 사용자
- mvpDistance: BigDecimal       // MVP의 총 거리 (km)
- version: Long                 // 낙관적 잠금용 버전
```

**비즈니스 메서드**:
```java
- updateWithMemberRun(memberDistance, memberPaceSeconds, isNewActiveMember): void
  // 멤버의 러닝 기록으로 통계 업데이트

- resetForNewMonth(newMonth): void
  // 새 달 통계로 리셋

- getFormattedAvgPace(): String
  // 평균 페이스를 "MM:SS" 형식으로 반환
```

### 6. CrewChatEntity (채팅 메시지)

**위치**: `com.waytoearth.entity.crew.CrewChatEntity`

**주요 필드**:
```java
- id: Long                      // 메시지 ID
- crew: CrewEntity              // 크루
- sender: User                  // 발신자
- message: String               // 메시지 내용 (최대 1000자)
- messageType: MessageType      // 메시지 타입
- sentAt: LocalDateTime         // 전송 시간
- isDeleted: Boolean            // 삭제 여부
- isActive: Boolean             // 활성 여부
- readStatus: List<CrewChatReadStatusEntity> // 읽음 상태 목록
```

**메시지 타입 (MessageType Enum)**:
```java
public enum MessageType {
    TEXT,           // 일반 텍스트
    SYSTEM,         // 시스템 메시지 (입장/퇴장 등)
    ANNOUNCEMENT    // 공지사항 (크루장 전용)
}
```

---

## API 엔드포인트

크루 API는 5개 컨트롤러로 나뉘어 있습니다:
1. **CrewController** - 크루 기본 관리 (생성, 조회, 수정, 삭제)
2. **CrewJoinController** - 가입 신청 관리
3. **CrewMemberController** - 멤버 관리
4. **CrewStatisticsController** - 통계 및 랭킹
5. **CrewChatController** - 채팅 기능

### 1. 크루 기본 관리 (CrewController)

#### 1.1 크루 생성
```
POST /v1/crews
```

**Request Body**:
```json
{
  "name": "서울 러닝 크루",
  "description": "함께 달리며 건강한 라이프스타일을 추구하는 크루입니다",
  "maxMembers": 20,
  "profileImageUrl": "https://example.com/crew-profile.jpg"
}
```

**Response** (201 Created):
```json
{
  "id": 1,
  "name": "서울 러닝 크루",
  "description": "함께 달리며 건강한 라이프스타일을 추구하는 크루입니다",
  "maxMembers": 20,
  "currentMembers": 1,
  "profileImageUrl": "https://example.com/crew-profile.jpg",
  "isActive": true,
  "ownerId": 123,
  "ownerNickname": "김러너",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

**특징**:
- 생성자가 자동으로 크루장(OWNER)이 됨
- currentMembers는 1로 시작 (크루장 포함)

#### 1.2 크루 상세 조회
```
GET /v1/crews/{crewId}
```

**Response** (200 OK):
```json
{
  "id": 1,
  "name": "서울 러닝 크루",
  "description": "함께 달리며 건강한 라이프스타일을 추구하는 크루입니다",
  "maxMembers": 20,
  "currentMembers": 15,
  "profileImageUrl": "https://example.com/crew-profile.jpg",
  "isActive": true,
  "ownerId": 123,
  "ownerNickname": "김러너",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

#### 1.3 크루 목록 조회 (페이징)
```
GET /v1/crews?page=0&size=20&sort=createdAt&direction=desc
```

**Query Parameters**:
- `page`: 페이지 번호 (0부터 시작, 기본값: 0)
- `size`: 페이지 크기 (기본값: 20)
- `sort`: 정렬 기준 (createdAt, name 등, 기본값: createdAt)
- `direction`: 정렬 방향 (asc/desc, 기본값: desc)

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": 1,
      "name": "서울 러닝 크루",
      "description": "함께 달리며 건강한 라이프스타일을 추구하는 크루입니다",
      "maxMembers": 20,
      "currentMembers": 15,
      "profileImageUrl": "https://example.com/crew-profile.jpg",
      "ownerNickname": "김러너",
      "createdAt": "2024-01-15T10:30:00",
      "canJoin": true
    }
  ],
  "pageable": {...},
  "totalElements": 50,
  "totalPages": 3,
  "size": 20,
  "number": 0
}
```

**특징**:
- `canJoin`: 로그인한 사용자가 가입 가능한지 여부 (비로그인 시 null)

#### 1.4 크루 검색
```
GET /v1/crews/search?keyword=서울&page=0&size=20
```

**Query Parameters**:
- `keyword`: 검색 키워드 (필수)
- `page`, `size`: 페이징 파라미터

**Response**: 크루 목록과 동일

#### 1.5 내가 속한 크루 목록
```
GET /v1/crews/my?page=0&size=20
```

**Response**: 크루 목록과 동일 (canJoin은 항상 false)

#### 1.6 크루 정보 수정
```
PUT /v1/crews/{crewId}
```

**권한**: 크루장만 가능

**Request Body**:
```json
{
  "name": "서울 러닝 크루 (수정)",
  "description": "업데이트된 설명",
  "maxMembers": 30,
  "profileImageUrl": "https://example.com/new-profile.jpg"
}
```

**Response** (200 OK): 크루 상세 정보

#### 1.7 크루 삭제
```
DELETE /v1/crews/{crewId}
```

**권한**: 크루장만 가능

**Response** (204 No Content)

#### 1.8 크루 활성화/비활성화 토글
```
PATCH /v1/crews/{crewId}/toggle-status
```

**권한**: 크루장만 가능

**Response** (200 OK): 크루 상세 정보

---

### 2. 가입 신청 관리 (CrewJoinController)

#### 2.1 크루 가입 신청
```
POST /v1/crews/{crewId}/join-requests
```

**Request Body**:
```json
{
  "message": "안녕하세요! 함께 러닝하고 싶습니다."
}
```

**Response** (201 Created):
```json
{
  "id": 1,
  "crewId": 1,
  "crewName": "서울 러닝 크루",
  "userId": 456,
  "userNickname": "박러너",
  "message": "안녕하세요! 함께 러닝하고 싶습니다.",
  "status": "PENDING",
  "createdAt": "2024-01-15T10:30:00",
  "processedAt": null,
  "processedBy": null,
  "processingNote": null
}
```

**제약사항**:
- 이미 멤버인 경우: 400 Bad Request
- 대기 중인 신청이 있는 경우: 400 Bad Request
- 크루가 정원 초과인 경우: 400 Bad Request

#### 2.2 가입 신청 승인
```
POST /v1/crews/join-requests/{requestId}/approve
```

**권한**: 크루장만 가능

**Request Body**:
```json
{
  "note": "환영합니다!"
}
```

**Response** (200 OK)

**결과**:
- 신청자가 크루 멤버로 추가됨 (MEMBER 역할)
- 신청 상태가 APPROVED로 변경
- currentMembers 증가

#### 2.3 가입 신청 거부
```
POST /v1/crews/join-requests/{requestId}/reject
```

**권한**: 크루장만 가능

**Request Body**:
```json
{
  "note": "죄송합니다. 현재 정원이 초과되어..."
}
```

**Response** (200 OK)

**결과**:
- 신청 상태가 REJECTED로 변경

#### 2.4 가입 신청 취소
```
DELETE /v1/crews/join-requests/{requestId}
```

**권한**: 본인이 신청한 것만 가능

**Response** (200 OK)

**결과**:
- 신청 상태가 CANCELLED로 변경

#### 2.5 크루별 가입 신청 목록
```
GET /v1/crews/{crewId}/join-requests?status=PENDING&page=0&size=20
```

**권한**: 크루장만 가능

**Query Parameters**:
- `status`: 상태 필터 (PENDING, APPROVED, REJECTED, CANCELLED)
- `page`, `size`: 페이징 파라미터

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": 1,
      "crewId": 1,
      "crewName": "서울 러닝 크루",
      "userId": 456,
      "userNickname": "박러너",
      "message": "안녕하세요! 함께 러닝하고 싶습니다.",
      "status": "PENDING",
      "createdAt": "2024-01-15T10:30:00",
      "processedAt": null,
      "processedBy": null,
      "processingNote": null
    }
  ],
  "totalElements": 5,
  "totalPages": 1
}
```

#### 2.6 내 가입 신청 내역
```
GET /v1/crews/join-requests/my
```

**Response** (200 OK): 가입 신청 목록

#### 2.7 특정 크루에 대한 내 가입 신청 상태
```
GET /v1/crews/{crewId}/join-requests/my
```

**Response**:
- 신청 내역 있음: 200 OK + JoinRequestResponse
- 신청 내역 없음: 204 No Content

#### 2.8 크루 가입 가능 여부 확인
```
GET /v1/crews/{crewId}/can-join
```

**Response** (200 OK):
```json
true
```

**가입 불가능 조건**:
- 이미 멤버인 경우
- 대기 중인 신청이 있는 경우
- 크루가 정원 초과인 경우
- 크루가 비활성화된 경우

#### 2.9 대기 중인 가입 신청 수
```
GET /v1/crews/{crewId}/pending-requests/count
```

**Response** (200 OK):
```json
5
```

---

### 3. 멤버 관리 (CrewMemberController)

#### 3.1 크루 멤버 목록 조회 (페이징)
```
GET /v1/crews/{crewId}/members?page=0&size=20
```

**Response** (200 OK):
```json
{
  "content": [
    {
      "id": 1,
      "userId": 123,
      "userNickname": "김러너",
      "userProfileImage": "https://example.com/profile.jpg",
      "role": "OWNER",
      "joinedAt": "2024-01-15T10:30:00",
      "isActive": true,
      "isOwner": true
    },
    {
      "id": 2,
      "userId": 456,
      "userNickname": "박러너",
      "userProfileImage": "https://example.com/profile2.jpg",
      "role": "MEMBER",
      "joinedAt": "2024-01-16T11:00:00",
      "isActive": true,
      "isOwner": false
    }
  ],
  "totalElements": 15,
  "totalPages": 1
}
```

#### 3.2 크루 멤버 목록 조회 (전체)
```
GET /v1/crews/{crewId}/members/all
```

**Response** (200 OK): 멤버 목록 (페이징 없음)

#### 3.3 크루 멤버 추방
```
DELETE /v1/crews/{crewId}/members/{userId}
```

**권한**: 크루장만 가능

**제약사항**:
- 자기 자신을 추방할 수 없음

**Response** (200 OK)

**결과**:
- 멤버십 삭제
- currentMembers 감소

#### 3.4 크루 탈퇴
```
DELETE /v1/crews/{crewId}/members/leave
```

**제약사항**:
- 크루장은 탈퇴할 수 없음 (권한 이양 후 탈퇴 가능)

**Response** (200 OK)

**결과**:
- 멤버십 삭제
- currentMembers 감소

#### 3.5 크루 멤버 역할 변경
```
PATCH /v1/crews/{crewId}/members/{userId}/role
```

**권한**: 크루장만 가능

**Request Body**:
```json
{
  "newRole": "MEMBER"
}
```

**제약사항**:
- 자기 자신의 역할을 변경할 수 없음
- OWNER 역할로 변경 불가 (권한 이양 API 사용)

**Response** (200 OK): 멤버 정보

#### 3.6 크루장 권한 이양
```
POST /v1/crews/{crewId}/transfer-ownership
```

**권한**: 현재 크루장만 가능

**Request Body**:
```json
{
  "newOwnerId": 456
}
```

**Response** (200 OK)

**결과**:
- 현재 크루장 → MEMBER로 변경
- 새 크루장 → OWNER로 변경
- CrewEntity.owner 변경

#### 3.7 내가 속한 크루 목록 (멤버십 정보)
```
GET /v1/crews/memberships/my
```

**Response** (200 OK): 멤버 정보 목록

#### 3.8 특정 크루 멤버십 조회
```
GET /v1/crews/{crewId}/members/{userId}
```

**Response** (200 OK): 멤버 정보

#### 3.9 크루 멤버 수 조회
```
GET /v1/crews/{crewId}/members/count
```

**Response** (200 OK):
```json
15
```

#### 3.10 크루 일반 멤버 목록 (크루장 제외)
```
GET /v1/crews/{crewId}/members/regular
```

**Response** (200 OK): 일반 멤버 목록

---

### 4. 통계 및 랭킹 (CrewStatisticsController)

#### 4.1 크루 월간 통계 조회
```
GET /v1/crews/statistics/{crewId}/monthly?month=202412
```

**Query Parameters**:
- `month`: 년월 (YYYYMM 형식, 생략 시 현재 월)

**Response** (200 OK):
```json
{
  "crewId": 1,
  "crewName": "서울 러닝 크루",
  "month": "202412",
  "runCount": 150,
  "totalDistance": 2500.5,
  "activeMembers": 12,
  "avgPaceSeconds": 375.5,
  "avgPaceFormatted": "6:15",
  "mvpUserId": 123,
  "mvpNickname": "김러너",
  "mvpDistance": 250.0
}
```

#### 4.2 크루 전체 월간 통계 목록
```
GET /v1/crews/statistics/{crewId}/monthly/all
```

**Response** (200 OK): 모든 월의 통계 목록

#### 4.3 크루 기간별 통계 조회
```
GET /v1/crews/statistics/{crewId}/period?startMonth=202401&endMonth=202412
```

**Response** (200 OK): 기간 내 통계 목록

#### 4.4 크루 랭킹 (거리 기준)
```
GET /v1/crews/statistics/rankings/distance?month=202412&limit=10
```

**Query Parameters**:
- `month`: 년월 (YYYYMM, 생략 시 현재 월)
- `limit`: 조회할 랭킹 수 (기본값: 10)

**Response** (200 OK):
```json
[
  {
    "rank": 1,
    "crewId": 1,
    "crewName": "서울 러닝 크루",
    "totalDistance": 2500.5,
    "runCount": 150,
    "activeMembers": 12
  },
  {
    "rank": 2,
    "crewId": 2,
    "crewName": "부산 러닝 크루",
    "totalDistance": 2300.0,
    "runCount": 140,
    "activeMembers": 10
  }
]
```

#### 4.5 크루 랭킹 (러닝 횟수 기준)
```
GET /v1/crews/statistics/rankings/runs?month=202412&limit=10
```

**Response** (200 OK): 러닝 횟수 기준 랭킹

#### 4.6 크루 랭킹 (성장률 기준)
```
GET /v1/crews/statistics/rankings/growth?currentMonth=202412&previousMonth=202411&limit=10
```

**Response** (200 OK): 전월 대비 성장률 기준 랭킹

#### 4.7 크루 내 멤버 랭킹
```
GET /v1/crews/statistics/{crewId}/members/ranking?month=202412&limit=20
```

**Response** (200 OK):
```json
[
  {
    "rank": 1,
    "userId": 123,
    "userNickname": "김러너",
    "userProfileImage": "https://example.com/profile.jpg",
    "totalDistance": 250.0,
    "runCount": 20,
    "avgPaceSeconds": 360,
    "avgPaceFormatted": "6:00"
  },
  {
    "rank": 2,
    "userId": 456,
    "userNickname": "박러너",
    "userProfileImage": "https://example.com/profile2.jpg",
    "totalDistance": 200.5,
    "runCount": 18,
    "avgPaceSeconds": 380,
    "avgPaceFormatted": "6:20"
  }
]
```

#### 4.8 크루 월간 MVP
```
GET /v1/crews/statistics/{crewId}/mvp?month=202412
```

**Response**:
- MVP 있음: 200 OK + CrewMemberRankingDto
- MVP 없음: 204 No Content

#### 4.9 월간 MVP 갱신 (관리자용)
```
POST /v1/crews/statistics/{crewId}/mvp/refresh?month=202412
```

**Response** (200 OK)

#### 4.10 새 달 통계 초기화 (관리자용)
```
POST /v1/crews/statistics/{crewId}/reset?newMonth=202501
```

**Response** (200 OK)

---

### 5. 채팅 기능 (CrewChatController)

#### 5.1 채팅 메시지 목록 조회 (페이징)
```
GET /v1/crews/{crewId}/chat/messages?page=0&size=50
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "채팅 메시지를 조회했습니다.",
  "data": {
    "content": [
      {
        "id": 1,
        "crewId": 1,
        "senderId": 123,
        "senderNickname": "김러너",
        "senderProfileImage": "https://example.com/profile.jpg",
        "message": "오늘 러닝 어떠셨나요?",
        "messageType": "TEXT",
        "sentAt": "2024-01-15T10:30:00",
        "isDeleted": false,
        "isRead": true
      }
    ],
    "totalElements": 100,
    "totalPages": 2
  }
}
```

#### 5.2 최근 채팅 메시지 조회
```
GET /v1/crews/{crewId}/chat/messages/recent?limit=20
```

**Response** (200 OK): 최근 메시지 목록

#### 5.3 메시지 읽음 처리
```
POST /v1/crews/{crewId}/chat/messages/{messageId}/read
```

**Response** (200 OK)

#### 5.4 다중 메시지 읽음 처리
```
POST /v1/crews/{crewId}/chat/messages/read/batch
```

**Request Body**:
```json
[1, 2, 3, 4, 5]
```

**Response** (200 OK)

#### 5.5 특정 시점 이후 모든 메시지 읽음 처리
```
POST /v1/crews/{crewId}/chat/messages/read/all-after/{afterMessageId}
```

**Response** (200 OK)

#### 5.6 읽지 않은 메시지 수 조회
```
GET /v1/crews/{crewId}/chat/unread-count
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "읽지 않은 메시지 수를 조회했습니다.",
  "data": 5
}
```

#### 5.7 메시지 삭제
```
DELETE /v1/crews/{crewId}/chat/messages/{messageId}
```

**권한**: 작성자 또는 크루장만 가능

**Response** (200 OK)

#### 5.8 알림 설정 조회
```
GET /v1/crews/{crewId}/chat/notification-settings
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "알림 설정을 조회했습니다.",
  "data": {
    "crewId": 1,
    "userId": 123,
    "isEnabled": true,
    "notificationType": "ALL",
    "isMuted": false
  }
}
```

**알림 타입**:
- `ALL`: 모든 메시지 알림
- `MENTIONS`: 멘션된 메시지만 알림
- `NONE`: 알림 없음

#### 5.9 알림 설정 업데이트
```
PUT /v1/crews/{crewId}/chat/notification-settings
```

**Request Body**:
```json
{
  "isEnabled": true,
  "notificationType": "ALL",
  "isMuted": false
}
```

**Response** (200 OK): 업데이트된 알림 설정

---

## 기능별 상세 설명

### 1. 크루 생성 및 관리

**흐름**:
```
1. 사용자가 크루 생성 요청
   ↓
2. CrewService.createCrew()
   - CrewEntity 생성 (owner 설정)
   - CrewMemberEntity 생성 (OWNER 역할)
   - currentMembers = 1
   ↓
3. 크루 및 멤버십 저장
```

**권한 체계**:
- **크루장 (OWNER)**: 모든 권한
  - 크루 정보 수정
  - 크루 삭제/비활성화
  - 가입 신청 승인/거부
  - 멤버 추방
  - 멤버 역할 변경
  - 권한 이양

- **일반 멤버 (MEMBER)**: 제한된 권한
  - 크루 정보 조회
  - 크루 탈퇴
  - 채팅 참여

### 2. 가입 신청 프로세스

**흐름**:
```
1. 사용자가 가입 신청
   ↓
2. 검증
   - 이미 멤버인가?
   - 대기 중인 신청이 있는가?
   - 크루가 정원 초과인가?
   - 크루가 활성화 상태인가?
   ↓
3. CrewJoinRequestEntity 생성 (PENDING 상태)
   ↓
4. 크루장이 승인/거부 결정
   ↓
5-a. 승인 시:
   - CrewMemberEntity 생성 (MEMBER 역할)
   - currentMembers 증가
   - 신청 상태 → APPROVED

5-b. 거부 시:
   - 신청 상태 → REJECTED
```

### 3. 크루 통계 업데이트

**자동 업데이트 시점**: 사용자가 러닝 완료할 때

**위치**: `RunningServiceImpl.java:163-173`

**흐름**:
```
1. 사용자가 러닝 완료
   ↓
2. RunningService.completeRunning()
   ↓
3. CrewStatisticsUpdater.updateCrewStatisticsIfMember()
   - 사용자가 크루 멤버인지 확인
   - 해당 월의 CrewStatisticsEntity 조회/생성
   - updateWithMemberRun() 호출
     * runCount++
     * totalDistance += 러닝 거리
     * avgPaceSeconds 재계산 (가중평균)
     * 이번 달 첫 러닝이면 activeMembers++
   ↓
4. Redis 캐시 갱신 (크루 랭킹)
   ↓
5. MVP 자동 갱신 (필요 시)
```

**통계 계산 로직** (`CrewStatisticsEntity.java:93-117`):
```java
public void updateWithMemberRun(BigDecimal memberDistance,
                                 BigDecimal memberPaceSeconds,
                                 boolean isNewActiveMember) {
    // 러닝 횟수 증가
    this.runCount++;

    // 총 거리 누적
    this.totalDistance = this.totalDistance.add(memberDistance);

    // 평균 페이스 재계산 (거리 기반 가중평균)
    if (this.avgPaceSeconds == null) {
        this.avgPaceSeconds = memberPaceSeconds;
    } else {
        BigDecimal totalWeightedPace =
            this.avgPaceSeconds.multiply(this.totalDistance.subtract(memberDistance));
        BigDecimal newWeightedPace =
            memberPaceSeconds.multiply(memberDistance);
        this.avgPaceSeconds = totalWeightedPace.add(newWeightedPace)
                .divide(this.totalDistance, 2, BigDecimal.ROUND_HALF_UP);
    }

    // 새로운 활성 멤버 카운트
    if (isNewActiveMember) {
        this.activeMembers++;
    }
}
```

### 4. 크루 랭킹 시스템

**랭킹 기준**:
1. **거리 랭킹**: totalDistance 내림차순
2. **러닝 횟수 랭킹**: runCount 내림차순
3. **성장률 랭킹**: (현재 월 거리 - 전월 거리) / 전월 거리

**MVP 선정**:
- 해당 월에 가장 많은 거리를 뛴 멤버
- 자동 업데이트: 러닝 완료 시 재계산
- 수동 갱신: `/v1/crews/statistics/{crewId}/mvp/refresh`

### 5. 채팅 읽음 상태 추적

**구조**:
```
CrewChatEntity (메시지)
  ↓ 1:N
CrewChatReadStatusEntity (읽음 상태)
  - message: CrewChatEntity
  - reader: User
  - readAt: LocalDateTime
```

**읽음 처리 로직**:
1. 발신자는 항상 읽음으로 간주
2. CrewChatReadStatusEntity 생성으로 읽음 표시
3. 읽지 않은 수 = 크루 멤버 수 - 읽음 상태 수 - 1(발신자)

---

## 프론트엔드 구현 예시

### 1. 크루 목록 페이지 (React/TypeScript)

```typescript
import React, { useEffect, useState } from 'react';
import axios from 'axios';

interface Crew {
  id: number;
  name: string;
  description: string;
  maxMembers: number;
  currentMembers: number;
  profileImageUrl: string | null;
  ownerNickname: string;
  createdAt: string;
  canJoin: boolean | null;
}

const CrewListPage: React.FC = () => {
  const [crews, setCrews] = useState<Crew[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchCrews();
  }, [page]);

  const fetchCrews = async () => {
    setLoading(true);
    try {
      const response = await axios.get('/v1/crews', {
        params: {
          page,
          size: 20,
          sort: 'createdAt',
          direction: 'desc'
        }
      });
      setCrews(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (error) {
      console.error('크루 목록 조회 실패:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleJoinCrew = async (crewId: number) => {
    try {
      await axios.post(`/v1/crews/${crewId}/join-requests`, {
        message: '함께 러닝하고 싶습니다!'
      });
      alert('가입 신청이 완료되었습니다.');
      fetchCrews(); // 목록 갱신
    } catch (error) {
      console.error('가입 신청 실패:', error);
      alert('가입 신청에 실패했습니다.');
    }
  };

  if (loading) return <div>로딩 중...</div>;

  return (
    <div className="crew-list">
      <h1>크루 목록</h1>

      {crews.map((crew) => (
        <div key={crew.id} className="crew-card">
          {crew.profileImageUrl && (
            <img src={crew.profileImageUrl} alt={crew.name} />
          )}
          <h2>{crew.name}</h2>
          <p>{crew.description}</p>
          <p>
            멤버: {crew.currentMembers} / {crew.maxMembers}
          </p>
          <p>크루장: {crew.ownerNickname}</p>

          {crew.canJoin && (
            <button onClick={() => handleJoinCrew(crew.id)}>
              가입 신청
            </button>
          )}
          {crew.canJoin === false && (
            <span className="badge">가입 불가</span>
          )}
        </div>
      ))}

      {/* 페이지네이션 */}
      <div className="pagination">
        <button
          disabled={page === 0}
          onClick={() => setPage(page - 1)}
        >
          이전
        </button>
        <span>
          {page + 1} / {totalPages}
        </span>
        <button
          disabled={page >= totalPages - 1}
          onClick={() => setPage(page + 1)}
        >
          다음
        </button>
      </div>
    </div>
  );
};

export default CrewListPage;
```

### 2. 크루 상세 페이지 (탭 구조)

```typescript
import React, { useEffect, useState } from 'react';
import axios from 'axios';

interface CrewDetail {
  id: number;
  name: string;
  description: string;
  maxMembers: number;
  currentMembers: number;
  profileImageUrl: string | null;
  isActive: boolean;
  ownerId: number;
  ownerNickname: string;
  createdAt: string;
  updatedAt: string;
}

interface CrewMember {
  id: number;
  userId: number;
  userNickname: string;
  userProfileImage: string | null;
  role: string;
  joinedAt: string;
  isActive: boolean;
  isOwner: boolean;
}

interface CrewStats {
  crewId: number;
  crewName: string;
  month: string;
  runCount: number;
  totalDistance: number;
  activeMembers: number;
  avgPaceFormatted: string;
  mvpNickname: string | null;
  mvpDistance: number | null;
}

type TabType = 'info' | 'members' | 'stats' | 'chat';

const CrewDetailPage: React.FC<{ crewId: number; currentUserId: number }> = ({
  crewId,
  currentUserId
}) => {
  const [crew, setCrew] = useState<CrewDetail | null>(null);
  const [members, setMembers] = useState<CrewMember[]>([]);
  const [stats, setStats] = useState<CrewStats | null>(null);
  const [activeTab, setActiveTab] = useState<TabType>('info');
  const [isOwner, setIsOwner] = useState(false);

  useEffect(() => {
    fetchCrewDetail();
  }, [crewId]);

  useEffect(() => {
    if (activeTab === 'members') {
      fetchMembers();
    } else if (activeTab === 'stats') {
      fetchStats();
    }
  }, [activeTab]);

  const fetchCrewDetail = async () => {
    try {
      const response = await axios.get(`/v1/crews/${crewId}`);
      setCrew(response.data);
      setIsOwner(response.data.ownerId === currentUserId);
    } catch (error) {
      console.error('크루 상세 조회 실패:', error);
    }
  };

  const fetchMembers = async () => {
    try {
      const response = await axios.get(`/v1/crews/${crewId}/members/all`);
      setMembers(response.data);
    } catch (error) {
      console.error('멤버 목록 조회 실패:', error);
    }
  };

  const fetchStats = async () => {
    try {
      const response = await axios.get(
        `/v1/crews/statistics/${crewId}/monthly`
      );
      setStats(response.data);
    } catch (error) {
      console.error('통계 조회 실패:', error);
    }
  };

  const handleLeaveCrew = async () => {
    if (!confirm('정말 탈퇴하시겠습니까?')) return;

    try {
      await axios.delete(`/v1/crews/${crewId}/members/leave`);
      alert('크루에서 탈퇴했습니다.');
      window.location.href = '/crews';
    } catch (error) {
      console.error('크루 탈퇴 실패:', error);
      alert('탈퇴에 실패했습니다.');
    }
  };

  const handleRemoveMember = async (userId: number) => {
    if (!confirm('정말 추방하시겠습니까?')) return;

    try {
      await axios.delete(`/v1/crews/${crewId}/members/${userId}`);
      alert('멤버를 추방했습니다.');
      fetchMembers(); // 목록 갱신
    } catch (error) {
      console.error('멤버 추방 실패:', error);
      alert('추방에 실패했습니다.');
    }
  };

  if (!crew) return <div>로딩 중...</div>;

  return (
    <div className="crew-detail">
      {/* 크루 헤더 */}
      <div className="crew-header">
        {crew.profileImageUrl && (
          <img src={crew.profileImageUrl} alt={crew.name} />
        )}
        <h1>{crew.name}</h1>
        <p>{crew.description}</p>
        <p>
          멤버: {crew.currentMembers} / {crew.maxMembers}
        </p>
        <p>크루장: {crew.ownerNickname}</p>

        {!isOwner && (
          <button onClick={handleLeaveCrew}>크루 탈퇴</button>
        )}
      </div>

      {/* 탭 메뉴 */}
      <div className="tab-menu">
        <button
          className={activeTab === 'info' ? 'active' : ''}
          onClick={() => setActiveTab('info')}
        >
          정보
        </button>
        <button
          className={activeTab === 'members' ? 'active' : ''}
          onClick={() => setActiveTab('members')}
        >
          멤버
        </button>
        <button
          className={activeTab === 'stats' ? 'active' : ''}
          onClick={() => setActiveTab('stats')}
        >
          통계
        </button>
        <button
          className={activeTab === 'chat' ? 'active' : ''}
          onClick={() => setActiveTab('chat')}
        >
          채팅
        </button>
      </div>

      {/* 탭 컨텐츠 */}
      <div className="tab-content">
        {activeTab === 'info' && (
          <div>
            <h2>크루 정보</h2>
            <p>생성일: {new Date(crew.createdAt).toLocaleDateString()}</p>
            <p>상태: {crew.isActive ? '활성' : '비활성'}</p>
          </div>
        )}

        {activeTab === 'members' && (
          <div>
            <h2>멤버 목록 ({members.length}명)</h2>
            {members.map((member) => (
              <div key={member.id} className="member-item">
                {member.userProfileImage && (
                  <img src={member.userProfileImage} alt={member.userNickname} />
                )}
                <span>{member.userNickname}</span>
                <span className="role-badge">
                  {member.isOwner ? '크루장' : '멤버'}
                </span>
                <span>가입: {new Date(member.joinedAt).toLocaleDateString()}</span>

                {isOwner && !member.isOwner && (
                  <button onClick={() => handleRemoveMember(member.userId)}>
                    추방
                  </button>
                )}
              </div>
            ))}
          </div>
        )}

        {activeTab === 'stats' && stats && (
          <div>
            <h2>{stats.month.substring(0, 4)}년 {stats.month.substring(4)}월 통계</h2>
            <div className="stats-grid">
              <div className="stat-item">
                <h3>총 거리</h3>
                <p>{stats.totalDistance.toFixed(2)} km</p>
              </div>
              <div className="stat-item">
                <h3>러닝 횟수</h3>
                <p>{stats.runCount}회</p>
              </div>
              <div className="stat-item">
                <h3>활성 멤버</h3>
                <p>{stats.activeMembers}명</p>
              </div>
              <div className="stat-item">
                <h3>평균 페이스</h3>
                <p>{stats.avgPaceFormatted} /km</p>
              </div>
              {stats.mvpNickname && (
                <div className="stat-item mvp">
                  <h3>이달의 MVP</h3>
                  <p>{stats.mvpNickname}</p>
                  <p>{stats.mvpDistance?.toFixed(2)} km</p>
                </div>
              )}
            </div>
          </div>
        )}

        {activeTab === 'chat' && (
          <div>
            <h2>채팅</h2>
            {/* 채팅 컴포넌트 */}
          </div>
        )}
      </div>
    </div>
  );
};

export default CrewDetailPage;
```

### 3. 가입 신청 관리 (크루장용)

```typescript
import React, { useEffect, useState } from 'react';
import axios from 'axios';

interface JoinRequest {
  id: number;
  crewId: number;
  crewName: string;
  userId: number;
  userNickname: string;
  message: string;
  status: string;
  createdAt: string;
  processedAt: string | null;
  processingNote: string | null;
}

const JoinRequestManagementPage: React.FC<{ crewId: number }> = ({ crewId }) => {
  const [requests, setRequests] = useState<JoinRequest[]>([]);
  const [filter, setFilter] = useState<string>('PENDING');

  useEffect(() => {
    fetchJoinRequests();
  }, [filter]);

  const fetchJoinRequests = async () => {
    try {
      const response = await axios.get(
        `/v1/crews/${crewId}/join-requests`,
        {
          params: {
            status: filter,
            page: 0,
            size: 20
          }
        }
      );
      setRequests(response.data.content);
    } catch (error) {
      console.error('가입 신청 조회 실패:', error);
    }
  };

  const handleApprove = async (requestId: number) => {
    try {
      await axios.post(
        `/v1/crews/join-requests/${requestId}/approve`,
        { note: '환영합니다!' }
      );
      alert('가입 신청을 승인했습니다.');
      fetchJoinRequests();
    } catch (error) {
      console.error('승인 실패:', error);
      alert('승인에 실패했습니다.');
    }
  };

  const handleReject = async (requestId: number) => {
    const note = prompt('거부 사유를 입력하세요:');
    if (!note) return;

    try {
      await axios.post(
        `/v1/crews/join-requests/${requestId}/reject`,
        { note }
      );
      alert('가입 신청을 거부했습니다.');
      fetchJoinRequests();
    } catch (error) {
      console.error('거부 실패:', error);
      alert('거부에 실패했습니다.');
    }
  };

  return (
    <div className="join-request-management">
      <h1>가입 신청 관리</h1>

      {/* 필터 */}
      <div className="filter">
        <button
          className={filter === 'PENDING' ? 'active' : ''}
          onClick={() => setFilter('PENDING')}
        >
          대기중
        </button>
        <button
          className={filter === 'APPROVED' ? 'active' : ''}
          onClick={() => setFilter('APPROVED')}
        >
          승인됨
        </button>
        <button
          className={filter === 'REJECTED' ? 'active' : ''}
          onClick={() => setFilter('REJECTED')}
        >
          거부됨
        </button>
      </div>

      {/* 신청 목록 */}
      <div className="request-list">
        {requests.length === 0 ? (
          <p>가입 신청이 없습니다.</p>
        ) : (
          requests.map((request) => (
            <div key={request.id} className="request-item">
              <div className="request-info">
                <h3>{request.userNickname}</h3>
                <p>{request.message}</p>
                <p className="date">
                  {new Date(request.createdAt).toLocaleString()}
                </p>
              </div>

              {request.status === 'PENDING' && (
                <div className="request-actions">
                  <button
                    className="approve"
                    onClick={() => handleApprove(request.id)}
                  >
                    승인
                  </button>
                  <button
                    className="reject"
                    onClick={() => handleReject(request.id)}
                  >
                    거부
                  </button>
                </div>
              )}

              {request.status !== 'PENDING' && (
                <div className="request-result">
                  <span className={`status ${request.status.toLowerCase()}`}>
                    {request.status === 'APPROVED' && '승인됨'}
                    {request.status === 'REJECTED' && '거부됨'}
                  </span>
                  {request.processingNote && (
                    <p className="note">{request.processingNote}</p>
                  )}
                </div>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default JoinRequestManagementPage;
```

---

## 주요 비즈니스 로직

### 1. 크루 정원 관리

**낙관적 잠금 (Optimistic Locking)**:
```java
@Entity
public class CrewEntity {
    @Version
    private Long version;  // JPA가 자동으로 동시성 제어
}
```

**정원 초과 방지**:
```java
// 가입 신청 승인 시
if (crew.isFull()) {
    throw new CrewFullException("크루 정원이 초과되었습니다.");
}
```

### 2. 통계 동시성 제어

**위치**: `CrewStatisticsUpdater.java`

**재시도 로직**:
```java
@Retryable(
    value = {OptimisticLockingFailureException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 100)
)
public void updateCrewStatisticsIfMember(Long userId, Double distanceKm, Integer durationSeconds) {
    // 통계 업데이트 로직
}
```

**특징**:
- 낙관적 잠금 실패 시 최대 3회 재시도
- 100ms 간격으로 재시도

### 3. Redis 캐시 활용

**크루 랭킹 캐시**:
- 키: `crew:ranking:distance:{month}`
- 값: 거리 기준 정렬된 크루 목록
- TTL: 1시간
- 갱신: 러닝 완료 시 자동

**목적**:
- 랭킹 조회 성능 향상
- DB 부하 감소

### 4. 채팅 알림 설정

**알림 타입**:
- `ALL`: 모든 메시지 알림
- `MENTIONS`: 멘션(@사용자명)된 메시지만 알림
- `NONE`: 알림 없음

**무음 모드 (isMuted)**:
- 알림은 받지만 소리/진동 없음

---

## 참고 파일 위치

### Backend 파일

**Entity**:
- `src/main/java/com/waytoearth/entity/crew/CrewEntity.java`
- `src/main/java/com/waytoearth/entity/crew/CrewMemberEntity.java`
- `src/main/java/com/waytoearth/entity/crew/CrewJoinRequestEntity.java`
- `src/main/java/com/waytoearth/entity/crew/CrewStatisticsEntity.java`
- `src/main/java/com/waytoearth/entity/crew/CrewChatEntity.java`
- `src/main/java/com/waytoearth/entity/enums/CrewRole.java`

**Repository**:
- `src/main/java/com/waytoearth/repository/crew/CrewRepository.java`
- `src/main/java/com/waytoearth/repository/crew/CrewMemberRepository.java`
- `src/main/java/com/waytoearth/repository/crew/CrewJoinRequestRepository.java`
- `src/main/java/com/waytoearth/repository/crew/CrewStatisticsRepository.java`
- `src/main/java/com/waytoearth/repository/crew/CrewChatRepository.java`

**Service**:
- `src/main/java/com/waytoearth/service/crew/CrewService.java`
- `src/main/java/com/waytoearth/service/crew/CrewServiceImpl.java`
- `src/main/java/com/waytoearth/service/crew/CrewJoinService.java`
- `src/main/java/com/waytoearth/service/crew/CrewJoinServiceImpl.java`
- `src/main/java/com/waytoearth/service/crew/CrewMemberService.java`
- `src/main/java/com/waytoearth/service/crew/CrewMemberServiceImpl.java`
- `src/main/java/com/waytoearth/service/crew/CrewStatisticsService.java`
- `src/main/java/com/waytoearth/service/crew/CrewStatisticsServiceImpl.java`
- `src/main/java/com/waytoearth/service/crew/CrewStatisticsUpdater.java`
- `src/main/java/com/waytoearth/service/crew/CrewChatService.java`
- `src/main/java/com/waytoearth/service/crew/CrewChatServiceImpl.java`

**Controller**:
- `src/main/java/com/waytoearth/controller/v1/crew/CrewController.java`
- `src/main/java/com/waytoearth/controller/v1/crew/CrewJoinController.java`
- `src/main/java/com/waytoearth/controller/v1/crew/CrewMemberController.java`
- `src/main/java/com/waytoearth/controller/v1/crew/CrewStatisticsController.java`
- `src/main/java/com/waytoearth/controller/v1/crew/CrewChatController.java`

**DTO**:
- Request: `src/main/java/com/waytoearth/dto/request/crew/`
- Response: `src/main/java/com/waytoearth/dto/response/crew/`

---

## 결론

크루 기능은 다음과 같은 완전한 소셜 러닝 시스템을 제공합니다:

1. **크루 관리**: 생성, 조회, 수정, 삭제, 검색
2. **멤버 관리**: 가입 신청/승인/거부, 멤버 추방/탈퇴, 역할 변경, 권한 이양
3. **통계 및 랭킹**: 월별 통계, 거리/횟수/성장률 랭킹, MVP 선정
4. **채팅**: 실시간 메시징, 읽음 상태 추적, 알림 설정
5. **동시성 제어**: 낙관적 잠금, 재시도 로직, Redis 캐싱

프론트엔드에서는 제공된 API를 활용하여 크루 목록, 상세 페이지, 멤버 관리, 통계 대시보드, 채팅 등 다양한 UI를 구현할 수 있습니다.
