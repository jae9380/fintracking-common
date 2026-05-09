# fintracking-common

> 공통 라이브러리 — 모든 MSA 서비스가 공유하는 컴포넌트

- **Group**: `com.ft`
- **Artifact**: `fintracking-common`
- **현재 버전**: `1.1.0`
- **배포 위치**: GitHub Packages (`github.com/jae9380/fintracking-common`)

---

## 개요

이 모듈은 **라이브러리**입니다. 직접 실행되지 않으며, 다른 서비스들이 의존성으로 포함합니다.
예외 처리, API 응답 형식, Kafka 추상화, 메트릭 수집 등 모든 서비스에서 반복될 수 있는 코드를 여기에 모아 일관성을 유지합니다.

```
fintracking-auth        ──┐
fintracking-account     ──┤
fintracking-transaction ──┤── implementation 'com.ft:fintracking-common:1.1.0'
fintracking-budget      ──┤
fintracking-notification──┤
fintracking-batch       ──┘
```

---

## 의존성 추가 방법

```groovy
repositories {
    maven {
        name = 'GitHubPackages'
        url = uri('https://maven.pkg.github.com/jae9380/fintracking-common')
        credentials {
            username = System.getenv('GITHUB_ACTOR')
            password = System.getenv('GITHUB_TOKEN')
        }
    }
}

dependencies {
    implementation 'com.ft:fintracking-common:1.1.0'

    // 메트릭 수집 AOP 활성화 (필수)
    implementation 'org.springframework.boot:spring-boot-starter-aop'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
}
```

---

## 원칙

이 모듈에 들어가면 **안 되는** 것들:

| 금지 항목 | 이유 |
|-----------|------|
| 비즈니스 로직 | 특정 도메인에 종속됨 |
| JPA Entity | 서비스별 DB 분리 원칙 위반 |
| 외부 API 호출 | 특정 서비스에 종속됨 |
| 서비스별 설정값 | 공통 모듈이 특정 서비스를 알면 안 됨 |

---

## 패키지 구조

```
com.ft.common
├── config/
│   └── CommonAutoConfiguration.java      ← Spring Boot 자동 구성 (의존성 추가만으로 활성화)
│
├── exception/
│   ├── ErrorCode.java                    ← 전 서비스 에러 코드 enum
│   ├── CustomException.java              ← 표준 예외 클래스
│   └── GlobalExceptionHandler.java       ← @RestControllerAdvice 전역 처리
│
├── response/
│   ├── ApiResponse.java                  ← 표준 응답 래퍼 (statusCode, message, data)
│   └── ApiResultType.java                ← SUCCESS / ERROR
│
├── entity/
│   └── BaseEntity.java                   ← createdAt, updatedAt 감사 필드
│
├── kafka/
│   ├── KafkaTopic.java                   ← 토픽 이름 상수
│   ├── AbstractEventPublisher.java       ← Kafka Producer 추상 클래스
│   └── EventHandler.java                 ← Kafka Consumer 인터페이스
│
├── event/
│   ├── TransactionCreatedEvent.java      ← 거래 생성 이벤트
│   ├── BudgetAlertEvent.java             ← 예산 알림 이벤트
│   ├── UserRegisteredEvent.java          ← 회원가입 이벤트
│   └── BatchReportEvent.java             ← 배치 완료 이벤트
│
└── metric/
    ├── annotation/
    │   ├── Monitored.java                ← 도메인 서비스 메트릭 어노테이션
    │   └── MonitoredKafka.java           ← Kafka 처리 메트릭 어노테이션
    ├── aspect/
    │   ├── DomainMetricAspect.java       ← @Monitored AOP 인터셉터
    │   └── KafkaMetricAspect.java        ← @MonitoredKafka AOP 인터셉터
    └── helper/
        ├── DomainMetricHelper.java
        ├── KafkaMetricHelper.java
        ├── ExternalApiMetricHelper.java  ← FCM / SMTP / OAuth2 등 외부 API 메트릭
        └── BatchMetricHelper.java        ← 배치 잡 메트릭
```

---

## 제공 기능

### 1. 예외 처리 — CustomException + ErrorCode

모든 서비스가 동일한 방식으로 예외를 던지고, 동일한 형식으로 응답합니다.

