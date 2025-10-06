# Spring Boot REST API의 완벽한 데이터 흐름: DTO → Controller → Service → Repository → Response 완전 정복

> **"크루 생성 API로 배우는 실전 아키텍처"**
> Request DTO부터 Response DTO까지, 프로덕션 레벨 코드로 이해하는 레이어드 아키텍처의 모든 것

---

## 📌 들어가며

Spring Boot로 REST API를 개발할 때 가장 많이 듣는 질문이 있습니다.

- "DTO는 왜 필요한가요?"
- "Controller와 Service의 역할 구분이 애매해요"
- "Entity를 그냥 반환하면 안 되나요?"
- "Request DTO와 Response DTO를 왜 따로 만들죠?"

이 글에서는 **실제 프로덕션 코드**를 기반으로, **크루 생성(Crew Creation)** 기능을 처음부터 끝까지 따라가며 각 레이어의 역할과 데이터 흐름을 완벽하게 이해해보겠습니다.

### 이 글에서 다룰 내용

- ✅ Request DTO의 역할과 유효성 검증
- ✅ Controller의 책임과 경계
- ✅ Service의 비즈니스 로직 처리
- ✅ Entity의 역할과 JPA 매핑
- ✅ Repository의 데이터 접근 계층
- ✅ Response DTO로 안전하게 데이터 반환하기
- ✅ 전체 데이터 흐름의 시각화

### 실습 환경

- **프로젝트**: WayToEarth (러닝 크루 관리 플랫폼)
- **기술 스택**: Spring Boot 3.x, JPA, Lombok, Validation
- **도메인**: 크루(Crew) 생성 API

---

## 🏗️ 레이어드 아키텍처 개요

Spring Boot의 전형적인 **3-Tier Architecture**는 다음과 같습니다:

```
┌─────────────────────────────────────────────────┐
│              Presentation Layer                  │
│        (Controller + Request/Response DTO)       │
│  역할: 클라이언트 요청/응답 처리, 데이터 변환      │
└─────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────┐
│               Business Layer                     │
│                  (Service)                       │
│  역할: 비즈니스 로직, 트랜잭션 관리, Entity 처리   │
└─────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────┐
│              Persistence Layer                   │
│            (Repository + Entity)                 │
│  역할: 데이터베이스 접근, CRUD 작업               │
└─────────────────────────────────────────────────┘
```

### 각 레이어의 핵심 원칙

| 레이어 | 사용 객체 | 절대 하지 말아야 할 것 |
|--------|-----------|------------------------|
| **Controller** | DTO | Entity를 직접 반환하거나 받지 않기 |
| **Service** | Entity | HTTP 관련 코드 작성하지 않기 |
| **Repository** | Entity | 비즈니스 로직 넣지 않기 |

---

## 📥 1단계: Request DTO - 클라이언트 요청 받기

### Request DTO란?

**Request DTO(Data Transfer Object)**는 클라이언트가 보낸 데이터를 안전하게 받기 위한 전용 객체입니다.

### 왜 Entity를 직접 받으면 안 될까요?

```java
// ❌ 나쁜 예: Entity를 직접 받는 경우
@PostMapping("/crews")
public ResponseEntity<CrewEntity> createCrew(@RequestBody CrewEntity crew) {
    // 문제 1: 클라이언트가 id, createdAt 등 민감한 필드를 조작할 수 있음
    // 문제 2: 불필요한 필드까지 모두 노출됨
    // 문제 3: Entity 구조 변경 시 API 스펙도 변경됨
}

// ✅ 좋은 예: Request DTO 사용
@PostMapping("/crews")
public ResponseEntity<CrewDetailResponse> createCrew(
    @Valid @RequestBody CrewCreateRequest request) {
    // 필요한 필드만 받음
    // 유효성 검증 자동화
    // API 스펙과 도메인 모델 분리
}
```

### CrewCreateRequest.java - 실전 코드

```java
package com.waytoearth.dto.request.crew;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "크루 생성 요청")
public class CrewCreateRequest {

    @NotBlank(message = "크루 이름은 필수입니다.")
    @Size(max = 50, message = "크루 이름은 50자를 초과할 수 없습니다.")
    @Schema(description = "크루 이름", example = "서울 러닝 크루", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 500, message = "크루 소개는 500자를 초과할 수 없습니다.")
    @Schema(description = "크루 소개", example = "함께 달리며 건강한 라이프스타일을 추구하는 크루입니다")
    private String description;

    @Min(value = 2, message = "최소 2명 이상이어야 합니다.")
    @Max(value = 100, message = "최대 100명까지 가능합니다.")
    @Schema(description = "최대 인원", example = "20")
    private Integer maxMembers = 50;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/crew-profile.jpg")
    private String profileImageUrl;
}
```

### 핵심 포인트

**1. `@NotBlank` - 필수 필드 검증**
```java
@NotBlank(message = "크루 이름은 필수입니다.")
private String name;
```
- `null`, 빈 문자열(`""`), 공백(`"   "`) 모두 거부
- 검증 실패 시 자동으로 400 Bad Request 응답

**2. `@Size` - 길이 제한**
```java
@Size(max = 50, message = "크루 이름은 50자를 초과할 수 없습니다.")
private String name;
```
- DB 컬럼 크기와 일치시켜 데이터 정합성 유지
- SQL Injection 공격 위험 감소

