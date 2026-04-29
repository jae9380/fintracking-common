package com.ft.common.metric.aspect;

import com.ft.common.metric.annotation.Monitored;
import com.ft.common.metric.helper.DomainMetricHelper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * {@code @Monitored} 어노테이션이 선언된 메서드의 실행을 인터셉트하여
 * 성공/실패 카운터와 실행 시간을 Micrometer로 기록하는 AOP Aspect.
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class DomainMetricAspect {

    private final MeterRegistry meterRegistry;
    private final DomainMetricHelper metricHelper;

    @Around("@annotation(monitored)")
    public Object measure(ProceedingJoinPoint joinPoint, Monitored monitored) throws Throwable {
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            Object result = joinPoint.proceed();

            metricHelper.success(monitored.domain(), monitored.layer(), monitored.api()).increment();

            return result;

        } catch (Exception e) {
            log.warn("[Metric] {}.{}.{} 실패 — 예외: {}",
                    monitored.domain(), monitored.layer(), monitored.api(),
                    e.getClass().getSimpleName());

            metricHelper.fail(monitored.domain(), monitored.layer(), monitored.api()).increment();

            throw e;

        } finally {
            sample.stop(metricHelper.timer(monitored.domain(), monitored.layer(), monitored.api()));
        }
    }
}
