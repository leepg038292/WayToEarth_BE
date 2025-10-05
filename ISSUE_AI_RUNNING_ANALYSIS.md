---
name: create issue
about: 프로젝트 작업에 필요한 이슈를 등록합니다
title: "[FEAT] OpenAI 기반 러닝 기록 AI 분석 및 피드백 시스템 구현"
labels: 'feature, enhancement'
assignees: ''

---

## 🤔 기능 설명
> OpenAI API를 활용하여 사용자의 러닝 기록을 AI로 분석하고, 데이터 기반의 구체적인 피드백을 제공하는 시스템을 구현합니다.
>
> **핵심 가치**: 단순한 칭찬이 아닌, 과거 기록과 비교한 **성장 패턴 분석**과 **구체적인 개선 목표 제시**를 통해 사용자의 러닝 동기를 부여합니다.

## 💻 작업 상세 내용
- [x] OpenAI API 연동 및 설정
  - [x] `build.gradle`에 OpenAI 라이브러리 의존성 추가
  - [x] `application.yml`에 OpenAI 설정 추가 (모델, 토큰, 타임아웃 등)
  - [x] `.env`에 API 키 설정
- [x] 데이터베이스 설계
  - [x] `RunningFeedback` 엔티티 생성 (AI 분석 결과 저장)
  - [x] `RunningFeedbackRepository` 구현
- [x] AI 분석 서비스 구현
  - [x] `OpenAIService`: OpenAI Chat Completion API 호출 로직
  - [x] `RunningAnalysisService`: 러닝 데이터 분석 및 프롬프트 설계
- [x] 비즈니스 로직 구현
  - [x] 최소 5회 이상 완료 기록 검증
  - [x] 과거 10개 러닝 기록 조회 및 통계 계산
  - [x] 평균 거리, 평균 페이스, 최고 기록 제공
  - [x] AI 피드백 캐싱 (중복 분석 방지)
- [x] 프롬프트 품질 개선
  - [x] 시스템 프롬프트: 데이터 기반 분석, 성장 패턴 파악, 구체적 목표 제시
  - [x] 사용자 프롬프트: 현재 기록 + 과거 통계 제공
- [x] API 엔드포인트 구현
  - [x] `POST /v1/running/analysis/{runningRecordId}` - AI 분석 실행
  - [x] `GET /v1/running/analysis/{runningRecordId}` - 피드백 조회
- [x] 에러 핸들링
  - [x] `OpenAIServiceException` 커스텀 예외 생성
  - [x] API 키 검증, HTTP 에러 상세 로깅
  - [x] `GlobalExceptionHandler`에 예외 핸들러 추가
- [x] DTO 생성
  - [x] `RunningAnalysisRequest`
  - [x] `RunningAnalysisResponse`

## 📋 기술적 요구사항
- **최소 완료 기록**: 5회 이상 (설정 가능)
- **과거 기록 참조**: 최근 10개 러닝 기록
- **AI 모델**: GPT-3.5-turbo (기본값, 설정 변경 가능)
- **응답 길이**: 4-6문장, 최대 800 토큰
- **피드백 캐싱**: 동일 기록에 대한 중복 분석 방지
- **토큰 사용량 저장**: 비용 추적을 위한 토큰 수 DB 저장

## 📊 향후 확장 계획
- [ ] 케이던스 데이터 분석
- [ ] 심박수 기반 운동 강도 평가
- [ ] 페이스 변화 패턴 분석
- [ ] 주간/월간 성장 리포트

## 참고할 수 있는 자료
- OpenAI Chat Completion API: https://platform.openai.com/docs/api-reference/chat
- 라이브러리: https://github.com/TheoKanning/openai-java
