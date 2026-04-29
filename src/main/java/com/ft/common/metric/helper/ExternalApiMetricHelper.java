package com.ft.common.metric.helper;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

/**
 * 외부 API 호출 메트릭 헬퍼 (수동 사용).
 * FCM, SMTP, Slack 등 외부 시스템 호출 지점에서 직접 주입하여 사용한다.
 *
 * <pre>
 * 메트릭:
 *   ft_external_api_requests_total{system, operation, result="success"|"fail", error_type?}
 *   ft_external_api_duration_seconds{system, operation}
 * </pre>
 *
 * 사용 예:
 * <pre>
 * Timer.Sample sample = metricHelper.startSample();
 * try {
 *     // 외부 API 호출
 *     metricHelper.success("smtp", "send_email").increment();
 * } catch (Exception e) {
 *     metricHelper.fail("smtp", "send_email", e.getClass().getSimpleName()).increment();
 *     throw e;
 * } finally {
 *     sample.stop(metricHelper.timer("smtp", "send_email"));
 * }
 * </pre>
 */
@RequiredArgsConstructor
public class ExternalApiMetricHelper {

    private final MeterRegistry meterRegistry;

    public Counter success(String system, String operation) {
        return Counter.builder("ft_external_api_requests_total")
                .tag("system", system)
                .tag("operation", operation)
                .tag("result", "success")
                .register(meterRegistry);
    }

    public Counter fail(String system, String operation, String errorType) {
        return Counter.builder("ft_external_api_requests_total")
                .tag("system", system)
                .tag("operation", operation)
                .tag("result", "fail")
                .tag("error_type", errorType)
                .register(meterRegistry);
    }

    public Timer timer(String system, String operation) {
        return Timer.builder("ft_external_api_duration_seconds")
                .tag("system", system)
                .tag("operation", operation)
                .publishPercentileHistogram(true)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    public Timer.Sample startSample() {
        return Timer.start(meterRegistry);
    }
}
