package com.ft.common.metric.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Kafka Consumer 메트릭 수집 어노테이션.
 * {@code @KafkaListener} 메서드에 함께 선언하면 이벤트 처리 성공/실패 카운터와
 * 처리 시간을 자동으로 기록한다.
 *
 * <p>Kafka Producer(publish) 메트릭은 {@code AbstractEventPublisher}에서
 * KafkaMetricAspect가 자동으로 수집하므로 별도 선언이 불필요하다.</p>
 *
 * <pre>
 * 생성 메트릭:
 *   ft_kafka_events_total{topic, action, result}
 *   ft_kafka_event_duration_seconds{topic, action}
 * </pre>
 *
 * 사용 예:
 * <pre>
 * {@code @MonitoredKafka(topic = KafkaTopic.TRANSACTION_CREATED, action = "consume")}
 * {@code @KafkaListener(topics = KafkaTopic.TRANSACTION_CREATED, groupId = "account-service")}
 * public void handle(TransactionCreatedEvent event) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MonitoredKafka {

    /** Kafka 토픽 이름 (예: "transaction.created", "budget.alert") */
    String topic();

    /** 액션 구분 — Consumer: "consume" */
    String action();
}