**3. `@Min`, `@Max` - 범위 검증**
```java
@Min(value = 2, message = "최소 2명 이상이어야 합니다.")
@Max(value = 100, message = "최대 100명까지 가능합니다.")
private Integer maxMembers = 50;
```
- 비즈니스 규칙을 DTO 레벨에서 강제
- 잘못된 데이터가 Service까지 내려가는 것 방지

**4. 기본값 설정**
```java
private Integer maxMembers = 50;  // 클라이언트가 안 보내면 50으로 자동 설정
```

### 클라이언트가 보내는 JSON 예시

```json
POST /v1/crews
Content-Type: application/json

{
  "name": "서울 러닝 크루",
  "description": "함께 달리며 건강한 라이프스타일을 추구하는 크루입니다",
  "maxMembers": 20,
  "profileImageUrl": "https://cdn.waytoearth.com/crew/profile123.jpg"
}
```

이 JSON이 자동으로 `CrewCreateRequest` 객체로 변환되고, 검증 어노테이션이 실행됩니다!

---

## 🎛️ 2단계: Controller - 요청 수신 및 응답 반환

### Controller의 역할

Controller는 **Presentation Layer**의 핵심으로, 다음 책임만 가집니다:

1. **HTTP 요청 받기** (URL, Method, Header, Body)
2. **Request DTO 유효성 검증** (`@Valid`)
3. **Service 호출** (비즈니스 로직 위임)
4. **Entity → Response DTO 변환**
5. **HTTP 응답 반환** (Status Code, Body)

### Controller가 해서는 안 되는 것 ❌

- ❌ 비즈니스 로직 작성 (ex: `if (crew.getCurrentMembers() > maxMembers) throw...`)
- ❌ DB 직접 접근 (`crewRepository.save(...)` 금지)
- ❌ 복잡한 연산 (통계 계산, 집계 등)
- ❌ Entity 직접 반환

### CrewController.java - 크루 생성 API

```java
package com.waytoearth.controller.v1.crew;

import com.waytoearth.dto.request.crew.CrewCreateRequest;
import com.waytoearth.dto.response.crew.CrewDetailResponse;
import com.waytoearth.entity.crew.CrewEntity;
import com.waytoearth.security.AuthenticatedUser;
import com.waytoearth.service.crew.CrewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/crews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Crew Management", description = "크루 기본 관리 API")
public class CrewController {

    private final CrewService crewService;

    @Operation(summary = "크루 생성", description = "새로운 크루를 생성합니다. 생성자가 자동으로 크루장이 됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "크루 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping
    public ResponseEntity<CrewDetailResponse> createCrew(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CrewCreateRequest request) {

        // 1️⃣ 로깅: 요청 추적을 위한 로그
        log.info("크루 생성 요청 - userId: {}, name: {}", user.getUserId(), request.getName());

        // 2️⃣ Service 호출: 비즈니스 로직 위임
        CrewEntity crew = crewService.createCrew(
                user,
                request.getName(),
                request.getDescription(),
                request.getMaxMembers(),
                request.getProfileImageUrl()
        );

        // 3️⃣ Entity → Response DTO 변환
        // Controller에서 DTO 변환하는 것이 일반적!
        CrewDetailResponse response = CrewDetailResponse.from(crew);

        // 4️⃣ HTTP 201 Created 응답 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

### 코드 상세 분석

**1. `@Valid` - 자동 유효성 검증**
```java
@Valid @RequestBody CrewCreateRequest request
```
- Spring이 자동으로 `CrewCreateRequest`의 검증 어노테이션 실행
- 검증 실패 시 `MethodArgumentNotValidException` 발생
- `@ControllerAdvice`로 전역 예외 처리 가능

**2. `@AuthenticationPrincipal` - 인증된 사용자 정보**
```java
@AuthenticationPrincipal AuthenticatedUser user
```
- JWT 토큰에서 추출한 사용자 정보
- `user.getUserId()`로 현재 로그인한 사용자 ID 획득

**3. Service 메소드 호출**
```java
CrewEntity crew = crewService.createCrew(
    user,
    request.getName(),          // DTO에서 값을 꺼내서 전달
    request.getDescription(),
    request.getMaxMembers(),
    request.getProfileImageUrl()
);
```
- **중요**: DTO 객체 자체를 넘기지 않고, 필요한 값만 추출해서 전달
- Service는 DTO를 몰라야 함 (의존성 역전)

**4. Entity → DTO 변환**
```java
CrewDetailResponse response = CrewDetailResponse.from(crew);
```
- **핵심**: Entity를 그대로 반환하지 않고 Response DTO로 변환
- 민감한 정보 제거, 필요한 필드만 노출

**5. HTTP 상태 코드**
```java
return ResponseEntity.status(HttpStatus.CREATED).body(response);
```
- `201 Created`: 리소스 생성 성공
- REST API 스펙에 맞는 적절한 상태 코드 사용

---

## 💼 3단계: Service - 비즈니스 로직 처리

### Service Layer의 역할

Service는 **Business Layer**의 핵심으로, 애플리케이션의 실제 비즈니스 로직을 담당합니다.

### Service의 책임

1. **비즈니스 로직 구현** (크루 생성 규칙, 권한 체크 등)
2. **트랜잭션 관리** (`@Transactional`)
3. **Entity 생성 및 조작**
4. **Repository 호출** (데이터 영속화)
5. **도메인 이벤트 발행** (필요 시)

### CrewServiceImpl.java - 크루 생성 로직

```java
package com.waytoearth.service.crew;