```
[서비스 코드]
    throw new CustomException(ErrorCode.ACCOUNT_NOT_FOUND)
                │
                ▼
    GlobalExceptionHandler (@RestControllerAdvice)
                │
                ▼
    HTTP 응답:
    {
      "statusCode": 404,
      "message": "계좌를 찾을 수 없습니다.",
      "data": null
    }
```

**왜 이 방식인가?**
- `RuntimeException`을 직접 쓰면 에러 코드 관리가 분산됨
- 하드코딩 메시지는 오타, 중복, 일관성 깨짐
- `ErrorCode` enum 하나에서 전체 에러를 관리하면 리팩터링이 안전함

```java
// ErrorCode.java — 서비스별 prefix로 그룹화
ACCOUNT_NOT_FOUND(404, "계좌를 찾을 수 없습니다.")
TRANSACTION_NOT_FOUND(404, "거래 내역을 찾을 수 없습니다.")
BUDGET_DUPLICATE(409, "이미 존재하는 예산입니다.")
BATCH_JOB_EXECUTION_FAILED(500, "배치 Job 실행에 실패했습니다.")
```

```java
// 사용 — RuntimeException 직접 사용 금지
throw new CustomException(ErrorCode.ACCOUNT_NOT_FOUND); // O
throw new RuntimeException("계좌 없음");                // X
```

---

### 2. API 응답 형식 — ApiResponse\<T\>

모든 서비스의 HTTP 응답이 동일한 구조를 가집니다.

```java
// 성공 응답
ApiResponse.success(data)
// → { "statusCode": 200, "message": "SUCCESS", "data": { ... } }

// 생성 응답
ApiResponse.created(data)
// → { "statusCode": 201, "message": "CREATED", "data": { ... } }

// 에러 응답 (GlobalExceptionHandler에서 자동 처리)
// → { "statusCode": 404, "message": "계좌를 찾을 수 없습니다.", "data": null }
```

---

### 3. BaseEntity — 감사 필드 자동화

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate
    private LocalDateTime createdAt;   // 최초 저장 시 자동 설정

    @LastModifiedDate
    private LocalDateTime updatedAt;   // 수정 시 자동 갱신
}

// 상속만 하면 적용됨
public class Account extends BaseEntity { ... }
```

---

### 4. Kafka 추상화

#### 전체 이벤트 흐름

```
[transaction-service]
    TransactionEventPublisher.publish(event)
            │ topic: transaction.created
            ▼
[account-service]           [budget-service]
BalanceUpdateEventHandler   BudgetAlertEventListener
.handle(event)              .handle(event)
            │
    (budget-service가 임계값 초과 감지)
            │ topic: budget.alert
            ▼
[notification-service]
BudgetAlertEventHandler.handle(event)
```

#### KafkaTopic — 토픽 이름 상수

```java
// 문자열 직접 사용 금지 → 오타/변경 시 컴파일 에러로 즉시 발견
KafkaTopic.TRANSACTION_CREATED  // "transaction.created"
KafkaTopic.BUDGET_ALERT         // "budget.alert"
KafkaTopic.USER_REGISTERED      // "user.registered"
KafkaTopic.BATCH_REPORT         // "batch.report"
```

#### AbstractEventPublisher\<T\> — Producer 공통 로직

```java
// 자식 클래스가 topic()만 구현하면 publish() 로직은 공통 상속
public class TransactionEventPublisher extends AbstractEventPublisher<TransactionCreatedEvent> {
    @Override
    public String topic() { return KafkaTopic.TRANSACTION_CREATED; }
}

