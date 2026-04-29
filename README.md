# fintracking-common

FinTracking MSA 프로젝트의 공유 라이브러리입니다.
모든 마이크로서비스가 공통으로 사용하는 예외, 응답, Kafka 추상화, 메트릭 수집 기능을 제공합니다.

- **Group**: `com.ft`
- **Artifact**: `fintracking-common`
- **현재 버전**: `1.1.0`
- **배포 위치**: GitHub Packages (`github.com/jae9380/fintracking-common`)

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

## 파일 구조

```
fintracking-common/
└── src/
    ├── main/java/com/ft/common/
    │   │
    │   ├── config/
    │   │   └── CommonAutoConfiguration.java      # 전체 Bean 자동 등록 (Spring Auto-configuration)
    │   │
    │   ├── exception/
    │   │   ├── ErrorCode.java                    # 전 서비스 에러 코드 열거형
    │   │   ├── CustomException.java              # 공통 런타임 예외
    │   │   └── GlobalExceptionHandler.java       # @RestControllerAdvice 전역 예외 처리
    │   │
    │   ├── response/
    │   │   ├── ApiResponse.java                  # 표준 응답 래퍼 (statusCode, message, data)
    │   │   └── ApiResultType.java                # 응답 유형 열거형 (SUCCESS / ERROR)
    │   │
    │   ├── entity/
    │   │   └── BaseEntity.java                   # createdAt, updatedAt 감사 필드
    │   │
    │   ├── kafka/
    │   │   ├── KafkaTopic.java                   # Kafka 토픽 이름 상수
    │   │   ├── AbstractEventPublisher.java        # Kafka Producer 추상 클래스
    │   │   └── EventHandler.java                 # Kafka Consumer 인터페이스
    │   │
    │   ├── event/
    │   │   ├── TransactionCreatedEvent.java       # 거래 생성 이벤트 (topic: transaction.created)
    │   │   ├── BudgetAlertEvent.java              # 예산 알림 이벤트 (topic: budget.alert)
    │   │   ├── UserRegisteredEvent.java           # 회원가입 이벤트 (topic: user.registered)
    │   │   └── BatchReportEvent.java             # 배치 완료 이벤트 (topic: batch.report)
    │   │
    │   └── metric/
    │       ├── annotation/
    │       │   ├── Monitored.java                 # 서비스 레이어 메트릭 수집 어노테이션
    │       │   └── MonitoredKafka.java            # Kafka Consumer 메트릭 수집 어노테이션
    │       ├── aspect/
    │       │   ├── DomainMetricAspect.java        # @Monitored AOP 인터셉터
    │       │   └── KafkaMetricAspect.java         # @MonitoredKafka + Publisher 자동 인터셉터
    │       └── helper/
    │           ├── DomainMetricHelper.java        # 도메인 메트릭 카운터/타이머 빌더
    │           ├── KafkaMetricHelper.java         # Kafka 메트릭 카운터/타이머 빌더
    │           ├── ExternalApiMetricHelper.java   # 외부 API 메트릭 (FCM/SMTP/OAuth2 등)
    │           └── BatchMetricHelper.java         # 배치 잡 메트릭
    │
    └── test/java/com/ft/common/
        ├── exception/
        │   └── GlobalExceptionHandlerTest.java
        └── metric/
            ├── helper/
            │   ├── DomainMetricHelperTest.java
            │   ├── KafkaMetricHelperTest.java
            │   ├── ExternalApiMetricHelperTest.java
            │   └── BatchMetricHelperTest.java
            └── aspect/
                ├── DomainMetricAspectTest.java
                └── KafkaMetricAspectTest.java
```

---

## 제공 기능

### 1. 예외 처리

모든 서비스에서 일관된 예외 처리를 위한 표준 패턴을 제공합니다.

**ErrorCode** — 전 서비스 에러 코드 열거형

```java
// 서비스별 에러 코드 네이밍: {SERVICE}_{ERROR_TYPE}
AUTH_USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다.")
ACCOUNT_INSUFFICIENT_BALANCE(400, "잔액이 부족합니다.")
TRANSACTION_NOT_FOUND(404, "거래 내역을 찾을 수 없습니다.")
BUDGET_DUPLICATE(409, "이미 존재하는 예산입니다.")
NOTIFICATION_SEND_FAILED(400, "메일 발송을 실패했습니다.")
BATCH_JOB_EXECUTION_FAILED(500, "배치 Job 실행에 실패했습니다.")
```

