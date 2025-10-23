

##  요약

심층 보안 분석 결과 **13개의 추가 보안 취약점**이 발견

| 심각도 | 개수 | 즉시 조치 필요 |
|--------|------|---------------|
|  Critical | 7 |  P0 (4개), P1 (3개) |
|  High | 4 | ⚠ P2 |
|  Medium | 2 |  P3 |

---

##  Critical - 즉시 수정 필요 (7개)

### 1.  크루장 권한 이양 시 Race Condition

**우선순위:** P0 (최우선)
**위치:** `CrewMemberServiceImpl.java:160-192`
**심각도:** Critical

#### 문제점

```java
@Transactional
public void transferOwnership(AuthenticatedUser user, Long crewId, Long newOwnerId) {
    //  문제: 2개의 OWNER가 동시에 존재 가능

    currentOwnerMember.setRole(CrewRole.MEMBER);  // Step 1: 이전 크루장 → 멤버
    newOwnerMember.setRole(CrewRole.OWNER);       // Step 2: 새 크루장 지정
    crew.setOwner(newOwnerUser);                   // Step 3: 크루 소유자 변경

    //  Step 1과 Step 2 사이에 타이밍 이슈 발생 가능
}
```

#### 공격 시나리오

```
T0: 크루장 A가 B에게 권한 이양 시작
T1: A의 역할이 MEMBER로 변경됨 (Step 1 완료)
T2:  이 순간 크루에 OWNER가 없음!
T3: 악의적인 사용자 C가 동시에 다른 권한 이양 시도
T4: B가 OWNER로 설정됨 (Step 2 완료)

결과: 크루 탈취 가능, 데이터 무결성 위반
```

#### 해결방안

```java
// CrewRepository.java에 추가
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT c FROM CrewEntity c WHERE c.id = :crewId")
Optional<CrewEntity> findByIdWithLock(@Param("crewId") Long crewId);

// CrewMemberServiceImpl.java 수정
@Transactional
public void transferOwnership(AuthenticatedUser user, Long crewId, Long newOwnerId) {
    //  크루 레벨에서 비관적 락 획득
    CrewEntity lockedCrew = crewRepository.findByIdWithLock(crewId)
        .orElseThrow(() -> new CrewNotFoundException("크루를 찾을 수 없습니다."));

    // 현재 크루장인지 확인 (락 획득 후 재확인)
    if (!lockedCrew.getOwner().getId().equals(user.getUserId())) {
        throw new RuntimeException("크루장 권한 이양은 현재 크루장만 가능합니다.");
    }

    // 자기 자신에게는 이양 불가
    if (user.getUserId().equals(newOwnerId)) {
        throw new RuntimeException("자기 자신에게는 권한을 이양할 수 없습니다.");
    }

    // 새 크루장이 멤버인지 확인
    CrewMemberEntity newOwnerMember = crewMemberRepository.findMembership(newOwnerId, crewId)
        .orElseThrow(() -> new RuntimeException("새 크루장은 해당 크루의 멤버여야 합니다."));

    // 현재 크루장을 일반 멤버로 변경
    CrewMemberEntity currentOwnerMember = crewMemberRepository.findMembership(user.getUserId(), crewId)
        .orElseThrow(() -> new RuntimeException("현재 크루장의 멤버십을 찾을 수 없습니다."));

    //  원자적 업데이트: 한 트랜잭션 내에서 모든 변경 완료
    currentOwnerMember.setRole(CrewRole.MEMBER);
    newOwnerMember.setRole(CrewRole.OWNER);
    lockedCrew.setOwner(getUserEntity(newOwnerId));

    log.info("크루장 권한이 이양되었습니다. crewId: {}, fromUserId: {}, toUserId: {}",
            crewId, user.getUserId(), newOwnerId);
}
```

#### 테스트 시나리오

```java
@Test
void 동시_권한_이양_시도_시_하나만_성공() throws InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(2);

    // 크루장 A가 동시에 B와 C에게 권한 이양 시도
    Future<?> future1 = executor.submit(() ->
        crewMemberService.transferOwnership(userA, crewId, userB));
    Future<?> future2 = executor.submit(() ->
        crewMemberService.transferOwnership(userA, crewId, userC));

    // 하나는 성공, 하나는 실패해야 함
    int successCount = 0;
    try { future1.get(); successCount++; } catch (Exception e) {}
    try { future2.get(); successCount++; } catch (Exception e) {}

    assertEquals(1, successCount);

    // 크루에는 정확히 1명의 OWNER만 존재해야 함
    long ownerCount = crewMemberRepository.countByCrewAndRole(crew, CrewRole.OWNER);
    assertEquals(1, ownerCount);
}
```

---

### 2.  멤버 추방 시 Race Condition

**우선순위:** P0
**위치:** `CrewMemberServiceImpl.java:48-76`
**심각도:** Critical

#### 문제점

```java
@Transactional
public void removeMemberFromCrew(AuthenticatedUser user, Long crewId, Long targetUserId) {
    //  동시에 2명이 같은 사용자를 추방하려고 할 때

    crewMemberRepository.findMembership(targetUserId, crewId)
        .orElseThrow(() -> new RuntimeException("해당 사용자는 크루 멤버가 아닙니다."));

    int affected = crewMemberRepository.deleteByCrewIdAndUserId(crewId, targetUserId);

    crew.decrementMemberCount();  //  멤버 수가 2번 감소될 수 있음
}
```

#### 공격 시나리오

```
T0: 크루 멤버 수 = 50명
T1: 크루장 A가 사용자 X 추방 시작 (멤버 확인 통과)
T2: 크루장이 동시에 사용자 X를 다시 추방 시도 (멤버 확인 통과)
T3: A의 트랜잭션 완료 → 멤버 수 = 49명
T4: 두 번째 트랜잭션도 완료? → 멤버 수 = 48명 (실제로는 1명만 삭제됨)

결과: 멤버 수 불일치, 정원 관리 오류
```

#### 해결방안

