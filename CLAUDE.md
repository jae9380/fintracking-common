# fintracking-common

공유 라이브러리 — 모든 MSA 서비스의 공통 의존성

---

## 원칙

- 비즈니스 로직 포함 금지
- JPA Entity 포함 금지
- 외부 API 호출 포함 금지
- 허용: 예외, 응답, 유틸, 열거형, Kafka 이벤트/추상화

---

## 제공 컴포넌트

### 예외

```java
CustomException(ErrorCode errorCode)
ErrorCode  // enum: status + code + message
```

### 응답

```java
ApiResponse<T>  // statusCode, message, data
```

### Kafka 추상화

```java
// Producer 추상 클래스
abstract class AbstractEventPublisher<T> {
    protected abstract String topic();
    public void publish(T event) { kafkaTemplate.send(topic(), event); }
}

// Consumer 인터페이스
interface EventHandler<T> {
    void handle(T event);
}
```

### Kafka 토픽 상수

```java
class KafkaTopic {
    static final String TRANSACTION_CREATED = "transaction.created";
    static final String BUDGET_ALERT = "budget.alert";
}
```

### Kafka 이벤트 레코드

```java
// 거래 생성 이벤트
record TransactionCreatedEvent(
    String eventId, Long userId, Long accountId, Long toAccountId,
    Long transactionId, BigDecimal amount, String type,
    Long categoryId, LocalDateTime occurredAt
)

// 예산 알림 이벤트
record BudgetAlertEvent(
    String eventId, Long userId, Long categoryId, String alertType,
    BigDecimal budgetAmount, BigDecimal spentAmount, String yearMonth
)
```

---

## 의존성 추가 방법

각 서비스 `build.gradle`:
```groovy
dependencies {
    implementation project(':fintracking-common')
}
```

---

## 새 이벤트 추가 시

1. `record` 클래스 이 모듈에 추가
2. `KafkaTopic`에 토픽 상수 추가
3. Producer 서비스: `AbstractEventPublisher<NewEvent>` 구현
4. Consumer 서비스: `EventHandler<NewEvent>` 구현 + `@KafkaListener` 추가
