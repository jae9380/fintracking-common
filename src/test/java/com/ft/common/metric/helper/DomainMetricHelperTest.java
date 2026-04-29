package com.ft.common.metric.helper;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainMetricHelperTest {

    private MeterRegistry meterRegistry;
    private DomainMetricHelper sut;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        sut = new DomainMetricHelper(meterRegistry);
    }

    @Test
    @DisplayName("success_정상호출_ft_domain_requests_total에_result=success태그로_카운터등록")
    void success_normalCall_registersCounterWithSuccessTag() {
        // given
        String domain = "transaction";
        String layer = "service";
        String api = "create";

        // when
        Counter counter = sut.success(domain, layer, api);
        counter.increment();

        // then
        Counter registered = meterRegistry.get("ft_domain_requests_total")
                .tag("domain", domain)
                .tag("layer", layer)
                .tag("api", api)
                .tag("result", "success")
                .counter();

        assertThat(registered.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("fail_정상호출_ft_domain_requests_total에_result=fail태그로_카운터등록")
    void fail_normalCall_registersCounterWithFailTag() {
        // given
        String domain = "account";
        String layer = "service";
        String api = "find_by_id";

        // when
        Counter counter = sut.fail(domain, layer, api);
        counter.increment();

        // then
        Counter registered = meterRegistry.get("ft_domain_requests_total")
                .tag("domain", domain)
                .tag("layer", layer)
                .tag("api", api)
                .tag("result", "fail")
                .counter();

        assertThat(registered.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("success_와_fail_동일api_각각독립카운터로_집계")
    void success_and_fail_sameApi_trackedIndependently() {
        // given
        String domain = "budget";
        String layer = "service";
        String api = "delete";

        // when
        sut.success(domain, layer, api).increment();
        sut.success(domain, layer, api).increment();
        sut.fail(domain, layer, api).increment();

        // then
        double successCount = meterRegistry.get("ft_domain_requests_total")
                .tag("result", "success").counter().count();
        double failCount = meterRegistry.get("ft_domain_requests_total")
                .tag("result", "fail").counter().count();

        assertThat(successCount).isEqualTo(2.0);
        assertThat(failCount).isEqualTo(1.0);
    }

    @Test
    @DisplayName("timer_정상호출_ft_domain_request_duration_seconds에_타이머등록")
    void timer_normalCall_registersTimerWithCorrectTags() {
        // given
        String domain = "auth";
        String layer = "service";
        String api = "login";

        // when
        Timer timer = sut.timer(domain, layer, api);

        // then
        assertThat(timer).isNotNull();
        Timer registered = meterRegistry.get("ft_domain_request_duration_seconds")
                .tag("domain", domain)
                .tag("layer", layer)
                .tag("api", api)
                .timer();
        assertThat(registered).isNotNull();
    }

    @Test
    @DisplayName("timer_record후_count증가")
    void timer_afterRecord_countIncreases() {
        // given
        Timer timer = sut.timer("notification", "service", "send");

        // when
        timer.record(() -> {});

        // then
        assertThat(timer.count()).isEqualTo(1L);
    }
}