// 발행
publisher.publish(event); // → KafkaTemplate.send(topic(), event)
```

#### EventHandler\<T\> — Consumer 인터페이스

```java
@Component
public class BudgetAlertEventHandler implements EventHandler<BudgetAlertEvent> {
    @Override
    @KafkaListener(topics = KafkaTopic.BUDGET_ALERT, groupId = "notification-service")
    public void handle(BudgetAlertEvent event) { ... }
}
```

#### Kafka 이벤트 레코드

| 이벤트 | 토픽 | Producer → Consumer |
|--------|------|---------------------|
| `TransactionCreatedEvent` | `transaction.created` | transaction → account, budget |
| `BudgetAlertEvent` | `budget.alert` | budget → notification |
| `UserRegisteredEvent` | `user.registered` | auth → batch |
| `BatchReportEvent` | `batch.report` | batch → notification |

---

### 5. 메트릭 수집

Micrometer 기반으로 Prometheus 메트릭을 자동 수집합니다.
`MeterRegistry` Bean이 있을 때만 조건부로 활성화됩니다.

#### @Monitored — 서비스 레이어

```java
@Monitored(domain = "account", layer = "service", api = "create")
public AccountResult createAccount(CreateAccountCommand command) {
    // DomainMetricAspect가 성공/실패 카운터 + 실행 시간 자동 기록
}
```

수집 메트릭:
- `ft_domain_requests_total{domain, layer, api, result="success"|"fail"}`
- `ft_domain_request_duration_seconds{domain, layer, api}` — P50/P95/P99

#### @MonitoredKafka — Kafka Consumer

```java
@MonitoredKafka(topic = KafkaTopic.TRANSACTION_CREATED, action = "consume")
public void handle(TransactionCreatedEvent event) {
    // KafkaMetricAspect가 처리 횟수 + 시간 자동 기록
}
```

수집 메트릭:
- `ft_kafka_events_total{topic, action, result, error_type?}`
- `ft_kafka_event_duration_seconds{topic, action}`

> `AbstractEventPublisher.publish()`는 어노테이션 없이 KafkaMetricAspect가 자동으로 인터셉트합니다.

#### ExternalApiMetricHelper — 외부 API 수동 수집

FCM, SMTP, OAuth2 등 외부 API 호출에 직접 주입해서 사용합니다.

```java
Timer.Sample sample = metricHelper.startSample();
try {
    // 외부 API 호출
    metricHelper.success("kakao", "get_access_token").increment();
} catch (Exception e) {
    metricHelper.fail("kakao", "get_access_token", e.getClass().getSimpleName()).increment();
} finally {
    sample.stop(metricHelper.timer("kakao", "get_access_token"));
}
```

수집 메트릭:
- `ft_external_api_requests_total{system, operation, result, error_type?}`
- `ft_external_api_duration_seconds{system, operation}`

---

### 6. 자동 설정 — CommonAutoConfiguration

`spring.factories` / `AutoConfiguration.imports`에 등록되어
**의존성 추가만으로** 아래 빈들이 자동으로 등록됩니다.

| Bean | 등록 조건 |
|------|-----------|
| `GlobalExceptionHandler` | 항상 등록 |
| `DomainMetricHelper` | `MeterRegistry` Bean 존재 시 |
| `KafkaMetricHelper` | `MeterRegistry` Bean 존재 시 |
| `ExternalApiMetricHelper` | `MeterRegistry` Bean 존재 시 |
| `BatchMetricHelper` | `MeterRegistry` Bean 존재 시 |
| `DomainMetricAspect` | `DomainMetricHelper` Bean 존재 시 |
| `KafkaMetricAspect` | `KafkaMetricHelper` Bean 존재 시 |

---

## 새 이벤트 추가 방법

```
1. event/ 패키지에 record 클래스 추가
       record NewEvent(String eventId, Long userId, ...)

2. KafkaTopic에 상수 추가
       public static final String NEW_EVENT = "new.event";

3. Producer 서비스에서 구현
       class NewEventPublisher extends AbstractEventPublisher<NewEvent> {
           public String topic() { return KafkaTopic.NEW_EVENT; }
       }

4. Consumer 서비스에서 구현
       class NewEventHandler implements EventHandler<NewEvent> {
           @KafkaListener(topics = KafkaTopic.NEW_EVENT, groupId = "...")
           public void handle(NewEvent event) { ... }
       }
```

---

## 버전 이력

| 버전 | 주요 변경 내용 |
|:----:|---------------|
| `1.1.0` | 메트릭 수집 기능 추가 (`@Monitored`, `@MonitoredKafka`, Helper 4종, Aspect 2종, `CommonAutoConfiguration` 확장) |

---

## 테스트

```
test/
├── exception/
│   └── GlobalExceptionHandlerTest.java        ← 에러 응답 형식 검증
└── metric/
    ├── aspect/
    │   ├── DomainMetricAspectTest.java         ← AOP 메트릭 수집 검증
    │   └── KafkaMetricAspectTest.java
    └── helper/
        ├── DomainMetricHelperTest.java
        ├── KafkaMetricHelperTest.java
        ├── ExternalApiMetricHelperTest.java
        └── BatchMetricHelperTest.java
```