import com.waytoearth.entity.crew.CrewEntity;
import com.waytoearth.entity.crew.CrewMemberEntity;
import com.waytoearth.entity.user.User;
import com.waytoearth.exception.UserNotFoundException;
import com.waytoearth.repository.crew.CrewRepository;
import com.waytoearth.repository.crew.CrewMemberRepository;
import com.waytoearth.repository.user.UserRepository;
import com.waytoearth.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 기본은 읽기 전용
@Slf4j
public class CrewServiceImpl implements CrewService {

    private final CrewRepository crewRepository;
    private final CrewMemberRepository crewMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional  // 쓰기 작업이므로 readOnly = false
    public CrewEntity createCrew(
            AuthenticatedUser user,
            String name,
            String description,
            Integer maxMembers,
            String profileImageUrl) {

        // 1️⃣ 사용자 존재 확인
        User owner = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new UserNotFoundException(user.getUserId()));

        // 2️⃣ Entity 생성 (Builder 패턴)
        CrewEntity crew = CrewEntity.builder()
                .name(name)
                .description(description)
                .maxMembers(maxMembers != null ? maxMembers : 50)  // null 처리
                .profileImageUrl(profileImageUrl)
                .owner(owner)
                .isActive(true)
                .build();

        // 3️⃣ 크루 저장 (Repository 호출)
        CrewEntity savedCrew = crewRepository.save(crew);

        // 4️⃣ 크루장을 멤버로 자동 추가 (비즈니스 로직!)
        CrewMemberEntity ownerMember = CrewMemberEntity.createOwner(savedCrew, owner);
        crewMemberRepository.save(ownerMember);

        // 5️⃣ 멤버 수 증가
        savedCrew.incrementMemberCount();

        // 6️⃣ 로깅
        log.info("크루가 생성되었습니다. crewId: {}, ownerId: {}", savedCrew.getId(), user.getUserId());

        // 7️⃣ Entity 반환
        return savedCrew;
    }
}
```

### 코드 상세 분석

**1. `@Transactional` - 트랜잭션 관리**
```java
@Transactional(readOnly = true)  // 클래스 레벨: 기본 읽기 전용
public class CrewServiceImpl implements CrewService {

    @Transactional  // 메소드 레벨: 쓰기 가능 (오버라이드)
    public CrewEntity createCrew(...) {
```
- **왜 필요한가?**
  - 여러 DB 작업을 하나의 단위로 묶음
  - 중간에 에러 발생 시 전체 롤백
  - 예: 크루 저장 성공했지만 멤버 추가 실패 → 둘 다 취소

**2. 사용자 조회**
```java
User owner = userRepository.findById(user.getUserId())
        .orElseThrow(() -> new UserNotFoundException(user.getUserId()));
```
- `Optional` 사용으로 안전한 null 처리
- 존재하지 않는 사용자면 바로 예외 발생 → 400 Bad Request

**3. Entity 생성 - Builder 패턴**
```java
CrewEntity crew = CrewEntity.builder()
        .name(name)
        .description(description)
        .maxMembers(maxMembers != null ? maxMembers : 50)
        .owner(owner)
        .isActive(true)
        .build();
```
- **장점**:
  - 가독성 좋음 (어떤 필드에 어떤 값이 들어가는지 명확)
  - 불변성 유지 가능
  - null 체크 로직 통합

**4. 비즈니스 로직 - 크루장 자동 추가**
```java
CrewMemberEntity ownerMember = CrewMemberEntity.createOwner(savedCrew, owner);
crewMemberRepository.save(ownerMember);
```
- **비즈니스 규칙**: "크루를 만든 사람은 자동으로 크루장이 된다"
- 이런 로직은 Service에 있어야 함!
- Controller나 Entity에 있으면 안 됨

**5. 도메인 메소드 활용**
```java
savedCrew.incrementMemberCount();  // Entity의 비즈니스 메소드
```
- `currentMembers++`를 직접 하지 않고 메소드로 캡슐화
- Entity가 자신의 상태를 스스로 관리

**6. 반환값은 Entity**
```java
return savedCrew;  // DTO가 아닌 Entity 반환
```
- Service는 DTO를 모름
- Controller가 Entity → DTO 변환 담당

---

## 🗄️ 4단계: Entity - 도메인 모델

### Entity란?

**Entity**는 데이터베이스 테이블과 1:1로 매핑되는 도메인 객체입니다.

### Entity의 특징

- JPA `@Entity` 어노테이션으로 선언
- 데이터베이스 스키마와 일치
- 비즈니스 로직 포함 가능 (도메인 주도 설계)
- 절대 외부로 직접 노출하지 않기

### CrewEntity.java - 크루 도메인 모델

```java
package com.waytoearth.entity.crew;

import com.waytoearth.entity.common.BaseTimeEntity;
import com.waytoearth.entity.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "crews",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"name"}, name = "uk_crew_name")
       })
