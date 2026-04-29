package com.ft.common.metric.aspect;

import com.ft.common.kafka.AbstractEventPublisher;
import com.ft.common.metric.annotation.MonitoredKafka;
import com.ft.common.metric.helper.KafkaMetricHelper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@SpringJUnitConfig(classes = KafkaMetricAspectTest.TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class KafkaMetricAspectTest {

    @Autowired
    private FakeConsumer fakeConsumer;

    @Autowired
    private FakePublisher fakePublisher;

    @Autowired
    private MeterRegistry meterRegistry;

    // ---- Consumer 인터셉트 테스트 ----

    @Test
    @DisplayName("@MonitoredKafka_메서드성공_ft_kafka_events_total_result=success_카운터1증가")
    void consumer_success_incrementsSuccessCounter() {
        // when
        fakeConsumer.handle("event");

        // then
        double count = meterRegistry.get("ft_kafka_events_total")
                .tag("topic", "transaction.created")
                .tag("action", "consume")
                .tag("result", "success")
                .counter().count();

        assertThat(count).isEqualTo(1.0);
    }

    @Test
    @DisplayName("@MonitoredKafka_메서드실패_ft_kafka_events_total_result=fail_카운터1증가후_예외재전파")
    void consumer_fail_incrementsFailCounterAndRethrows() {
        // when & then
        assertThatThrownBy(() -> fakeConsumer.handleFail("event"))
                .isInstanceOf(RuntimeException.class);

        double count = meterRegistry.get("ft_kafka_events_total")
                .tag("topic", "budget.alert")
                .tag("action", "consume")
                .tag("result", "fail")
                .counter().count();

        assertThat(count).isEqualTo(1.0);
    }

    @Test
    @DisplayName("@MonitoredKafka_메서드성공_ft_kafka_event_duration_seconds_타이머기록")
    void consumer_success_recordsDuration() {
        // when
        fakeConsumer.handle("event");

        // then
        long timerCount = meterRegistry.get("ft_kafka_event_duration_seconds")
                .tag("topic", "transaction.created")
                .tag("action", "consume")
                .timer().count();

        assertThat(timerCount).isEqualTo(1L);
    }

    @Test
    @DisplayName("@MonitoredKafka_메서드실패_finally에서도_타이머기록")
    void consumer_fail_stillRecordsDurationInFinally() {
        // when
        try { fakeConsumer.handleFail("event"); } catch (RuntimeException ignored) {}

        // then
        long timerCount = meterRegistry.get("ft_kafka_event_duration_seconds")
                .tag("topic", "budget.alert")
                .tag("action", "consume")
                .timer().count();

        assertThat(timerCount).isEqualTo(1L);
    }

    // ---- Publisher 자동 인터셉트 테스트 ----

    @Test
    @DisplayName("AbstractEventPublisher_publish_자동인터셉트_result=success_카운터1증가")
    void publisher_publish_autoIntercepted_incrementsSuccessCounter() {
        // when
        fakePublisher.publish("test-event");

        // then
        double count = meterRegistry.get("ft_kafka_events_total")
                .tag("topic", "test.topic")
                .tag("action", "publish")
                .tag("result", "success")
                .counter().count();

        assertThat(count).isEqualTo(1.0);
    }

    @Test
    @DisplayName("AbstractEventPublisher_publish_자동인터셉트_타이머기록")
    void publisher_publish_autoIntercepted_recordsDuration() {
        // when
        fakePublisher.publish("test-event");

        // then
        long timerCount = meterRegistry.get("ft_kafka_event_duration_seconds")
                .tag("topic", "test.topic")
                .tag("action", "publish")
                .timer().count();

        assertThat(timerCount).isEqualTo(1L);
    }

    // ---- Test doubles ----

    static class FakeConsumer {

        @MonitoredKafka(topic = "transaction.created", action = "consume")
        public void handle(String event) {
            // 정상 처리
        }

        @MonitoredKafka(topic = "budget.alert", action = "consume")
        public void handleFail(String event) {
            throw new RuntimeException("consume failure");
        }
    }

    static class FakePublisher extends AbstractEventPublisher<String> {

        @SuppressWarnings("unchecked")
        FakePublisher() {
            super(mock(KafkaTemplate.class));
        }

        @Override
        public String topic() {
            return "test.topic";
        }

        @Override
        public void publish(String event) {
            // KafkaTemplate.send() 실제 호출 대신 stub — Aspect만 검증
        }
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {

        @Bean
        public MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }

        @Bean
        public KafkaMetricHelper kafkaMetricHelper(MeterRegistry meterRegistry) {
            return new KafkaMetricHelper(meterRegistry);
        }

        @Bean
        public KafkaMetricAspect kafkaMetricAspect(KafkaMetricHelper kafkaMetricHelper) {
            return new KafkaMetricAspect(kafkaMetricHelper);
        }

        @Bean
        public FakeConsumer fakeConsumer() {
            return new FakeConsumer();
        }

        @Bean
        public FakePublisher fakePublisher() {
            return new FakePublisher();
        }
    }
}
