## #️⃣ 연관 이슈
> 관련 이슈 번호로 교체 필요

### PR 타입(하나 이상의 PR 타입을 선택해주세요)
- [x] 기능 추가
- [ ] 기능 삭제
- [ ] 버그 수정
- [x] 의존성, 환경 변수, 빌드 관련 코드 업데이트

## 📝 작업 내용

### ✨ 주요 변경사항
OpenAI API를 활용한 러닝 기록 AI 분석 및 피드백 시스템을 구현했습니다.

**핵심 기능**:
- 과거 러닝 기록 대비 성장 패턴 분석
- 구체적 수치 기반 피드백 제공
- 데이터 기반 개선 목표 제시

### 🔧 구현 상세

#### 1. 의존성 및 설정 추가
- [x] `build.gradle`: OpenAI Java 라이브러리 추가 (`com.theokanning.openai-gpt3-java:service:0.18.2`)
- [x] `application.yml`: OpenAI 설정 추가
  ```yaml
  openai:
    api-key: ${OPENAI_API_KEY}
    model: ${OPENAI_MODEL:gpt-3.5-turbo}
    max-tokens: ${OPENAI_MAX_TOKENS:800}
    temperature: ${OPENAI_TEMPERATURE:0.7}
    timeout-seconds: ${OPENAI_TIMEOUT:30}
    min-completed-records: ${OPENAI_MIN_RECORDS:5}
  ```
- [x] `.env`: `OPENAI_API_KEY` 환경 변수 추가

#### 2. 엔티티 및 Repository
- [x] `RunningFeedback.java`: AI 분석 결과 저장 엔티티
  - OneToOne 관계로 `RunningRecord`와 연결
  - 토큰 사용량 추적 (promptTokens, completionTokens, totalTokens)
  - 모델명 저장
- [x] `RunningFeedbackRepository.java`: 피드백 조회용 Repository

#### 3. 서비스 레이어
- [x] `OpenAIService.java`: OpenAI API 연동
  - Chat Completion API 호출
  - API 키 검증
  - 타임아웃 설정 (기본 30초)
  - 상세 에러 핸들링 (OpenAiHttpException 구분)

- [x] `RunningAnalysisService.java`: 러닝 분석 비즈니스 로직
  - **5회 이상 완료 기록 검증**: 충분한 데이터 확보 후 분석
  - **과거 기록 비교 분석**: 최근 10개 기록 통계 계산
    - 평균 거리, 평균 페이스
    - 최장 거리, 최고 페이스
  - **피드백 캐싱**: 동일 기록 재분석 방지
  - **프롬프트 설계**:
    - 시스템 프롬프트: 데이터 기반 코칭, 성장 패턴 분석, 구체적 목표 제시
    - 사용자 프롬프트: 현재 기록 + 과거 통계 제공

#### 4. API 엔드포인트
- [x] `RunningAnalysisController.java`
  - `POST /v1/running/analysis/{runningRecordId}`: AI 분석 실행
  - `GET /v1/running/analysis/{runningRecordId}`: 피드백 조회
  - Swagger 문서화 완료

#### 5. DTO
- [x] `RunningAnalysisRequest.java`: 분석 요청
- [x] `RunningAnalysisResponse.java`: 분석 결과
  - feedbackId, runningRecordId
  - feedbackContent (AI 생성 텍스트)
  - createdAt, modelName

#### 6. 예외 처리
- [x] `OpenAIServiceException.java`: OpenAI API 관련 커스텀 예외
- [x] `GlobalExceptionHandler.java`: 예외 핸들러 추가
  - HTTP 503 (Service Unavailable) 반환
  - 사용자 친화적 에러 메시지

### 📊 AI 피드백 품질 개선

**Before (단순 분석)**:
```
"오늘 5km를 30분에 달렸네요. 잘했어요! 다음엔 더 빨리 달려보세요."
```

**After (데이터 기반 분석)**:
```
"오늘 5.2km를 29분 30초에 완주했네! 이전 평균 4.8km보다 400m 더 달렸고,
평균 페이스는 5:40/km로 지난주 대비 15초 단축됐어. 특히 최장 거리 기록을
경신한 점이 인상적이야. 다음 목표로 6km 도전하면서 페이스 5:30/km를
유지해보는 건 어때? 꾸준히 성장하고 있으니 이 페이스 유지하면 좋겠어!"
```

### 🎯 비즈니스 로직 검증

