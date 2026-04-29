package com.ft.common.metric.helper;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalApiMetricHelperTest {

    private MeterRegistry meterRegistry;
    private ExternalApiMetricHelper sut;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        sut = new ExternalApiMetricHelper(meterRegistry);
    }

    @Test
    @DisplayName("success_정상호출_ft_external_api_requests_total에_result=success태그로_카운터등록")
    void success_normalCall_registersCounterWithSuccessTag() {
        // given
        String system = "kakao";
        String operation = "get_access_token";

        // when
        sut.success(system, operation).increment();

        // then
        Counter counter = meterRegistry.get("ft_external_api_requests_total")
                .tag("system", system)
                .tag("operation", operation)
                .tag("result", "success")
                .counter();

        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("fail_정상호출_ft_external_api_requests_total에_result=fail과_error_type태그로_카운터등록")
    void fail_normalCall_registersCounterWithFailAndErrorType() {
        // given
        String system = "smtp";
        String operation = "send_email";
        String errorType = "MessagingException";

        // when
        sut.fail(system, operation, errorType).increment();

        // then
        Counter counter = meterRegistry.get("ft_external_api_requests_total")
                .tag("system", system)
                .tag("operation", operation)
                .tag("result", "fail")
                .tag("error_type", errorType)
                .counter();

        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("fcm시스템_success_카운터_집계")
    void success_fcmSystem_countsCorrectly() {
        // given & when
        sut.success("fcm", "send_push").increment();
        sut.success("fcm", "send_push").increment();
        sut.success("fcm", "send_push").increment();

        // then
        double count = meterRegistry.get("ft_external_api_requests_total")
                .tag("system", "fcm")
                .tag("operation", "send_push")
                .tag("result", "success")
                .counter().count();

        assertThat(count).isEqualTo(3.0);
    }

    @Test
    @DisplayName("timer_정상호출_ft_external_api_duration_seconds에_타이머등록")
    void timer_normalCall_registersTimerWithCorrectTags() {
        // given
        String system = "kakao";
        String operation = "get_user_info";

        // when
        Timer timer = sut.timer(system, operation);

        // then
        assertThat(timer).isNotNull();
        Timer registered = meterRegistry.get("ft_external_api_duration_seconds")
                .tag("system", system)
                .tag("operation", operation)
                .timer();
        assertThat(registered).isNotNull();
    }

    @Test
    @DisplayName("startSample_stop_호출_후_타이머count_증가")
    void startSample_stop_timerCountIncreases() {
        // given
        Timer.Sample sample = sut.startSample();
        Timer timer = sut.timer("smtp", "send_email");

        // when
        sample.stop(timer);

        // then
        assertThat(timer.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("success와_fail_서로_다른_system_operation_독립집계")
    void success_and_fail_differentSystems_trackedIndependently() {
        // given & when
        sut.success("kakao", "get_access_token").increment();
        sut.fail("smtp", "send_email", "MessagingException").increment();

        // then
        double kakaoCount = meterRegistry.get("ft_external_api_requests_total")
                .tag("system", "kakao").tag("result", "success").counter().count();
        double smtpCount = meterRegistry.get("ft_external_api_requests_total")
                .tag("system", "smtp").tag("result", "fail").counter().count();

        assertThat(kakaoCount).isEqualTo(1.0);
        assertThat(smtpCount).isEqualTo(1.0);
    }
}