```java
// CrewMemberRepository.java에 추가
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT cm FROM CrewMemberEntity cm " +
       "WHERE cm.user.id = :userId AND cm.crew.id = :crewId")
Optional<CrewMemberEntity> findMembershipWithLock(
    @Param("userId") Long userId,
    @Param("crewId") Long crewId);

// CrewMemberServiceImpl.java 수정
@Transactional
public void removeMemberFromCrew(AuthenticatedUser user, Long crewId, Long targetUserId) {
    CrewEntity crew = getCrewEntity(crewId);

    // 크루장인지 확인
    if (!isCrewOwner(crew, user.getUserId())) {
        throw new RuntimeException("멤버 추방은 크루장만 가능합니다.");
    }

    // 자기 자신은 추방할 수 없음
    if (user.getUserId().equals(targetUserId)) {
        throw new RuntimeException("크루장은 자신을 추방할 수 없습니다. 크루장 권한을 이양하세요.");
    }

    //  멤버 삭제 전에 존재 여부 확인 (SELECT FOR UPDATE)
    CrewMemberEntity targetMember = crewMemberRepository
        .findMembershipWithLock(targetUserId, crewId)
        .orElseThrow(() -> new RuntimeException("해당 사용자는 크루 멤버가 아닙니다."));

    // 물리 삭제
    int affected = crewMemberRepository.deleteByCrewIdAndUserId(crewId, targetUserId);

    //  실제로 삭제된 경우에만 카운트 감소
    if (affected > 0) {
        crew.decrementMemberCount();
        log.info("크루 멤버가 추방되었습니다. crewId: {}, targetUserId: {}, removedBy: {}",
                crewId, targetUserId, user.getUserId());
    } else {
        log.warn("멤버 추방 실패 - 이미 삭제됨: crewId: {}, targetUserId: {}", crewId, targetUserId);
    }
}
```

---

### 3.  JWT 토큰 갱신 시 이전 권한 유지 문제

**우선순위:** P0
**위치:** 전역 (JWT 인증 시스템)
**심각도:** Critical

#### 문제점

```
1. 사용자 A가 크루 1의 크루장 (JWT: role=OWNER)
2. 크루장 권한을 B에게 이양 (DB: A=MEMBER, B=OWNER)
3.  A의 JWT는 여전히 role=OWNER
4.  JWT가 만료될 때까지 (24시간?) A는 크루장 권한 유지
5. A가 멤버를 추방하거나 크루 설정 변경 가능

결과: 권한 상승(Privilege Escalation), 크루 침해
```

#### 해결방안 1: JWT에 역할 정보를 포함하지 않기 (권장)

```java
// JwtTokenProvider.java
public String generateToken(Long userId) {
    //  JWT에 role 포함하지 않음
    Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
    // claims.put("role", role);  // ← 제거

    Date now = new Date();
    Date validity = new Date(now.getTime() + validityInMilliseconds);

    return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
}

// CrewServiceImpl.java - 매번 DB에서 권한 확인
@Override
public boolean isCrewOwner(Long crewId, Long userId) {
    //  JWT가 아닌 DB에서 실시간 권한 확인
    return crewMemberRepository.isUserOwnerOfCrew(userId, crewId);
}
```

#### 해결방안 2: 권한 변경 시 토큰 무효화 (복잡함)

```java
// Redis에 블랙리스트 저장
@Transactional
public void transferOwnership(AuthenticatedUser user, Long crewId, Long newOwnerId) {
    // ... 권한 이양 로직 ...

    //  이전 크루장의 토큰 무효화
    tokenBlacklistService.addToBlacklist(user.getToken(), Duration.ofHours(24));

    log.info("크루장 권한이 이양되었으며, 이전 크루장의 토큰이 무효화되었습니다.");
}

// JwtAuthenticationFilter.java
@Override
protected void doFilterInternal(HttpServletRequest request, ...) {
    String token = extractToken(request);

    //  블랙리스트 확인
    if (tokenBlacklistService.isBlacklisted(token)) {
        throw new InvalidTokenException("무효화된 토큰입니다.");
    }

    // ... 인증 로직 ...
}
```

---

### 4.  멤버 수 동기화 불일치

**우선순위:** P0
**위치:** 전역 (`incrementMemberCount()`, `decrementMemberCount()`)
**심각도:** Critical

#### 문제점

```java
// CrewEntity.java
public void incrementMemberCount() {
    this.currentMembers++;  //  메모리 상태만 변경
}

// CrewServiceImpl.java:66
savedCrew.incrementMemberCount();  //  DB 업데이트 없음

// CrewMemberServiceImpl.java:72
crew.decrementMemberCount();  //  DB 업데이트 없음
```

**문제점:**
1. `incrementMemberCount()`/`decrementMemberCount()`는 메모리 상태만 변경
2. `@Transactional`이 끝나면 JPA가 dirty checking으로 DB 업데이트
3. **하지만 낙관적 락 충돌 시 OptimisticLockException 발생**
4. 예외 처리 없으면 멤버 수 동기화 실패

#### 실제 발생 시나리오

```
T0: 크루 1, 멤버 수 = 50명, version = 10
T1: 사용자 A 가입 승인 시작 (version 10 읽음)
T2: 사용자 B 가입 승인 시작 (version 10 읽음)
T3: A 가입 완료, 멤버 수 = 51, version = 11로 업데이트
T4: B 가입 시도 → OptimisticLockException (version 불일치)
T5:  B의 가입은 롤백되지만, 사용자에게 "가입 승인됨" 응답 전송됨

결과: 정원 관리 오류, 사용자 혼란
```

#### 해결방안: 비관적 락 사용 (권장)

```java
// CrewRepository.java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT c FROM CrewEntity c WHERE c.id = :crewId")
Optional<CrewEntity> findByIdWithLock(@Param("crewId") Long crewId);

// CrewJoinServiceImpl.java
@Transactional
public void approveJoinRequest(AuthenticatedUser user, Long requestId) {
    CrewJoinRequestEntity joinRequest = getJoinRequest(requestId);

    //  비관적 락으로 크루 잠금
    CrewEntity crew = crewRepository.findByIdWithLock(joinRequest.getCrew().getId())
        .orElseThrow(() -> new CrewNotFoundException("크루를 찾을 수 없습니다."));

    // 크루장인지 확인
    if (!isCrewOwner(crew, user.getUserId())) {
        throw new RuntimeException("가입 신청 승인은 크루장만 가능합니다.");
    }

    // 신청 상태 확인
    if (joinRequest.getStatus() != JoinRequestStatus.PENDING) {
        throw new RuntimeException("이미 처리된 가입 신청입니다.");
    }

    //  실시간 멤버 수 확인 (Race Condition 방지)
    long actualMemberCount = crewMemberRepository.countByCrewIdAndIsActiveTrue(crew.getId());
    if (actualMemberCount >= crew.getMaxMembers()) {
        throw new RuntimeException("크루 정원이 초과되었습니다. (현재: " + actualMemberCount + "명)");
    }

    // 가입 신청 승인
    joinRequest.approve(getUserEntity(user.getUserId()), "가입 승인");
    joinRequestRepository.saveAndFlush(joinRequest);

    // 새로운 멤버 추가
    CrewMemberEntity newMember = CrewMemberEntity.createMember(crew, joinRequest.getUser());
    crewMemberRepository.save(newMember);

    // 크루 멤버 수 증가
    crew.incrementMemberCount();

    log.info("크루 가입 신청이 승인되었습니다. requestId: {}, approvedBy: {}, newMemberId: {}, actualCount: {}",
            requestId, user.getUserId(), joinRequest.getUser().getId(), actualMemberCount + 1);
}
```