@org.hibernate.annotations.Check(constraints =
    "max_members > 0 AND max_members <= 1000 AND current_members >= 0 AND current_members <= max_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "크루 엔티티")
public class CrewEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxMembers = 50;

    private String profileImageUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false)
    @Builder.Default
    private Integer currentMembers = 0;

    @Version  // 낙관적 락
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "crew", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CrewMemberEntity> members = new ArrayList<>();

    @OneToMany(mappedBy = "crew", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CrewJoinRequestEntity> joinRequests = new ArrayList<>();

    // ========== 비즈니스 메서드 ==========

    public boolean isFull() {
        return members.size() >= maxMembers;
    }

    public boolean isOwner(User user) {
        return owner.equals(user);
    }

    public int getCurrentMemberCount() {
        return (int) members.stream()
                .filter(member -> member.getIsActive())
                .count();
    }

    public void incrementMemberCount() {
        this.currentMembers++;
    }

    public void decrementMemberCount() {
        this.currentMembers--;
    }
}
```

### 코드 상세 분석

**1. `@Table` - 테이블 매핑 및 제약조건**
```java
@Table(name = "crews",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"name"}, name = "uk_crew_name")
       })
```
- 크루 이름은 유니크해야 함 (DB 레벨 제약)
- 중복 크루명 생성 시도 → `DataIntegrityViolationException` 발생

**2. `@Check` - DB 체크 제약조건**
```java
@org.hibernate.annotations.Check(constraints =
    "max_members > 0 AND max_members <= 1000 AND current_members >= 0 AND current_members <= max_members")
```
- 최대 인원은 1~1000명
- 현재 인원은 항상 최대 인원 이하
- 데이터 정합성을 DB 레벨에서 보장

**3. `@GeneratedValue` - 기본키 생성 전략**
```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```
- `IDENTITY`: MySQL의 AUTO_INCREMENT 사용
- DB가 자동으로 ID 생성

**4. `@Column` - 컬럼 상세 설정**
```java
@Column(nullable = false, length = 50)
private String name;
```
- `nullable = false`: NOT NULL 제약
- `length = 50`: VARCHAR(50)

**5. `@Version` - 낙관적 락**
```java
@Version
private Long version;
```
- 동시성 제어
- 두 사용자가 동시에 같은 크루 수정 시도 → 한 명만 성공

**6. `@ManyToOne` - 연관관계 매핑**
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "owner_id", nullable = false)
private User owner;
```
- 크루 N : 크루장 1
- `FetchType.LAZY`: 필요할 때만 조회 (N+1 문제 방지)

**7. `@OneToMany` - 양방향 연관관계**
```java
@OneToMany(mappedBy = "crew", cascade = CascadeType.ALL, orphanRemoval = true)
private List<CrewMemberEntity> members = new ArrayList<>();
```
- 크루 1 : 멤버 N
- `cascade = ALL`: 크루 삭제 시 멤버도 함께 삭제
- `orphanRemoval = true`: 관계 끊어진 멤버 자동 삭제

**8. 비즈니스 메소드**
```java
public boolean isFull() {
    return members.size() >= maxMembers;
}

public void incrementMemberCount() {
    this.currentMembers++;
}
```
- **Anemic Domain Model** ❌ vs **Rich Domain Model** ✅
- Entity가 자신의 비즈니스 로직을 갖는 것이 좋음
- `crew.incrementMemberCount()` > `crew.setCurrentMembers(crew.getCurrentMembers() + 1)`

---

## 💾 5단계: Repository - 데이터 접근 계층

### Repository의 역할

**Repository**는 **Persistence Layer**로, Entity의 영속성을 관리합니다.

### Spring Data JPA의 마법

```java
package com.waytoearth.repository.crew;

import com.waytoearth.entity.crew.CrewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CrewRepository extends JpaRepository<CrewEntity, Long> {

    // 메소드 이름으로 쿼리 자동 생성
    Optional<CrewEntity> findByName(String name);

    boolean existsByName(String name);

    // JPQL 사용
    @Query("SELECT c FROM CrewEntity c WHERE c.isActive = true")
    Page<CrewEntity> findAllActiveCrews(Pageable pageable);

    // N+1 문제 방지 (Fetch Join)
    @Query("SELECT c FROM CrewEntity c JOIN FETCH c.owner WHERE c.name LIKE %:name%")
    Page<CrewEntity> findByNameContainingWithOwner(@Param("name") String name, Pageable pageable);
}
```

### Repository 메소드 호출 과정

```java
// Service에서 호출
CrewEntity savedCrew = crewRepository.save(crew);
```

**실제 실행되는 SQL:**
```sql
INSERT INTO crews (
    name,
    description,
    max_members,
    profile_image_url,
    is_active,
    current_members,
    owner_id,
    created_at,
    updated_at
) VALUES (
    '서울 러닝 크루',
    '함께 달리는 크루',
    20,
    'https://...',
    true,
    0,
    123,
    '2024-10-06 20:00:00',
    '2024-10-06 20:00:00'
);
```

### JpaRepository가 제공하는 기본 메소드

```java
// 저장
CrewEntity save(CrewEntity entity)

// 조회
Optional<CrewEntity> findById(Long id)
List<CrewEntity> findAll()

// 삭제
void delete(CrewEntity entity)
void deleteById(Long id)

// 존재 여부
boolean existsById(Long id)

// 개수
long count()
```

### Custom Query 작성

