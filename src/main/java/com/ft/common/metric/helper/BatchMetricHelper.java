package com.ft.common.metric.helper;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;

/**
 * Spring Batch Job 메트릭 헬퍼 (수동 사용).
 * Decorator 패턴으로 조립된 배치 실행기에서 직접 주입하여 사용한다.
 *
 * <pre>
 * 메트릭:
 *   ft_batch_job_total{job, result="success"|"fail"}
 *   ft_batch_job_duration_seconds{job, result}
 * </pre>
 */
@RequiredArgsConstructor
public class BatchMetricHelper {

    private final MeterRegistry meterRegistry;

    public void success(String jobName) {
        jobCounter(jobName, "success").increment();
    }

    public void fail(String jobName, String reason) {
        Counter.builder("ft_batch_job_total")
                .tag("job", jobName)
                .tag("result", "fail")
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }

    public void recordDuration(String jobName, String result, long executionTimeMs) {
        Timer.builder("ft_batch_job_duration_seconds")
                .tag("job", jobName)
                .tag("result", result)
                .publishPercentileHistogram(true)
                .register(meterRegistry)
                .record(executionTimeMs, TimeUnit.MILLISECONDS);
    }

    private Counter jobCounter(String jobName, String result) {
        return Counter.builder("ft_batch_job_total")
                .tag("job", jobName)
                .tag("result", result)
                .register(meterRegistry);
    }
}
