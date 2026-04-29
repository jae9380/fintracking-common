package com.ft.common.metric.helper;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaMetricHelperTest {

    private MeterRegistry meterRegistry;
    private KafkaMetricHelper sut;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        sut = new KafkaMetricHelper(meterRegistry);
    }

    @Test
    @DisplayName("success_정상호출_ft_kafka_events_total에_result=success태그로_카운터등록")
    void success_normalCall_registersCounterWithSuccessTag() {
        // given
        String topic = "transaction.created";
        String action = "consume";

        // when
        sut.success(topic, action).increment();

        // then
        Counter counter = meterRegistry.get("ft_kafka_events_total")
                .tag("topic", topic)
                .tag("action", action)
                .tag("result", "success")
                .counter();

        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("fail_정상호출_ft_kafka_events_total에_result=fail과_error_type태그로_카운터등록")
    void fail_normalCall_registersCounterWithFailAndErrorTypeTag() {
        // given
        String topic = "budget.alert";
        String action = "consume";
        String errorType = "RuntimeException";

        // when
        sut.fail(topic, action, errorType).increment();

        // then
        Counter counter = meterRegistry.get("ft_kafka_events_total")
                .tag("topic", topic)
                .tag("action", action)
                .tag("result", "fail")
                .tag("error_type", errorType)
                .counter();

        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("publish_action으로_success_카운터_집계")
    void success_publishAction_registersCounter() {
        // given
        String topic = "transaction.created";
        String action = "publish";

        // when
        sut.success(topic, action).increment();
        sut.success(topic, action).increment();

        // then
        double count = meterRegistry.get("ft_kafka_events_total")
                .tag("action", "publish")
                .counter().count();

        assertThat(count).isEqualTo(2.0);
    }

    @Test
    @DisplayName("timer_정상호출_ft_kafka_event_duration_seconds에_타이머등록")
    void timer_normalCall_registersTimerWithCorrectTags() {
        // given
        String topic = "transaction.created";
        String action = "consume";

        // when
        Timer timer = sut.timer(topic, action);

        // then
        assertThat(timer).isNotNull();
        Timer registered = meterRegistry.get("ft_kafka_event_duration_seconds")
                .tag("topic", topic)
                .tag("action", action)
                .timer();
        assertThat(registered).isNotNull();
    }

    @Test
    @DisplayName("startSample_반환값으로_timer_stop_가능")
    void startSample_canStopWithTimer() {
        // given
        Timer.Sample sample = sut.startSample();
        Timer timer = sut.timer("transaction.created", "publish");

        // when
        sample.stop(timer);

        // then
        assertThat(timer.count()).isEqualTo(1L);
    }
}