**1. 메소드 이름 규칙**
```java
// 이름으로 찾기
Optional<CrewEntity> findByName(String name);
→ SELECT * FROM crews WHERE name = ?

// 이름 포함하고 활성화된 크루
List<CrewEntity> findByNameContainingAndIsActiveTrue(String keyword);
→ SELECT * FROM crews WHERE name LIKE %?% AND is_active = true

// 최대 인원 이상인 크루
List<CrewEntity> findByMaxMembersGreaterThanEqual(Integer minMembers);
→ SELECT * FROM crews WHERE max_members >= ?
```

**2. `@Query` 사용**
```java
@Query("SELECT c FROM CrewEntity c JOIN FETCH c.owner WHERE c.name LIKE %:name%")
Page<CrewEntity> findByNameContainingWithOwner(@Param("name") String name, Pageable pageable);
```
- **Fetch Join**으로 N+1 문제 해결
- 크루 조회 시 owner도 한 번에 가져옴

---

## 📤 6단계: Response DTO - 안전한 응답 반환

### Response DTO가 필요한 이유

**Entity를 직접 반환하면 생기는 문제:**

```java
// ❌ Entity 직접 반환
@GetMapping("/crews/{id}")
public CrewEntity getCrew(@PathVariable Long id) {
    return crewService.getCrewById(id);
}
```

**문제점:**
1. **순환 참조** - `Crew → Owner → Crew → Owner → ...` 무한 루프
2. **민감 정보 노출** - 패스워드, 내부 ID, 버전 등
3. **불필요한 데이터** - 클라이언트가 필요하지 않은 모든 필드 전송
4. **API 스펙 고정** - Entity 구조 변경 시 API도 변경됨

### CrewDetailResponse.java - 응답 DTO

```java
package com.waytoearth.dto.response.crew;

import com.waytoearth.entity.crew.CrewEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "크루 상세 정보 응답")
public class CrewDetailResponse {

    @Schema(description = "크루 ID", example = "1")
    private Long id;

    @Schema(description = "크루 이름", example = "서울 러닝 크루")
    private String name;

    @Schema(description = "크루 소개", example = "함께 달리며 건강한 라이프스타일을 추구하는 크루입니다")
    private String description;

    @Schema(description = "최대 인원", example = "20")
    private Integer maxMembers;

    @Schema(description = "현재 멤버 수", example = "10")
    private Integer currentMembers;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/crew-profile.jpg")
    private String profileImageUrl;

    @Schema(description = "활성화 상태", example = "true")
    private Boolean isActive;

    @Schema(description = "크루장 ID", example = "123")
    private Long ownerId;

    @Schema(description = "크루장 닉네임", example = "김러너")
    private String ownerNickname;

    @Schema(description = "생성일", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;

    /**
     * Entity → DTO 변환 (정적 팩토리 메소드)
     */
    public static CrewDetailResponse from(CrewEntity crew) {
        return new CrewDetailResponse(
                crew.getId(),
                crew.getName(),
                crew.getDescription(),
                crew.getMaxMembers(),
                crew.getCurrentMembers(),
                crew.getProfileImageUrl(),
                crew.getIsActive(),
                crew.getOwner().getId(),               // User Entity에서 필요한 것만 추출
                crew.getOwner().getNickname(),         // User 전체를 노출하지 않음
                crew.getCreatedAt(),
                crew.getUpdatedAt()
        );
    }
}
```

### 핵심 패턴: 정적 팩토리 메소드

```java
public static CrewDetailResponse from(CrewEntity crew) {
    return new CrewDetailResponse(...);
}
```

**사용 예:**
```java
// Controller에서
CrewDetailResponse response = CrewDetailResponse.from(crew);
```

**장점:**
- 가독성: `new CrewDetailResponse(...)` 보다 의도가 명확
- 재사용성: 여러 곳에서 동일한 변환 로직 사용
- 유지보수: 변환 로직이 한 곳에만 있음

### 클라이언트가 받는 JSON

```json
HTTP/1.1 201 Created
Content-Type: application/json

{
  "id": 1,
  "name": "서울 러닝 크루",
  "description": "함께 달리며 건강한 라이프스타일을 추구하는 크루입니다",
  "maxMembers": 20,
  "currentMembers": 1,
  "profileImageUrl": "https://cdn.waytoearth.com/crew/profile123.jpg",
  "isActive": true,
  "ownerId": 123,
  "ownerNickname": "김러너",
  "createdAt": "2024-10-06T20:00:00",
  "updatedAt": "2024-10-06T20:00:00"
}
```

**포함되지 않는 정보:**
- ❌ `version` (낙관적 락용)
- ❌ `members` 리스트 (순환 참조 방지)
- ❌ `owner` 전체 객체 (민감 정보 제거)

---

## 🔄 전체 데이터 흐름 시각화

### 단계별 상세 흐름도