#### 검증 단계
1. **사용자 존재 확인**: UserRepository로 조회
2. **러닝 기록 권한 검증**: 본인의 기록만 분석 가능
3. **완료 여부 확인**: 미완료 기록은 분석 불가
4. **최소 기록 수 검증**: 5회 이상 완료 기록 필요
5. **피드백 캐싱 확인**: 이미 분석된 기록은 재사용

### 🔐 보안 및 안정성
- API 키 검증: 서비스 초기화 시 필수 체크
- 타임아웃 설정: 장시간 대기 방지
- 에러 로깅: 상세한 에러 정보 기록
- 권한 검증: 타인의 러닝 기록 접근 차단

### 📁 파일 구조
```
src/main/java/com/waytoearth/
├── controller/v1/running/
│   └── RunningAnalysisController.java (NEW)
├── service/ai/
│   ├── OpenAIService.java (NEW)
│   └── RunningAnalysisService.java (NEW)
├── entity/running/
│   └── RunningFeedback.java (NEW)
├── repository/running/
│   └── RunningFeedbackRepository.java (NEW)
├── dto/request/running/ai/
│   └── RunningAnalysisRequest.java (NEW)
├── dto/response/running/ai/
│   └── RunningAnalysisResponse.java (NEW)
└── exception/
    ├── OpenAIServiceException.java (NEW)
    └── GlobalExceptionHandler.java (UPDATED)

src/main/resources/
└── application.yml (UPDATED)

build.gradle (UPDATED)
.env (UPDATED)
```

## 💬 리뷰 요구사항

### 특별히 검토해주세요
1. **프롬프트 품질**: 시스템/사용자 프롬프트가 실제 사용자에게 도움이 될지
   - `RunningAnalysisService.java` 115-138줄 (시스템 프롬프트)
   - `RunningAnalysisService.java` 144-202줄 (사용자 프롬프트)

2. **비용 최적화**:
   - 피드백 캐싱 로직이 적절한지 (72-76줄)
   - 토큰 사용량 저장 로직 확인 (104-106줄)

3. **에러 핸들링**:
   - OpenAI API 실패 시 사용자 경험 (OpenAIService.java 83-89줄)
   - 5회 미만 기록 시 에러 메시지 적절성 (RunningAnalysisService.java 65-70줄)

4. **성능**:
   - 최근 10개 기록 조회 쿼리 효율성 (84-89줄)
   - N+1 문제 가능성 검토

### 테스트 가이드
1. **환경 설정**
   ```bash
   # .env에 실제 OpenAI API 키 추가
   OPENAI_API_KEY=sk-xxxxx
   ```

2. **테스트 시나리오**
   - [ ] 5회 미만 기록: 에러 메시지 확인
   - [ ] 5회 이상 기록: AI 피드백 생성 확인
   - [ ] 동일 기록 재요청: 캐싱된 결과 반환 확인
   - [ ] 타인의 기록 분석 시도: 권한 에러 확인
   - [ ] OpenAI API 키 없이 실행: 초기화 실패 확인

3. **API 호출 예시**
   ```bash
   # AI 분석 요청
   POST /v1/running/analysis/123
   Authorization: Bearer {token}

   # 응답 예시
   {
     "success": true,
     "data": {
       "feedbackId": 1,
       "runningRecordId": 123,
       "feedbackContent": "오늘 5.2km를 29분 30초에 완주했네! ...",
       "createdAt": "2025-10-05T12:34:56",
       "modelName": "gpt-3.5-turbo"
     },
     "message": "AI 분석이 완료되었습니다."
   }
   ```

## ⚠️ 주의사항
- **OpenAI API 키 필수**: `.env`에 `OPENAI_API_KEY` 설정 필요
- **비용 발생**: 토큰 사용량에 따라 비용 발생 (GPT-3.5-turbo 기준 저렴)
- **응답 속도**: OpenAI API 호출로 인해 2-5초 소요 가능
- **GitHub Secrets 설정**: 배포 환경에서는 GitHub Secrets에 API 키 저장

## 🚀 향후 개선 사항
- [ ] 케이던스 데이터 추가 시 프롬프트 확장
- [ ] 심박수 데이터 추가 시 운동 강도 분석
- [ ] 주간/월간 성장 리포트 생성
- [ ] 다국어 피드백 지원
- [ ] AI 모델 업그레이드 (GPT-4)