---

### 5.  크루 생성 시 중복 처리 취약점 (멱등성 문제)

**우선순위:** P1
**위치:** `CrewServiceImpl.java:42-70`
**심각도:** Critical

#### 문제점

```java
@Transactional
public CrewEntity createCrew(AuthenticatedUser user, String name, ...) {
    //  중복 이름 검사 없음

    CrewEntity crew = CrewEntity.builder()
            .name(name)  //  동일 이름으로 여러 크루 생성 가능
            .build();

    CrewEntity savedCrew = crewRepository.save(crew);

    //  네트워크 타임아웃 발생 시 재시도하면 중복 크루 생성
    CrewMemberEntity ownerMember = CrewMemberEntity.createOwner(savedCrew, owner);
    crewMemberRepository.save(ownerMember);

    savedCrew.incrementMemberCount();

    return savedCrew;
}
```

#### 공격 시나리오

```
1. 사용자가 "서울 러닝 크루" 생성 요청
2. 네트워크 지연으로 응답 받지 못함
3. 사용자가 다시 "서울 러닝 크루" 생성 요청
4.  동일 이름의 크루 2개 생성됨

결과: 데이터 중복, 사용자 혼란
```

#### 해결방안

```java
// CrewRepository.java에 추가
boolean existsByName(String name);
long countByOwnerAndIsActiveTrue(User owner);

// CrewServiceImpl.java 수정
@Transactional
public CrewEntity createCrew(AuthenticatedUser user, String name, String description,
                            Integer maxMembers, String profileImageUrl) {
    User owner = userRepository.findById(user.getUserId())
            .orElseThrow(() -> new UserNotFoundException(user.getUserId()));

    //  중복 이름 검사 (DB unique constraint와 이중 방어)
    if (crewRepository.existsByName(name)) {
        throw new DuplicateCrewNameException("이미 존재하는 크루 이름입니다: " + name);
    }

    //  사용자당 생성 가능한 크루 수 제한
    long ownedCrewCount = crewRepository.countByOwnerAndIsActiveTrue(owner);
    if (ownedCrewCount >= 10) {  // 예: 최대 10개
        throw new RuntimeException("한 사용자는 최대 10개의 크루만 생성할 수 있습니다.");
    }

    try {
        CrewEntity crew = CrewEntity.builder()
                .name(name)
                .description(description)
                .maxMembers(maxMembers != null ? maxMembers : 50)
                .profileImageUrl(profileImageUrl)
                .owner(owner)
                .isActive(true)
                .build();

        CrewEntity savedCrew = crewRepository.save(crew);

        // 크루 소유자를 멤버로 추가
        CrewMemberEntity ownerMember = CrewMemberEntity.createOwner(savedCrew, owner);
        crewMemberRepository.save(ownerMember);

        // 현재 멤버 수 업데이트
        savedCrew.incrementMemberCount();

        log.info("크루가 생성되었습니다. crewId: {}, ownerId: {}", savedCrew.getId(), user.getUserId());
        return savedCrew;

    } catch (DataIntegrityViolationException e) {
        //  DB unique constraint 위반 시 명확한 에러 메시지
        if (e.getMessage().contains("uk_crew_name")) {
            throw new DuplicateCrewNameException("이미 존재하는 크루 이름입니다: " + name);
        }
        throw e;
    }
}
```

**CrewEntity에 unique constraint 확인:**
```java
@Entity
@Table(name = "crews",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"name"}, name = "uk_crew_name")
       })
public class CrewEntity extends BaseTimeEntity {
    // ...
}
```

---

### 6.  가입 신청 중복 처리 취약점

**우선순위:** P1
**위치:** `CrewJoinServiceImpl.java:37-71`
**심각도:** Critical

#### 문제점

```java
@Transactional
public CrewJoinRequestEntity requestToJoinCrew(AuthenticatedUser user, Long crewId, String message) {
    //  canJoinCrew()에서 대기 중인 신청 확인은 하지만...

    if (!canJoinCrew(user, crewId)) {
        throw new RuntimeException("해당 크루에 가입할 수 없습니다.");
    }

    //  동시에 2번 요청하면?
    CrewJoinRequestEntity joinRequest = CrewJoinRequestEntity.builder()
            .crew(crew)
            .user(userEntity)
            .message(message)
            .status(JoinRequestStatus.PENDING)
            .build();

    CrewJoinRequestEntity savedRequest = joinRequestRepository.save(joinRequest);

    return savedRequest;
}
```

#### 공격 시나리오

```
T0: 사용자 A가 크루 1 가입 신청 (중복 검사 통과)
T1: 사용자 A가 다시 크루 1 가입 신청 (중복 검사 통과 - 아직 DB 저장 전)
T2: 첫 번째 신청 저장됨
T3: 두 번째 신청도 저장됨
T4:  동일 사용자의 가입 신청 2개 생성

결과: 데이터 중복, 크루장 혼란
```

#### 해결방안

**CrewJoinRequestEntity.java에 추가:**
```java
@Entity
@Table(name = "crew_join_requests",
       uniqueConstraints = {
           @UniqueConstraint(
               columnNames = {"crew_id", "user_id", "status"},
               name = "uk_join_request_pending"
           )
       })
public class CrewJoinRequestEntity extends BaseTimeEntity {
    // ...
}
```