```
[클라이언트]
    │
    │ POST /v1/crews
    │ Content-Type: application/json
    │ Authorization: Bearer eyJhbGc...
    │
    │ {
    │   "name": "서울 러닝 크루",
    │   "description": "함께 달리는 크루",
    │   "maxMembers": 20,
    │   "profileImageUrl": "https://..."
    │ }
    ↓
┌─────────────────────────────────────────────────────────┐
│              🎛️ Controller Layer                        │
│                                                          │
│  @PostMapping                                           │
│  public ResponseEntity<CrewDetailResponse> createCrew(  │
│      @Valid @RequestBody CrewCreateRequest request) {   │
│                                                          │
│    // 1️⃣ Request DTO 수신 및 검증                       │
│    // - @NotBlank, @Size, @Min/@Max 자동 실행           │
│    // - 검증 실패 시 400 Bad Request                    │
│                                                          │
│    CrewCreateRequest {                                  │
│      name: "서울 러닝 크루",                             │
│      description: "함께 달리는 크루",                    │
│      maxMembers: 20,                                    │
│      profileImageUrl: "https://..."                     │
│    }                                                    │
└─────────────────────────────────────────────────────────┘
    │
    │ request.getName(), request.getDescription(), ...
    ↓
┌─────────────────────────────────────────────────────────┐
│              💼 Service Layer                            │
│                                                          │
│  @Transactional                                         │
│  public CrewEntity createCrew(                          │
│      String name, String description, ...) {            │
│                                                          │
│    // 2️⃣ 사용자 조회                                    │
│    User owner = userRepository.findById(userId)         │
│                                                          │
│    // 3️⃣ Entity 생성 (DTO 값 → Entity)                  │
│    CrewEntity crew = CrewEntity.builder()               │
│        .name("서울 러닝 크루")                            │
│        .description("함께 달리는 크루")                   │
│        .maxMembers(20)                                  │
│        .owner(owner)                                    │
│        .isActive(true)                                  │
│        .build();                                        │
│                                                          │
│    // 4️⃣ 비즈니스 로직 (크루장 멤버 추가)                │
│    CrewEntity savedCrew = crewRepository.save(crew);    │
│    CrewMemberEntity ownerMember =                       │
│        CrewMemberEntity.createOwner(savedCrew, owner);  │
│    crewMemberRepository.save(ownerMember);              │
│    savedCrew.incrementMemberCount();                    │
│                                                          │
│    return savedCrew;  // Entity 반환                     │
│  }                                                       │
└─────────────────────────────────────────────────────────┘
    │
    │ CrewEntity
    ↓
┌─────────────────────────────────────────────────────────┐
│              💾 Repository Layer                         │
│                                                          │
│  crewRepository.save(crew);                             │
│                                                          │
│  // 5️⃣ JPA가 자동으로 SQL 생성 및 실행                   │
│  INSERT INTO crews (                                    │
│    name, description, max_members,                      │
│    profile_image_url, owner_id, is_active,              │
│    current_members, created_at, updated_at              │
│  ) VALUES (                                             │
│    '서울 러닝 크루', '함께 달리는 크루', 20,              │
│    'https://...', 123, true, 1,                         │
│    '2024-10-06 20:00:00', '2024-10-06 20:00:00'         │
│  );                                                     │
│                                                          │
│  // 6️⃣ 생성된 ID와 함께 Entity 반환                      │
│  return CrewEntity { id: 1, name: "서울 러닝 크루", ... }│
└─────────────────────────────────────────────────────────┘
    │
    │ savedCrew (Entity with ID)
    ↑
┌─────────────────────────────────────────────────────────┐
│              💼 Service Layer (복귀)                     │
│                                                          │
│    return savedCrew;                                    │
└─────────────────────────────────────────────────────────┘
    │
    │ CrewEntity
    ↑
┌─────────────────────────────────────────────────────────┐
│              🎛️ Controller Layer (복귀)                 │
│                                                          │
│    // 7️⃣ Entity → Response DTO 변환                     │
│    CrewDetailResponse response =                        │
│        CrewDetailResponse.from(crew);                   │
│                                                          │
│    CrewDetailResponse {                                 │
│      id: 1,                                             │
│      name: "서울 러닝 크루",                              │
│      description: "함께 달리는 크루",                     │
│      maxMembers: 20,                                    │
│      currentMembers: 1,                                 │
│      ownerId: 123,                                      │
│      ownerNickname: "김러너",                            │
│      createdAt: "2024-10-06T20:00:00",                  │
│      updatedAt: "2024-10-06T20:00:00"                   │
│    }                                                    │
│                                                          │
│    // 8️⃣ HTTP 201 Created 응답                          │
│    return ResponseEntity.status(HttpStatus.CREATED)     │
│                         .body(response);                │
└─────────────────────────────────────────────────────────┘
    │
    │ HTTP/1.1 201 Created
    │ Content-Type: application/json
    │
    │ {
    │   "id": 1,
    │   "name": "서울 러닝 크루",
    │   "description": "함께 달리는 크루",
    │   "maxMembers": 20,
    │   "currentMembers": 1,
    │   "ownerId": 123,
    │   "ownerNickname": "김러너",
    │   ...
    │ }
    ↓
[클라이언트]
```

---

## 📊 레이어별 객체 변환 정리

### 데이터 형태의 변화

| 단계 | 레이어 | 객체 타입 | 역할 |
|------|--------|-----------|------|
| 1 | 클라이언트 → Controller | **JSON** | HTTP 요청 본문 |
| 2 | Controller | **Request DTO** | 유효성 검증 |
| 3 | Controller → Service | **Primitive 값들** | name, description 등 |
| 4 | Service | **Entity** | 비즈니스 로직 처리 |
| 5 | Service → Repository | **Entity** | DB 영속화 |
| 6 | Repository → DB | **SQL** | INSERT 쿼리 |
| 7 | DB → Repository | **Entity** (ID 포함) | 저장된 데이터 |
| 8 | Repository → Service | **Entity** | 비즈니스 로직 완료 |
| 9 | Service → Controller | **Entity** | 처리 결과 |
| 10 | Controller | **Response DTO** | 안전한 응답 생성 |
| 11 | Controller → 클라이언트 | **JSON** | HTTP 응답 본문 |

