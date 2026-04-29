package com.ft.common.metric.helper;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

/**
 * 도메인 서비스 레이어 메트릭 헬퍼.
 * {@code @Monitored} AOP Aspect에서 내부적으로 사용한다.
 *
 * <pre>
 * 메트릭:
 *   ft_domain_requests_total{domain, layer, api, result="success"|"fail"}
 *   ft_domain_request_duration_seconds{domain, layer, api}
 * </pre>
 */
@RequiredArgsConstructor
public class DomainMetricHelper {

    private final MeterRegistry meterRegistry;

    public Counter success(String domain, String layer, String api) {
        return Counter.builder("ft_domain_requests_total")
                .tag("domain", domain)
                .tag("layer", layer)
                .tag("api", api)
                .tag("result", "success")
                .register(meterRegistry);
    }

    public Counter fail(String domain, String layer, String api) {
        return Counter.builder("ft_domain_requests_total")
                .tag("domain", domain)
                .tag("layer", layer)
                .tag("api", api)
                .tag("result", "fail")
                .register(meterRegistry);
    }

    public Timer timer(String domain, String layer, String api) {
        return Timer.builder("ft_domain_request_duration_seconds")
                .tag("domain", domain)
                .tag("layer", layer)
                .tag("api", api)
                .publishPercentileHistogram(true)
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }
}