**CrewJoinServiceImpl.java 수정:**
```java
@Transactional
public CrewJoinRequestEntity requestToJoinCrew(AuthenticatedUser user, Long crewId, String message) {
    CrewEntity crew = getCrewEntity(crewId);
    User userEntity = getUserEntity(user.getUserId());

    //  DB unique constraint 활용 (crew_id, user_id, status='PENDING')

    // 가입 가능 여부 확인
    if (!canJoinCrew(user, crewId)) {
        throw new RuntimeException("해당 크루에 가입할 수 없습니다.");
    }

    // 크루가 활성 상태인지 확인
    if (!crew.getIsActive()) {
        throw new RuntimeException("비활성화된 크루에는 가입 신청할 수 없습니다.");
    }

    // 크루 인원이 가득 찬지 확인
    if (crew.isFull()) {
        throw new RuntimeException("크루 정원이 가득 찼습니다.");
    }

    try {
        // 가입 신청 생성
        CrewJoinRequestEntity joinRequest = CrewJoinRequestEntity.builder()
                .crew(crew)
                .user(userEntity)
                .message(message)
                .status(JoinRequestStatus.PENDING)
                .build();

        CrewJoinRequestEntity savedRequest = joinRequestRepository.save(joinRequest);

        log.info("크루 가입 신청이 생성되었습니다. requestId: {}, crewId: {}, userId: {}",
                savedRequest.getId(), crewId, user.getUserId());

        return savedRequest;

    } catch (DataIntegrityViolationException e) {
        //  unique constraint 위반 시 명확한 에러
        if (e.getMessage().contains("uk_join_request_pending")) {
            throw new DuplicateJoinRequestException("이미 대기 중인 가입 신청이 있습니다.");
        }
        throw e;
    }
}
```

---

### 7.  크루 삭제 시 데이터 정합성 문제

**우선순위:** P1
**위치:** `CrewServiceImpl.java:228-257`
**심각도:** Critical

#### 문제점

```java
@Transactional
public void deleteCrew(Long userId, Long crewId) {
    CrewEntity crew = getCrewById(crewId);

    if (!isCrewOwner(crewId, userId)) {
        throw new RuntimeException("크루 삭제는 크루장만 가능합니다.");
    }

    // 1. S3 이미지 삭제
    fileService.deleteObject(imageKey);  //  외부 API 호출

    // 2. 통계 삭제
    crewStatisticsService.cleanupStatisticsForCrew(crewId);

    // 3. 알림 설정 삭제
    crewChatNotificationSettingRepository.deleteAllByCrew_Id(crewId);

    // 4. Redis 랭킹 삭제
    crewRankingService.removeCrewFromAllRankings(crewId);  //  외부 서비스

    // 5. 크루 물리 삭제
    crewRepository.deleteById(crewId);

    //  문제:
    // - S3 삭제 실패 시 트랜잭션 롤백되어야 하는가?
    // - Redis 삭제 실패 시?
    // - 채팅 메시지는 보존되지만 orphan 데이터 발생
}
```

#### 해결방안

**Step 1: 소프트 삭제**
```java
// CrewServiceImpl.java
@Transactional
public void deleteCrew(Long userId, Long crewId) {
    CrewEntity crew = getCrewById(crewId);

    if (!isCrewOwner(crewId, userId)) {
        throw new RuntimeException("크루 삭제는 크루장만 가능합니다.");
    }

    //  Step 1: 소프트 삭제 먼저 (DB 트랜잭션 내)
    crew.setIsActive(false);
    crew.setDeletedAt(LocalDateTime.now());
    crewRepository.save(crew);

    //  이벤트 발행
    eventPublisher.publishEvent(new CrewDeletedEvent(
        crewId,
        crew.getProfileImageKey(),
        LocalDateTime.now()
    ));

    log.info("크루가 비활성화되었습니다. crewId: {}, userId: {}", crewId, userId);
}
```

**Step 2: 비동기 리소스 정리**
```java
// CrewDeletionEventListener.java
@Component
@Slf4j
public class CrewDeletionEventListener {

    private final FileService fileService;
    private final CrewRankingService crewRankingService;
    private final CrewDeletionScheduler crewDeletionScheduler;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCrewDeletedEvent(CrewDeletedEvent event) {
        Long crewId = event.getCrewId();

        try {
            //  S3 이미지 삭제 (실패해도 괜찮음)
            if (event.getProfileImageKey() != null) {
                fileService.deleteObject(event.getProfileImageKey());
            }
        } catch (Exception e) {
            log.error("크루 프로필 이미지 삭제 실패 - crewId: {}", crewId, e);
        }

        try {
            //  Redis 랭킹 삭제
            crewRankingService.removeCrewFromAllRankings(crewId);
        } catch (Exception e) {
            log.error("크루 랭킹 삭제 실패 - crewId: {}", crewId, e);
        }

        //  30일 후 물리 삭제 (스케줄러)
        crewDeletionScheduler.schedulePhysicalDeletion(
            crewId,
            LocalDateTime.now().plusDays(30)
        );
    }
}
```

**Step 3: 물리 삭제 스케줄러**
```java
// CrewDeletionScheduler.java
@Component
@Slf4j
public class CrewDeletionScheduler {

    private final CrewRepository crewRepository;
    private final CrewStatisticsService crewStatisticsService;
    private final CrewChatNotificationSettingRepository notificationRepository;

    @Scheduled(cron = "0 0 3 * * ?")  // 매일 새벽 3시
    @Transactional
    public void processScheduledDeletions() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        List<CrewEntity> crewsToDelete = crewRepository
            .findByIsActiveFalseAndDeletedAtBefore(thirtyDaysAgo);

        for (CrewEntity crew : crewsToDelete) {
            try {
                // 통계 데이터 삭제
                crewStatisticsService.cleanupStatisticsForCrew(crew.getId());

                // 알림 설정 삭제
                notificationRepository.deleteAllByCrew_Id(crew.getId());

                // 크루 물리 삭제 (CASCADE로 멤버, 가입신청 자동 삭제)
                crewRepository.deleteById(crew.getId());

                log.info("크루 물리 삭제 완료 - crewId: {}", crew.getId());

            } catch (Exception e) {
                log.error("크루 물리 삭제 실패 - crewId: {}", crew.getId(), e);
            }
        }
    }
}
```

---

## 🟠 High - 높은 우선순위 (4개)