---

## 🎯 왜 이렇게 복잡하게 나눠야 하나요?

### 단순하게 하면 안 될까?

**❌ 안티패턴 예시:**

```java
// 모든 로직이 Controller에...
@PostMapping("/crews")
public CrewEntity createCrew(@RequestBody CrewEntity crew) {
    // 유효성 검증 (Controller에서)
    if (crew.getName() == null || crew.getName().isEmpty()) {
        throw new IllegalArgumentException("이름 필수");
    }

    // 비즈니스 로직 (Controller에서)
    User owner = userRepository.findById(crew.getOwner().getId()).get();
    crew.setOwner(owner);

    // DB 접근 (Controller에서)
    CrewEntity saved = crewRepository.save(crew);

    // Entity 직접 반환
    return saved;
}
```

**문제점:**
1. **테스트 불가능** - HTTP 없이 비즈니스 로직만 테스트할 수 없음
2. **재사용 불가능** - 다른 API에서 동일 로직 필요 시 중복 코드
3. **트랜잭션 관리 어려움**
4. **유지보수 지옥** - 수정 시 어디를 고쳐야 할지 모호
5. **보안 문제** - Entity 직접 노출로 민감 정보 유출

### 레이어 분리의 장점

**✅ 관심사의 분리 (Separation of Concerns)**

```java
// Controller: HTTP만 신경 씀
@PostMapping("/crews")
public ResponseEntity<CrewDetailResponse> createCrew(...)

// Service: 비즈니스 로직만 신경 씀
@Transactional
public CrewEntity createCrew(...)

// Repository: DB만 신경 씀
CrewEntity save(CrewEntity entity)
```

**✅ 테스트 용이성**

```java
@Test
void 크루_생성_테스트() {
    // Service만 단위 테스트 가능
    // HTTP 서버 띄울 필요 없음
    CrewEntity crew = crewService.createCrew(user, "테스트 크루", ...);
    assertThat(crew.getName()).isEqualTo("테스트 크루");
}
```

**✅ 재사용성**

```java
// 배치 작업에서도 동일한 Service 사용
@Scheduled(cron = "0 0 1 * * *")
public void createDailyCrews() {
    crewService.createCrew(...);  // 같은 로직 재사용
}
```

**✅ 유지보수성**

- 비즈니스 로직 변경 → Service만 수정
- API 스펙 변경 → Controller, DTO만 수정
- DB 스키마 변경 → Entity, Repository만 수정

---

## 🔐 보안과 데이터 보호

### Entity 직접 노출의 위험성

**시나리오 1: 양방향 연관관계로 인한 순환 참조**

```java
// Entity 직접 반환 시
@GetMapping("/crews/{id}")
public CrewEntity getCrew(@PathVariable Long id) {
    return crewRepository.findById(id).get();
}
```

**결과 JSON (에러 발생!):**
```json
{
  "id": 1,
  "name": "서울 러닝 크루",
  "owner": {
    "id": 123,
    "crews": [
      {
        "id": 1,
        "owner": {
          "id": 123,
          "crews": [
            ... 무한 반복 ...
          ]
        }
      }
    ]
  }
}
```

**해결: Response DTO 사용**
```java
@GetMapping("/crews/{id}")
public CrewDetailResponse getCrew(@PathVariable Long id) {
    CrewEntity crew = crewRepository.findById(id).get();
    return CrewDetailResponse.from(crew);  // owner 전체가 아닌 ID, 닉네임만
}
```

**시나리오 2: 민감 정보 노출**

```java
// Entity에 있는 모든 필드
public class CrewEntity {
    private Long version;           // 내부 정보
    private List<CrewMember> members;  // 전체 멤버 리스트
    private String internalNote;    // 관리자 메모
}
```

**Response DTO는 필요한 것만:**
```java
public class CrewDetailResponse {
    private Long id;
    private String name;
    private Integer currentMembers;  // 개수만 (리스트 전체 X)
    // version, internalNote 제외
}
```

---

## 📈 성능 최적화 포인트

### N+1 문제 해결

**문제 상황:**
```java
// 크루 100개 조회
List<CrewEntity> crews = crewRepository.findAll();

// 각 크루의 owner 조회 시 100번의 추가 쿼리 발생!
for (CrewEntity crew : crews) {
    System.out.println(crew.getOwner().getNickname());  // N+1 문제!
}
```

**해결 1: Fetch Join**
```java
@Query("SELECT c FROM CrewEntity c JOIN FETCH c.owner")
List<CrewEntity> findAllWithOwner();
```

**해결 2: EntityGraph**
```java
@EntityGraph(attributePaths = {"owner"})
List<CrewEntity> findAll();
```

### DTO Projection으로 필요한 필드만 조회

```java
// 인터페이스 기반 Projection
public interface CrewSummary {
    Long getId();
    String getName();
    Integer getCurrentMembers();
}

@Query("SELECT c.id as id, c.name as name, c.currentMembers as currentMembers FROM CrewEntity c")
List<CrewSummary> findAllSummaries();
```

---

