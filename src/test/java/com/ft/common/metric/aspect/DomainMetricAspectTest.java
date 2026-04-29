package com.ft.common.metric.aspect;

import com.ft.common.metric.annotation.Monitored;
import com.ft.common.metric.helper.DomainMetricHelper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringJUnitConfig(classes = DomainMetricAspectTest.TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DomainMetricAspectTest {

    @Autowired
    private TargetService targetService;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    @DisplayName("성공메서드호출_ft_domain_requests_total_result=success_카운터1증가")
    void success_methodCall_incrementsSuccessCounter() {
        // when
        targetService.successMethod();

        // then
        double count = meterRegistry.get("ft_domain_requests_total")
                .tag("domain", "test")
                .tag("layer", "service")
                .tag("api", "success_method")
                .tag("result", "success")
                .counter().count();

        assertThat(count).isEqualTo(1.0);
    }

    @Test
    @DisplayName("실패메서드호출_ft_domain_requests_total_result=fail_카운터1증가후_예외재전파")
    void fail_methodCall_incrementsFailCounterAndRethrows() {
        // when & then
        assertThatThrownBy(() -> targetService.failMethod())
                .isInstanceOf(RuntimeException.class);

        double count = meterRegistry.get("ft_domain_requests_total")
                .tag("domain", "test")
                .tag("layer", "service")
                .tag("api", "fail_method")
                .tag("result", "fail")
                .counter().count();

        assertThat(count).isEqualTo(1.0);
    }

    @Test
    @DisplayName("성공메서드호출_ft_domain_request_duration_seconds_타이머_count1")
    void success_methodCall_recordsDuration() {
        // when
        targetService.successMethod();

        // then
        long timerCount = meterRegistry.get("ft_domain_request_duration_seconds")
                .tag("domain", "test")
                .tag("layer", "service")
                .tag("api", "success_method")
                .timer().count();

        assertThat(timerCount).isEqualTo(1L);
    }

    @Test
    @DisplayName("실패메서드호출_ft_domain_request_duration_seconds_타이머_finally에서_기록")
    void fail_methodCall_stillRecordsDurationInFinally() {
        // when
        assertThatThrownBy(() -> targetService.failMethod()).isInstanceOf(RuntimeException.class);

        // then
        long timerCount = meterRegistry.get("ft_domain_request_duration_seconds")
                .tag("domain", "test")
                .tag("layer", "service")
                .tag("api", "fail_method")
                .timer().count();

        assertThat(timerCount).isEqualTo(1L);
    }

    @Test
    @DisplayName("성공5회_실패2회_각각독립집계")
    void mixed_calls_trackedIndependently() {
        // when
        for (int i = 0; i < 5; i++) targetService.successMethod();
        for (int i = 0; i < 2; i++) {
            try { targetService.failMethod(); } catch (RuntimeException ignored) {}
        }

        // then
        double successCount = meterRegistry.get("ft_domain_requests_total")
                .tag("api", "success_method").tag("result", "success").counter().count();
        double failCount = meterRegistry.get("ft_domain_requests_total")
                .tag("api", "fail_method").tag("result", "fail").counter().count();

        assertThat(successCount).isEqualTo(5.0);
        assertThat(failCount).isEqualTo(2.0);
    }

    // ---- Test doubles ----

    static class TargetService {

        @Monitored(domain = "test", layer = "service", api = "success_method")
        public String successMethod() {
            return "ok";
        }

        @Monitored(domain = "test", layer = "service", api = "fail_method")
        public void failMethod() {
            throw new RuntimeException("intentional failure");
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
        public DomainMetricHelper domainMetricHelper(MeterRegistry meterRegistry) {
            return new DomainMetricHelper(meterRegistry);
        }

        @Bean
        public DomainMetricAspect domainMetricAspect(MeterRegistry meterRegistry,
                                                      DomainMetricHelper domainMetricHelper) {
            return new DomainMetricAspect(meterRegistry, domainMetricHelper);
        }

        @Bean
        public TargetService targetService() {
            return new TargetService();
        }
    }
}