### 8. 🟠 크루 정보 수정 시 검증 부족

**우선순위:** P2
**위치:** `CrewServiceImpl.java:92-117`
**심각도:** High

#### 문제점

```java
@Transactional
public CrewEntity updateCrew(AuthenticatedUser user, Long crewId, String name,
                            String description, Integer maxMembers, ...) {
    CrewEntity crew = getCrewById(crewId);

    // ... 권한 확인 ...

    // 현재 멤버 수보다 적게 설정할 수 없음
    int currentMemberCount = crew.getCurrentMemberCount();
    if (maxMembers != null && maxMembers < currentMemberCount) {
        throw new InvalidParameterException("현재 멤버 수(" + currentMemberCount + ")보다 적게 설정할 수 없습니다.");
    }

    //  문제 1: currentMemberCount는 캐시된 값 (부정확할 수 있음)
    //  문제 2: 크루 이름 변경 시 중복 검사 없음

    if (name != null) crew.setName(name);  //  중복 이름 가능
    if (description != null) crew.setDescription(description);
    if (maxMembers != null) crew.setMaxMembers(maxMembers);

    return crew;
}
```

#### 해결방안

```java
// CrewRepository.java에 추가
boolean existsByNameAndIdNot(String name, Long id);

// CrewServiceImpl.java 수정
@Transactional
public CrewEntity updateCrew(AuthenticatedUser user, Long crewId, String name,
                            String description, Integer maxMembers,
                            String profileImageUrl, String profileImageKey) {
    CrewEntity crew = getCrewById(crewId);

    // 크루장인지 확인
    if (!isCrewOwner(crewId, user.getUserId())) {
        throw new UnauthorizedAccessException("크루 정보 수정은 크루장만 가능합니다.");
    }

    //  이름 변경 시 중복 검사
    if (name != null && !name.equals(crew.getName())) {
        if (crewRepository.existsByNameAndIdNot(name, crewId)) {
            throw new DuplicateCrewNameException("이미 존재하는 크루 이름입니다: " + name);
        }
        crew.setName(name);
    }

    //  실시간 멤버 수로 정원 검증
    if (maxMembers != null) {
        long actualMemberCount = crewMemberRepository.countByCrewIdAndIsActiveTrue(crewId);
        if (maxMembers < actualMemberCount) {
            throw new InvalidParameterException(
                "현재 멤버 수(" + actualMemberCount + ")보다 적게 설정할 수 없습니다.");
        }
        crew.setMaxMembers(maxMembers);
    }

    if (description != null) crew.setDescription(description);
    if (profileImageUrl != null) crew.setProfileImageUrl(profileImageUrl);
    if (profileImageKey != null) crew.setProfileImageKey(profileImageKey);

    log.info("크루 정보가 수정되었습니다. crewId: {}, userId: {}", crewId, user.getUserId());
    return crew;
}
```

---

### 9. 🟠 역할 변경 시 권한 검증 부족

**우선순위:** P2
**위치:** `CrewMemberServiceImpl.java:104-134`
**심각도:** High

#### 문제점

```java
@Transactional
public CrewMemberEntity changeMemberRole(AuthenticatedUser user, Long crewId,
                                        Long targetUserId, CrewRole newRole) {
    CrewEntity crew = getCrewEntity(crewId);

    // 크루장인지 확인
    if (!isCrewOwner(crew, user.getUserId())) {
        throw new RuntimeException("멤버 역할 변경은 크루장만 가능합니다.");
    }

    //  문제: CrewRole에 ADMIN 역할이 추가되면?
    //  OWNER → ADMIN → MEMBER 계층 구조 확인 없음

    if (newRole == CrewRole.OWNER) {
        throw new RuntimeException("크루장 권한 이양은 별도 기능을 사용하세요.");
    }

    targetMember.setRole(newRole);

    return targetMember;
}
```

#### 해결방안

**CrewRole.java 개선:**
```java
package com.waytoearth.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CrewRole {
    OWNER("OWNER", "크루장", 100),
    ADMIN("ADMIN", "관리자", 50),      // 향후 추가 가능
    MODERATOR("MODERATOR", "모더레이터", 30),
    MEMBER("MEMBER", "일반 멤버", 0);

    private final String code;
    private final String description;
    private final int level;  //  권한 레벨

    /**
     * 현재 역할이 대상 역할을 관리할 수 있는지 확인
     */
    public boolean canManage(CrewRole targetRole) {
        return this.level > targetRole.level;
    }

    /**
     * 현재 역할이 특정 작업을 수행할 수 있는지 확인
     */
    public boolean hasPermission(CrewPermission permission) {
        return this.level >= permission.getRequiredLevel();
    }
}

// 권한 정의
public enum CrewPermission {
    KICK_MEMBER(50),          // 멤버 추방
    CHANGE_ROLE(50),          // 역할 변경
    UPDATE_CREW_INFO(50),     // 크루 정보 수정
    DELETE_MESSAGE(30),       // 메시지 삭제
    SEND_ANNOUNCEMENT(30);    // 공지 작성

    private final int requiredLevel;

    CrewPermission(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }
}
```

**CrewMemberServiceImpl.java 수정:**
```java
@Transactional
public CrewMemberEntity changeMemberRole(AuthenticatedUser user, Long crewId,
                                        Long targetUserId, CrewRole newRole) {
    CrewEntity crew = getCrewEntity(crewId);

    // 현재 사용자의 역할 조회
    CrewMemberEntity currentUserMember = crewMemberRepository
        .findMembership(user.getUserId(), crewId)
        .orElseThrow(() -> new RuntimeException("크루 멤버가 아닙니다."));

    //  권한 레벨 검증
    if (!currentUserMember.getRole().canManage(newRole)) {
        throw new UnauthorizedAccessException("해당 역할로 변경할 권한이 없습니다.");
    }

    // 자기 자신의 역할은 변경할 수 없음
    if (user.getUserId().equals(targetUserId)) {
        throw new RuntimeException("자신의 역할은 변경할 수 없습니다.");
    }

    // OWNER 역할로는 변경 불가
    if (newRole == CrewRole.OWNER) {
        throw new RuntimeException("크루장 권한 이양은 별도 기능을 사용하세요.");
    }

    // 대상 멤버 조회
    CrewMemberEntity targetMember = crewMemberRepository
        .findMembership(targetUserId, crewId)
        .orElseThrow(() -> new RuntimeException("해당 사용자는 크루 멤버가 아닙니다."));

    //  대상 멤버의 현재 역할보다 높은 권한을 가져야 함
    if (!currentUserMember.getRole().canManage(targetMember.getRole())) {
        throw new UnauthorizedAccessException("대상 멤버의 역할을 변경할 권한이 없습니다.");
    }

    targetMember.setRole(newRole);

    log.info("크루 멤버 역할이 변경되었습니다. crewId: {}, targetUserId: {}, newRole: {}, changedBy: {}",
            crewId, targetUserId, newRole, user.getUserId());

    return targetMember;
}
```