## ⚠️ 자주 하는 실수와 해결책

### 실수 1: Service에서 DTO 반환

```java
// ❌ 나쁜 예
public CrewDetailResponse createCrew(...) {
    CrewEntity crew = crewRepository.save(...);
    return CrewDetailResponse.from(crew);  // Service가 DTO를 알게 됨
}

// ✅ 좋은 예
public CrewEntity createCrew(...) {
    return crewRepository.save(...);  // Entity만 반환
}
```

### 실수 2: Controller에 비즈니스 로직

```java
// ❌ 나쁜 예
@PostMapping("/crews")
public ResponseEntity<?> createCrew(@RequestBody CrewCreateRequest request) {
    if (crewRepository.existsByName(request.getName())) {
        throw new DuplicateException();  // Controller에서 검증
    }
    // ...
}

// ✅ 좋은 예
@PostMapping("/crews")
public ResponseEntity<?> createCrew(@RequestBody CrewCreateRequest request) {
    CrewEntity crew = crewService.createCrew(...);  // Service에 위임
    return ResponseEntity.ok(CrewDetailResponse.from(crew));
}

// Service에서
public CrewEntity createCrew(...) {
    if (crewRepository.existsByName(name)) {
        throw new DuplicateException();  // 비즈니스 로직
    }
}
```

### 실수 3: `@Transactional` 누락

```java
// ❌ 나쁜 예: @Transactional 없음
public CrewEntity createCrew(...) {
    CrewEntity crew = crewRepository.save(crew);
    crewMemberRepository.save(member);  // 별도 트랜잭션으로 실행됨
    // crew 저장 성공했지만 member 저장 실패 시 일관성 깨짐
}

// ✅ 좋은 예
@Transactional
public CrewEntity createCrew(...) {
    CrewEntity crew = crewRepository.save(crew);
    crewMemberRepository.save(member);
    // 둘 다 성공하거나 둘 다 롤백
}
```

### 실수 4: Lazy Loading 예외

```java
// ❌ 나쁜 예
@Transactional(readOnly = true)
public CrewEntity getCrew(Long id) {
    return crewRepository.findById(id).get();
}

// Controller에서
CrewEntity crew = crewService.getCrew(1L);
crew.getOwner().getNickname();  // LazyInitializationException!
// 트랜잭션이 Service에서 끝나서 Lazy Loading 불가
```

**해결책:**
```java
// 1. Fetch Join 사용
@Query("SELECT c FROM CrewEntity c JOIN FETCH c.owner WHERE c.id = :id")
Optional<CrewEntity> findByIdWithOwner(@Param("id") Long id);

// 2. DTO로 변환 (권장)
@Transactional(readOnly = true)
public CrewDetailResponse getCrew(Long id) {
    CrewEntity crew = crewRepository.findById(id).get();
    return CrewDetailResponse.from(crew);  // 트랜잭션 안에서 DTO 변환
}
```

---

## 📚 추가 학습 자료

### 관련 디자인 패턴

1. **DTO Pattern** - 계층 간 데이터 전송
2. **Repository Pattern** - 데이터 접근 추상화
3. **Service Layer Pattern** - 비즈니스 로직 캡슐화
4. **Builder Pattern** - 복잡한 객체 생성
5. **Factory Method Pattern** - `CrewDetailResponse.from()`

### 참고할 만한 개념

- **DDD (Domain-Driven Design)** - Entity의 비즈니스 메소드
- **Clean Architecture** - 의존성 방향 규칙
- **SOLID 원칙** - 특히 SRP (단일 책임 원칙)

---

## 🎓 마무리

이 글에서 우리는 **Spring Boot REST API의 완벽한 데이터 흐름**을 실전 코드로 살펴봤습니다.

### 핵심 요약

```
📥 Request DTO   → 유효성 검증, 안전한 입력 받기
🎛️ Controller    → HTTP 처리, DTO ↔ Entity 변환
💼 Service       → 비즈니스 로직, 트랜잭션 관리
🗄️ Entity        → 도메인 모델, DB 매핑
💾 Repository    → 데이터 영속화
📤 Response DTO  → 안전한 응답, 민감 정보 제거
```

### 각 레이어의 황금률

| 레이어 | 할 일 | 하지 말 일 |
|--------|-------|-----------|
| **Controller** | HTTP 요청/응답, DTO 변환 | 비즈니스 로직, DB 접근 |
| **Service** | 비즈니스 로직, 트랜잭션 | HTTP 처리, DTO 의존 |
| **Repository** | CRUD, 쿼리 최적화 | 비즈니스 로직 |

### 실전 체크리스트

- [ ] Request DTO에 유효성 검증 어노테이션 추가했는가?
- [ ] Controller는 DTO만 받고 반환하는가?
- [ ] Service 메소드에 `@Transactional` 붙었는가?
- [ ] Entity를 외부에 직접 노출하지 않는가?
- [ ] N+1 문제를 고려했는가?
- [ ] Response DTO에 민감 정보가 없는가?

이제 여러분도 **프로덕션 레벨의 Spring Boot API**를 설계할 수 있습니다! 🚀

---

**프로젝트 출처**: WayToEarth - 러닝 크루 관리 플랫폼
**작성일**: 2024년 10월 6일
**키워드**: Spring Boot, REST API, DTO, Controller, Service, Repository, JPA, 레이어드 아키텍처