**CustomException** — 공통 런타임 예외

```java
// 사용
throw new CustomException(ErrorCode.ACCOUNT_NOT_FOUND);

// RuntimeException 직접 사용 금지
// 하드코딩 메시지 금지
```

**GlobalExceptionHandler** — 전역 예외 처리

`@RestControllerAdvice`로 `CustomException`과 `MethodArgumentNotValidException`을 `ApiResponse` 형태로 응답합니다.
`CommonAutoConfiguration`에 의해 자동으로 Bean 등록됩니다.

---

### 2. 표준 응답

**ApiResponse\<T\>** — 모든 API 응답의 표준 래퍼

```java
// 성공 응답
ApiResponse.success(data)
// → { "statusCode": 200, "message": "SUCCESS", "data": { ... } }

// 실패 응답
ApiResponse.error(errorCode)
// → { "statusCode": 404, "message": "사용자를 찾을 수 없습니다.", "data": null }
```

---

### 3. 공통 엔티티

**BaseEntity** — JPA 감사 필드 상속용 추상 클래스

```java
@MappedSuperclass
public abstract class BaseEntity {
    private LocalDateTime createdAt;  // @CreatedDate 자동 설정
    private LocalDateTime updatedAt;  // @LastModifiedDate 자동 설정
}
```

---

### 4. Kafka 추상화

서비스 간 이벤트 기반 통신을 위한 Producer/Consumer 추상화입니다.

**KafkaTopic** — 토픽 이름 상수

```java
KafkaTopic.TRANSACTION_CREATED  // "transaction.created"
KafkaTopic.BUDGET_ALERT         // "budget.alert"
KafkaTopic.USER_REGISTERED      // "user.registered"
KafkaTopic.BATCH_REPORT         // "batch.report"
```

**AbstractEventPublisher\<T\>** — Kafka Producer 추상 클래스

```java
@Component
public class TransactionEventPublisher extends AbstractEventPublisher<TransactionCreatedEvent> {

    public TransactionEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        super(kafkaTemplate);
    }

    @Override
    public String topic() {
        return KafkaTopic.TRANSACTION_CREATED;
    }
}

// 발행
publisher.publish(event);
// → KafkaMetricAspect가 publish() 자동 인터셉트 → 메트릭 자동 수집
```

**EventHandler\<T\>** — Kafka Consumer 인터페이스

```java
@Component
public class BudgetAlertEventHandler implements EventHandler<BudgetAlertEvent> {

    @Override
    @KafkaListener(topics = KafkaTopic.BUDGET_ALERT, groupId = "notification-service")
    @MonitoredKafka(topic = KafkaTopic.BUDGET_ALERT, action = "consume")
    public void handle(BudgetAlertEvent event) {
        // 이벤트 처리
    }
}
```

**Kafka 이벤트 레코드**

| 이벤트                    | 토픽                  | 주요 필드                                                           |
| ------------------------- | --------------------- | ------------------------------------------------------------------- |
| `TransactionCreatedEvent` | `transaction.created` | userId, accountId, amount, type, categoryId, occurredAt             |
| `BudgetAlertEvent`        | `budget.alert`        | userId, categoryId, alertType, budgetAmount, spentAmount, yearMonth |
| `UserRegisteredEvent`     | `user.registered`     | userId, email, registeredAt                                         |
| `BatchReportEvent`        | `batch.report`        | yearMonth, totalUsers, executedAt                                   |

---

### 5. 메트릭 수집

Micrometer 기반 메트릭 수집 기능입니다.
`MeterRegistry` Bean이 존재할 때만(`@ConditionalOnBean`) 자동으로 활성화됩니다.

#### @Monitored — 서비스 레이어 자동 수집

```java
@Monitored(domain = "transaction", layer = "service", api = "create")
public TransactionResult create(CreateTransactionCommand command) {
    // DomainMetricAspect가 성공/실패 카운터 + 실행시간 자동 기록
}
```

수집 메트릭:

- `ft_domain_requests_total{domain, layer, api, result="success"|"fail"}`
- `ft_domain_request_duration_seconds{domain, layer, api}` — P50/P95/P99 히스토그램

#### @MonitoredKafka — Kafka Consumer 자동 수집

```java
@MonitoredKafka(topic = KafkaTopic.TRANSACTION_CREATED, action = "consume")
public void handle(TransactionCreatedEvent event) {
    // KafkaMetricAspect가 성공/실패 카운터 + 처리시간 자동 기록
}
```

수집 메트릭:

- `ft_kafka_events_total{topic, action="publish"|"consume", result, error_type?}`
- `ft_kafka_event_duration_seconds{topic, action}`

> `AbstractEventPublisher.publish()`는 어노테이션 없이 자동으로 인터셉트됩니다.

#### ExternalApiMetricHelper — 외부 API 수동 수집

FCM, SMTP, OAuth2 등 외부 시스템 호출 지점에 직접 주입합니다.

```java
@RequiredArgsConstructor
public class KakaoOAuth2Client {

    private final ExternalApiMetricHelper metricHelper;

    public String getAccessToken(String code) {
        Timer.Sample sample = metricHelper.startSample();
        try {
            // 외부 API 호출 ...
            metricHelper.success("kakao", "get_access_token").increment();
            return token;
        } catch (HttpClientErrorException e) {
            metricHelper.fail("kakao", "get_access_token", "HttpClientErrorException").increment();
            throw new CustomException(AUTH_OAUTH2_FAILED);
        } finally {
            sample.stop(metricHelper.timer("kakao", "get_access_token"));
        }
    }
}
```

수집 메트릭:

- `ft_external_api_requests_total{system, operation, result, error_type?}`
- `ft_external_api_duration_seconds{system, operation}`

#### BatchMetricHelper — 배치 잡 수동 수집

```java
public class MetricBatchDecorator extends AbstractBatchDecorator {

    private final BatchMetricHelper metricHelper;

    @Override
    public void execute(YearMonth yearMonth) {
        long startMs = System.currentTimeMillis();
        String result = "success";
        try {
            delegate.execute(yearMonth);
            metricHelper.success("monthlyStatisticsJob");
        } catch (Exception e) {
            result = "fail";
            metricHelper.fail("monthlyStatisticsJob", e.getClass().getSimpleName());
            throw e;
        } finally {
            metricHelper.recordDuration("monthlyStatisticsJob", result, System.currentTimeMillis() - startMs);
        }
    }
}
```

수집 메트릭:

- `ft_batch_job_total{job, result, reason?}`
- `ft_batch_job_duration_seconds{job, result}`

---

### 6. 자동 설정 (CommonAutoConfiguration)

`spring.factories` / `AutoConfiguration.imports`에 등록되어 의존성 추가만으로 자동 활성화됩니다.

| Bean                      | 등록 조건                                           |
| ------------------------- | --------------------------------------------------- |
| `GlobalExceptionHandler`  | 항상 등록                                           |
| `DomainMetricHelper`      | `MeterRegistry` Bean 존재 시                        |
| `KafkaMetricHelper`       | `MeterRegistry` Bean 존재 시                        |
| `ExternalApiMetricHelper` | `MeterRegistry` Bean 존재 시                        |
| `BatchMetricHelper`       | `MeterRegistry` Bean 존재 시                        |
| `DomainMetricAspect`      | `MeterRegistry` + `DomainMetricHelper` Bean 존재 시 |
| `KafkaMetricAspect`       | `KafkaMetricHelper` Bean 존재 시                    |

---

## 사용 원칙

```
허용                              금지
─────────────────────────────     ─────────────────────────────
예외 / 응답 / 유틸 / 열거형        비즈니스 로직
Kafka 이벤트 / 추상화             JPA Entity
메트릭 수집 헬퍼 / 어노테이션      외부 API 직접 호출
공통 감사 필드 (BaseEntity)        서비스 전용 의존성
```

---

## 버전 이력

|  버전   |                                                 주요 변경 내용                                                  |
| :-----: | :-------------------------------------------------------------------------------------------------------------: |
| `1.1.0` | 메트릭 수집 기능 추가 (`@Monitored`, `@MonitoredKafka`, Helper 4종, Aspect 2종, `CommonAutoConfiguration` 확장) |