---

### 10. 🟠 가입 신청 거부 시 재신청 차단 없음

**우선순위:** P2
**위치:** `CrewJoinServiceImpl.java:110-130`
**심각도:** High

#### 문제점

```java
@Transactional
public void rejectJoinRequest(AuthenticatedUser user, Long requestId, String reason) {
    CrewJoinRequestEntity joinRequest = getJoinRequest(requestId);

    // ... 권한 확인 ...

    // 가입 신청 거부
    joinRequest.reject(getUserEntity(user.getUserId()), reason);

    //  문제: 즉시 다시 신청 가능
    //  스팸 신청 방지 로직 없음
}
```

#### 공격 시나리오

```
1. 사용자 A가 크루 1에 가입 신청
2. 크루장이 거부
3. 사용자 A가 즉시 다시 신청
4. 크루장이 거부
5. 무한 반복 → 크루장 괴롭힘

결과: 스팸, DoS 공격
```

#### 해결방안

**CrewJoinRequestRepository.java에 추가:**
```java
@Query("SELECT jr FROM CrewJoinRequestEntity jr " +
       "WHERE jr.user.id = :userId AND jr.crew.id = :crewId " +
       "AND jr.status = 'REJECTED' AND jr.processedAt > :after " +
       "ORDER BY jr.processedAt DESC")
Optional<CrewJoinRequestEntity> findRecentRejectedRequest(
    @Param("userId") Long userId,
    @Param("crewId") Long crewId,
    @Param("after") LocalDateTime after);
```

**CrewJoinServiceImpl.java 수정:**
```java
@Transactional
public CrewJoinRequestEntity requestToJoinCrew(AuthenticatedUser user, Long crewId, String message) {
    CrewEntity crew = getCrewEntity(crewId);
    User userEntity = getUserEntity(user.getUserId());

    //  최근 거부된 신청 확인 (예: 7일 이내)
    LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
    Optional<CrewJoinRequestEntity> recentRejection = joinRequestRepository
        .findRecentRejectedRequest(userEntity.getId(), crewId, sevenDaysAgo);

    if (recentRejection.isPresent()) {
        CrewJoinRequestEntity rejected = recentRejection.get();
        long daysRemaining = ChronoUnit.DAYS.between(
            LocalDateTime.now(),
            rejected.getProcessedAt().plusDays(7)
        );

        throw new RecentRejectionException(
            "거부된 신청 후 7일간 재신청할 수 없습니다. 남은 기간: " + daysRemaining + "일"
        );
    }

    //  신청 빈도 제한 (예: 1시간에 최대 3회)
    long recentRequestCount = joinRequestRepository.countRecentRequests(
        userEntity.getId(),
        crewId,
        LocalDateTime.now().minusHours(1)
    );

    if (recentRequestCount >= 3) {
        throw new RateLimitExceededException(
            "가입 신청이 너무 빈번합니다. 1시간 후 다시 시도해주세요."
        );
    }

    // 가입 가능 여부 확인
    if (!canJoinCrew(user, crewId)) {
        throw new RuntimeException("해당 크루에 가입할 수 없습니다.");
    }

    try {
        // 가입 신청 생성
        CrewJoinRequestEntity joinRequest = CrewJoinRequestEntity.builder()
                .crew(crew)
                .user(userEntity)
                .message(message)
                .status(JoinRequestStatus.PENDING)
                .build();

        CrewJoinRequestEntity savedRequest = joinRequestRepository.save(joinRequest);

        log.info("크루 가입 신청이 생성되었습니다. requestId: {}, crewId: {}, userId: {}",
                savedRequest.getId(), crewId, user.getUserId());

        return savedRequest;

    } catch (DataIntegrityViolationException e) {
        if (e.getMessage().contains("uk_join_request_pending")) {
            throw new DuplicateJoinRequestException("이미 대기 중인 가입 신청이 있습니다.");
        }
        throw e;
    }
}
```

---

### 11. 🟠 크루 비활성화 시 진행 중인 작업 처리 없음

**우선순위:** P2
**위치:** `CrewServiceImpl.java:144-158`
**심각도:** High

#### 문제점

```java
@Transactional
public CrewEntity toggleCrewStatus(AuthenticatedUser user, Long crewId) {
    CrewEntity crew = getCrewById(crewId);

    // 크루장인지 확인
    if (!isCrewOwner(crewId, user.getUserId())) {
        throw new UnauthorizedAccessException("크루 상태 변경은 크루장만 가능합니다.");
    }

    crew.setIsActive(!crew.getIsActive());  //  즉시 비활성화

    //  문제:
    // 1. 진행 중인 가입 신청은?
    // 2. WebSocket 연결된 사용자는?
    // 3. 비활성화된 크루의 채팅은?
}
```

#### 해결방안

```java
// CrewJoinRequestRepository.java에 추가
List<CrewJoinRequestEntity> findByCrewAndStatus(CrewEntity crew, JoinRequestStatus status);

// CrewServiceImpl.java 수정
@Transactional
public CrewEntity toggleCrewStatus(AuthenticatedUser user, Long crewId) {
    CrewEntity crew = getCrewById(crewId);

    // 크루장인지 확인
    if (!isCrewOwner(crewId, user.getUserId())) {
        throw new UnauthorizedAccessException("크루 상태 변경은 크루장만 가능합니다.");
    }

    boolean newStatus = !crew.getIsActive();
    crew.setIsActive(newStatus);

    if (!newStatus) {
        //  비활성화 시 후처리

        // 1. 대기 중인 가입 신청 자동 거부
        List<CrewJoinRequestEntity> pendingRequests =
            joinRequestRepository.findByCrewAndStatus(crew, JoinRequestStatus.PENDING);

        User systemUser = crew.getOwner();
        for (CrewJoinRequestEntity request : pendingRequests) {
            request.reject(systemUser, "크루가 비활성화되어 자동 거부되었습니다.");
        }

        // 2. WebSocket 연결 종료 이벤트 발행
        eventPublisher.publishEvent(new CrewDeactivatedEvent(crewId));

        log.info("크루가 비활성화되었으며, {} 건의 가입 신청이 자동 거부되었습니다.",
                pendingRequests.size());
    } else {
        log.info("크루가 활성화되었습니다. crewId: {}", crewId);
    }

    log.info("크루 상태가 변경되었습니다. crewId: {}, isActive: {}, userId: {}",
            crewId, crew.getIsActive(), user.getUserId());
    return crew;
}
```

