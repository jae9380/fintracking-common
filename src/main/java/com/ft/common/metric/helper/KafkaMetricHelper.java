package com.ft.common.metric.helper;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

/**
 * Kafka Producer/Consumer 메트릭 헬퍼.
 * {@code @MonitoredKafka} Aspect와 {@code AbstractEventPublisher} Aspect에서 사용한다.
 *
 * <pre>
 * 메트릭:
 *   ft_kafka_events_total{topic, action="publish"|"consume", result="success"|"fail"}
 *   ft_kafka_event_duration_seconds{topic, action}
 * </pre>
 */
@RequiredArgsConstructor
public class KafkaMetricHelper {

    private final MeterRegistry meterRegistry;

    public Counter success(String topic, String action) {
        return Counter.builder("ft_kafka_events_total")
                .tag("topic", topic)
                .tag("action", action)
                .tag("result", "success")
                .register(meterRegistry);
    }

    public Counter fail(String topic, String action, String errorType) {
        return Counter.builder("ft_kafka_events_total")
                .tag("topic", topic)
                .tag("action", action)
                .tag("result", "fail")
                .tag("error_type", errorType)
                .register(meterRegistry);
    }

    public Timer timer(String topic, String action) {
        return Timer.builder("ft_kafka_event_duration_seconds")
                .tag("topic", topic)
                .tag("action", action)
                .publishPercentileHistogram(true)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    public Timer.Sample startSample() {
        return Timer.start(meterRegistry);
    }
}
