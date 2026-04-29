package com.ft.common.metric.aspect;

import com.ft.common.kafka.AbstractEventPublisher;
import com.ft.common.metric.annotation.MonitoredKafka;
import com.ft.common.metric.helper.KafkaMetricHelper;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * Kafka 이벤트 메트릭 수집 AOP Aspect.
 *
 * <p>두 가지 포인트컷을 처리한다:</p>
 * <ul>
 *   <li>{@code @MonitoredKafka} — Consumer {@code handle()} 메서드 인터셉트</li>
 *   <li>{@code AbstractEventPublisher.publish()} — 모든 Producer 자동 인터셉트</li>
 * </ul>
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class KafkaMetricAspect {

    private final KafkaMetricHelper metricHelper;

    /**
     * {@code @MonitoredKafka} 선언 메서드 (Consumer) 인터셉트.
     */
    @Around("@annotation(monitoredKafka)")
    public Object measureConsume(ProceedingJoinPoint joinPoint, MonitoredKafka monitoredKafka) throws Throwable {
        String topic = monitoredKafka.topic();
        String action = monitoredKafka.action();

        Timer.Sample sample = metricHelper.startSample();

        try {
            Object result = joinPoint.proceed();

            metricHelper.success(topic, action).increment();

            return result;

        } catch (Exception e) {
            log.warn("[Metric][Kafka] topic={}, action={} 실패 — 예외: {}", topic, action, e.getClass().getSimpleName());

            metricHelper.fail(topic, action, e.getClass().getSimpleName()).increment();

            throw e;

        } finally {
            sample.stop(metricHelper.timer(topic, action));
        }
    }

    /**
     * {@code AbstractEventPublisher.publish()} 자동 인터셉트 (Producer).
     * 어노테이션 없이 모든 이벤트 발행을 자동으로 수집한다.
     */
    @Around("execution(* com.ft.common.kafka.AbstractEventPublisher.publish(..))")
    public Object measurePublish(ProceedingJoinPoint joinPoint) throws Throwable {
        AbstractEventPublisher<?> publisher = (AbstractEventPublisher<?>) joinPoint.getTarget();
        String topic = publisher.topic();
        String action = "publish";

        Timer.Sample sample = metricHelper.startSample();

        try {
            Object result = joinPoint.proceed();

            metricHelper.success(topic, action).increment();

            return result;

        } catch (Exception e) {
            log.warn("[Metric][Kafka] topic={}, action=publish 실패 — 예외: {}", topic, e.getClass().getSimpleName());

            metricHelper.fail(topic, action, e.getClass().getSimpleName()).increment();

            throw e;

        } finally {
            sample.stop(metricHelper.timer(topic, action));
        }
    }
}