**이벤트 리스너:**
```java
// CrewWebSocketEventListener.java
@Component
@Slf4j
public class CrewWebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleCrewDeactivated(CrewDeactivatedEvent event) {
        //  해당 크루의 모든 WebSocket 세션에 연결 종료 메시지 전송
        messagingTemplate.convertAndSend(
            "/topic/crew/" + event.getCrewId() + "/system",
            Map.of(
                "type", "CREW_DEACTIVATED",
                "message", "크루가 비활성화되어 연결이 종료됩니다.",
                "timestamp", LocalDateTime.now()
            )
        );

        log.info("크루 비활성화 알림 전송 완료 - crewId: {}", event.getCrewId());
    }
}
```

---

## 🟡 Medium - 중간 우선순위 (2개)

### 12. 🟡 크루 탈퇴 시 채팅 메시지 고아 데이터

**우선순위:** P3
**위치:** `CrewMemberServiceImpl.java:78-102`
**심각도:** Medium

#### 문제점

```java
@Transactional
public void leaveCrew(AuthenticatedUser user, Long crewId) {
    // ... 멤버 삭제 ...

    //  문제: 탈퇴한 사용자의 채팅 메시지는?
    // - sender_id가 유효하지만 크루 멤버가 아님
    // - 메시지 표시 시 "탈퇴한 사용자" 처리 필요
}
```

#### 해결방안

**CrewChatMessageDto.java 수정:**
```java
@Builder
public class CrewChatMessageDto {
    private Long messageId;
    private Long crewId;
    private Long senderId;
    private String senderName;  //  "탈퇴한 사용자"로 표시
    private boolean isSenderActive;  //  멤버 여부
    private String message;
    private CrewChatEntity.MessageType messageType;
    private LocalDateTime sentAt;
    private boolean isRead;
    private int readCount;
}
```

**CrewChatServiceImpl.java 수정:**
```java
@Override
public Page<CrewChatMessageDto> getChatMessages(Long crewId, Long userId, Pageable pageable) {
    validateCrewMember(crewId, userId);

    Page<CrewChatEntity> entities = crewChatRepository
        .findChatEntitiesWithReadStatus(crewId, userId, pageable);

    return entities.map(entity -> {
        //  발신자가 현재 멤버인지 확인
        boolean isSenderActive = crewMemberRepository.isUserMemberOfCrew(
            entity.getSender().getId(),
            crewId
        );

        String senderName = isSenderActive
            ? entity.getSender().getNickname()
            : "탈퇴한 사용자";

        return CrewChatMessageDto.builder()
            .messageId(entity.getId())
            .crewId(entity.getCrew().getId())
            .senderId(entity.getSender().getId())
            .senderName(senderName)
            .isSenderActive(isSenderActive)
            .message(entity.getMessage())
            .messageType(entity.getMessageType())
            .sentAt(entity.getSentAt())
            .isRead(entity.isReadBy(userId))
            .readCount(entity.getReadStatus().size())
            .build();
    });
}
```

---

### 13. 🟡 크루 검색 시 SQL Injection 가능성

**우선순위:** P3
**위치:** `CrewServiceImpl.java:85-89`
**심각도:** Medium (낮음, 하지만 확인 필요)

#### 문제점

```java
@Override
public Page<CrewEntity> searchCrewsByName(String name, Pageable pageable) {
    // N+1 방지 및 DB 네이티브 페이징 사용
    return crewRepository.findByNameContainingWithOwner(name, pageable);
}
```

#### 확인 필요

**CrewRepository.java 구현 확인:**
```java
//  안전한 구현 (JPQL with parameter binding)
@Query("SELECT c FROM CrewEntity c JOIN FETCH c.owner " +
       "WHERE c.name LIKE %:name% AND c.isActive = true")
Page<CrewEntity> findByNameContainingWithOwner(@Param("name") String name, Pageable pageable);

//  위험한 구현 (네이티브 쿼리 + 문자열 연결)
// @Query(value = "SELECT * FROM crews WHERE name LIKE '%" + :name + "%'", nativeQuery = true)
// → SQL Injection 가능!

//  안전한 네이티브 쿼리 (parameter binding)
@Query(value = "SELECT * FROM crews WHERE name LIKE CONCAT('%', :name, '%') " +
               "AND is_active = true", nativeQuery = true)
Page<CrewEntity> findByNameContainingWithOwner(@Param("name") String name, Pageable pageable);
```

#### 추가 방어 레이어

**컨트롤러 레벨 입력 검증:**
```java
// CrewController.java
@GetMapping("/search")
public ResponseEntity<Page<CrewListResponse>> searchCrews(
        @Parameter(description = "검색 키워드")
        @RequestParam
        @Pattern(regexp = "^[a-zA-Z0-9가-힣\\s]+$",
                 message = "검색어는 한글, 영문, 숫자, 공백만 가능합니다.")
        @Size(min = 2, max = 50, message = "검색어는 2~50자여야 합니다.")
        String keyword,
        @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
        @AuthUser AuthenticatedUser user) {

    //  특수문자 이스케이프
    String sanitizedKeyword = StringEscapeUtils.escapeHtml4(keyword);

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<CrewEntity> crews = crewService.searchCrewsByName(sanitizedKeyword, pageable);

    // ... 응답 로직 ...
}
```

---

## 📋 보안 개선 우선순위 요약

