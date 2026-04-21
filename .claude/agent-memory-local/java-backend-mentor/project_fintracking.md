---
name: fintracking-msa project overview
description: MSA 전환 프로젝트 전반 정보 — 도메인, 기술 스택, 모듈 구조, 아키텍처 원칙
type: project
---

## 프로젝트 개요
- 모놀리스(fintracking) → MSA(msa-fintracking) 전환 포트폴리오 프로젝트
- 도메인: 개인 금융 지출 추적 (FinTracking)

## 기술 스택
- Java 21, Spring Boot 3.5.x
- Spring Cloud Netflix Eureka (서비스 디스커버리)
- PostgreSQL (각 서비스별 독립 DB)
- Kafka + Zookeeper (서비스 간 비동기 통신, RestTemplate/Feign 금지)
- JWT (jjwt 0.12.3)
- fintracking-common 공유 라이브러리 (GitHub Packages에서 배포, 버전 1.0.2)

## 주요 모듈
- fintracking-auth: 인증/인가 (포트 0, 랜덤 할당, Eureka 등록)
- fintracking-common: 공유 라이브러리 (exception, response, entity, util만 허용 — 비즈니스 로직 금지)

## 아키텍처 원칙
- DDD 레이어: domain -> application -> infrastructure -> presentation
- 패키지: com.ft.back.* (모놀리스) → com.ft.* (MSA 각 서비스)
- 모듈 간 직접 호출 금지, Kafka 이벤트만 허용
- common 패키지 경로: com.ft.back.common.* → com.ft.common.*

## 모놀리스 경로
- /Users/jae/Desktop/git/msa-fintracking/fintracking/back/src/main/java/com/ft/back/

## MSA 서비스 경로
- /Users/jae/Desktop/git/msa-fintracking/fintracking-auth/
- /Users/jae/Desktop/git/msa-fintracking/fintracking-common/

## 설계 패턴 (per module)
- fintracking-auth: Template Method (AbstractAuthHandler)
- fintracking-account: Factory + Strategy (AES256/RSA 암호화 전략)
- fintracking-transaction: DDD + Event (Kafka 이벤트 발행)
- fintracking-budget: Chain of Responsibility (50/80/100% 임계값)
- fintracking-notification: Observer (Kafka 이벤트 수신)
- fintracking-batch: Decorator
