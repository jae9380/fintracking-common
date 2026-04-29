package com.ft.common.metric.helper;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BatchMetricHelperTest {

    private MeterRegistry meterRegistry;
    private BatchMetricHelper sut;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        sut = new BatchMetricHelper(meterRegistry);
    }

    @Test
    @DisplayName("success_정상호출_ft_batch_job_total에_result=success태그로_카운터1증가")
    void success_normalCall_incrementsSuccessCounter() {
        // given
        String jobName = "monthlyStatisticsJob";

        // when
        sut.success(jobName);

        // then
        Counter counter = meterRegistry.get("ft_batch_job_total")
                .tag("job", jobName)
                .tag("result", "success")
                .counter();

        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("fail_정상호출_ft_batch_job_total에_result=fail과_reason태그로_카운터1증가")
    void fail_normalCall_incrementsFailCounterWithReason() {
        // given
        String jobName = "monthlyStatisticsJob";
        String reason = "RuntimeException";

        // when
        sut.fail(jobName, reason);

        // then
        Counter counter = meterRegistry.get("ft_batch_job_total")
                .tag("job", jobName)
                .tag("result", "fail")
                .tag("reason", reason)
                .counter();

        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("success_여러번호출_누적카운트")
    void success_multipleCalls_accumulatesCount() {
        // given
        String jobName = "monthlyStatisticsJob";

        // when
        sut.success(jobName);
        sut.success(jobName);
        sut.success(jobName);

        // then
        double count = meterRegistry.get("ft_batch_job_total")
                .tag("result", "success")
                .counter().count();

        assertThat(count).isEqualTo(3.0);
    }

    @Test
    @DisplayName("recordDuration_정상호출_ft_batch_job_duration_seconds에_타이머기록")
    void recordDuration_normalCall_recordsTimerWithJobAndResult() {
        // given
        String jobName = "monthlyStatisticsJob";
        String result = "success";
        long executionTimeMs = 1500L;

        // when
        sut.recordDuration(jobName, result, executionTimeMs);

        // then
        Timer timer = meterRegistry.get("ft_batch_job_duration_seconds")
                .tag("job", jobName)
                .tag("result", result)
                .timer();

        assertThat(timer.count()).isEqualTo(1L);
        assertThat(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS))
                .isGreaterThanOrEqualTo(executionTimeMs);
    }

    @Test
    @DisplayName("recordDuration_fail결과_별도타이머로_집계")
    void recordDuration_failResult_trackedSeparately() {
        // given
        String jobName = "monthlyStatisticsJob";

        // when
        sut.recordDuration(jobName, "success", 1000L);
        sut.recordDuration(jobName, "fail", 200L);

        // then
        Timer successTimer = meterRegistry.get("ft_batch_job_duration_seconds")
                .tag("result", "success").timer();
        Timer failTimer = meterRegistry.get("ft_batch_job_duration_seconds")
                .tag("result", "fail").timer();

        assertThat(successTimer.count()).isEqualTo(1L);
        assertThat(failTimer.count()).isEqualTo(1L);
    }
}