| 우선순위 | 취약점 | 심각도 | 영향 | 수정 예상 시간 |
|---------|--------|--------|------|---------------|
| **P0** | 크루장 권한 이양 Race Condition |  Critical | 크루 탈취 가능 | 4시간 |
| **P0** | 멤버 추방 Race Condition |  Critical | 멤버 수 오류 | 3시간 |
| **P0** | JWT 권한 유지 문제 |  Critical | 권한 상승 | 6시간 |
| **P0** | 멤버 수 동기화 불일치 |  Critical | 정원 초과 | 5시간 |
| **P1** | 크루 생성 중복 처리 |  Critical | 중복 크루 생성 | 2시간 |
| **P1** | 가입 신청 중복 처리 |  Critical | 중복 신청 | 2시간 |
| **P1** | 크루 삭제 데이터 정합성 |  Critical | 데이터 손실 | 8시간 |
| **P2** | 크루 정보 수정 검증 부족 | 🟠 High | 중복 이름 | 2시간 |
| **P2** | 역할 변경 권한 검증 부족 | 🟠 High | 권한 상승 | 3시간 |
| **P2** | 재신청 차단 없음 | 🟠 High | 스팸 | 2시간 |
| **P2** | 크루 비활성화 후처리 없음 | 🟠 High | 불완전한 상태 | 3시간 |
| **P3** | 채팅 메시지 고아 데이터 | 🟡 Medium | UX 저하 | 1시간 |
| **P3** | SQL Injection (낮음) | 🟡 Medium | 데이터 노출 | 1시간 |

**총 예상 작업 시간:** 약 42시간 (P0: 18시간, P1: 12시간, P2: 10시간, P3: 2시간)

---

## 🔧 즉시 적용 가능한 임시 방어책

### 1. Rate Limiting 추가

```java
// application.yml
spring:
  cache:
    type: redis
  data:
    redis:
      host: localhost
      port: 6379

// RateLimitConfig.java
@Configuration
public class RateLimitConfig {

    @Bean
    public RateLimiter crewCreationRateLimiter() {
        return RateLimiter.create(5.0);  // 초당 5개 요청
    }
}

// RateLimitAspect.java
@Aspect
@Component
@Slf4j
public class RateLimitAspect {

    private final RateLimiter rateLimiter;

    @Around("@annotation(RateLimited)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!rateLimiter.tryAcquire()) {
            throw new RateLimitExceededException("요청이 너무 빈번합니다. 잠시 후 다시 시도해주세요.");
        }
        return joinPoint.proceed();
    }
}

// CrewController.java
@PostMapping
@RateLimited  //  추가
public ResponseEntity<CrewDetailResponse> createCrew(...) {
    // ...
}
```

### 2. 트랜잭션 타임아웃 설정

```java
@Transactional(timeout = 10)  //  10초 타임아웃
public void transferOwnership(AuthenticatedUser user, Long crewId, Long newOwnerId) {
    // ...
}
```

### 3. 낙관적 락 재시도 로직

```java
@Retryable(
    value = {OptimisticLockException.class},
    maxAttempts = 3,
    backoff = @Backoff(delay = 100)
)
@Transactional
public void approveJoinRequest(AuthenticatedUser user, Long requestId) {
    // ...
}
```

---

## 📝 추가 권장사항

### 1. 모니터링 및 알림

```java
// 크루장 권한 이양 시 알림
@EventListener
public void onOwnershipTransferred(OwnershipTransferredEvent event) {
    // 이전 크루장에게 알림
    notificationService.send(event.getOldOwnerId(),
        "크루장 권한이 이양되었습니다.");

    // 새 크루장에게 알림
    notificationService.send(event.getNewOwnerId(),
        "크루장 권한을 받았습니다.");

    // 관리자에게 로그
    adminNotificationService.logOwnershipChange(event);
}
```

### 2. 감사 로그 (Audit Log)

```java
@Entity
@Table(name = "crew_audit_logs")
public class CrewAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long crewId;
    private Long userId;
    private String action;  // TRANSFER_OWNERSHIP, KICK_MEMBER, etc.
    private String details;
    private LocalDateTime timestamp;
}

// AuditLogService.java
public void logAction(Long crewId, Long userId, String action, String details) {
    CrewAuditLog log = new CrewAuditLog();
    log.setCrewId(crewId);
    log.setUserId(userId);
    log.setAction(action);
    log.setDetails(details);
    log.setTimestamp(LocalDateTime.now());
    auditLogRepository.save(log);
}
```

### 3. 정기적인 데이터 무결성 검증

```java
@Scheduled(cron = "0 0 4 * * ?")  // 매일 새벽 4시
public void validateDataIntegrity() {
    // 멤버 수 불일치 검증
    List<CrewEntity> crews = crewRepository.findAll();
    for (CrewEntity crew : crews) {
        long actualCount = crewMemberRepository.countByCrewIdAndIsActiveTrue(crew.getId());
        if (crew.getCurrentMemberCount() != actualCount) {
            log.error("멤버 수 불일치 감지 - crewId: {}, cached: {}, actual: {}",
                     crew.getId(), crew.getCurrentMemberCount(), actualCount);

            // 자동 수정
            crew.setCurrentMembers((int) actualCount);
            crewRepository.save(crew);
        }
    }
}
```

---

##  체크리스트

### P0 (최우선)
- [ ] 크루장 권한 이양 Race Condition 수정
- [ ] 멤버 추방 Race Condition 수정
- [ ] JWT 권한 유지 문제 해결
- [ ] 멤버 수 동기화 불일치 수정

### P1 (높음)
- [ ] 크루 생성 중복 처리 수정
- [ ] 가입 신청 중복 처리 수정
- [ ] 크루 삭제 데이터 정합성 개선

### P2 (중간)
- [ ] 크루 정보 수정 검증 강화
- [ ] 역할 변경 권한 검증 개선
- [ ] 재신청 차단 로직 추가
- [ ] 크루 비활성화 후처리 추가

### P3 (낮음)
- [ ] 채팅 메시지 고아 데이터 처리
- [ ] SQL Injection 검증

### 추가 개선
- [ ] Rate Limiting 추가
- [ ] 감사 로그 시스템 구축
- [ ] 데이터 무결성 검증 스케줄러 구현
- [ ] 모니터링 및 알림 시스템 구축

---

## 📞 문의

보안 이슈에 대한 문의사항은 GitHub Issues에 등록해주세요.

**작성:** Security Team
**검토:** Backend Team
**승인 대기 중**
