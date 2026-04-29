package com.ft.common.config;

import com.ft.common.exception.GlobalExceptionHandler;
import com.ft.common.metric.aspect.DomainMetricAspect;
import com.ft.common.metric.aspect.KafkaMetricAspect;
import com.ft.common.metric.helper.DomainMetricHelper;
import com.ft.common.metric.helper.BatchMetricHelper;
import com.ft.common.metric.helper.ExternalApiMetricHelper;
import com.ft.common.metric.helper.KafkaMetricHelper;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(afterName = {
        "org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration",
        "org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration"
})
public class CommonAutoConfiguration {

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    // ─── Metric Helpers ────────────────────────────────────────────────────────

    @Bean
    @ConditionalOnClass(name = "io.micrometer.core.instrument.MeterRegistry")
    @ConditionalOnBean(MeterRegistry.class)
    public DomainMetricHelper domainMetricHelper(MeterRegistry meterRegistry) {
        return new DomainMetricHelper(meterRegistry);
    }

    @Bean
    @ConditionalOnClass(name = "io.micrometer.core.instrument.MeterRegistry")
    @ConditionalOnBean(MeterRegistry.class)
    public KafkaMetricHelper kafkaMetricHelper(MeterRegistry meterRegistry) {
        return new KafkaMetricHelper(meterRegistry);
    }

    @Bean
    @ConditionalOnClass(name = "io.micrometer.core.instrument.MeterRegistry")
    @ConditionalOnBean(MeterRegistry.class)
    public ExternalApiMetricHelper externalApiMetricHelper(MeterRegistry meterRegistry) {
        return new ExternalApiMetricHelper(meterRegistry);
    }

    @Bean
    @ConditionalOnClass(name = "io.micrometer.core.instrument.MeterRegistry")
    @ConditionalOnBean(MeterRegistry.class)
    public BatchMetricHelper batchMetricHelper(MeterRegistry meterRegistry) {
        return new BatchMetricHelper(meterRegistry);
    }

    // ─── Metric Aspects ────────────────────────────────────────────────────────

    @Bean
    @ConditionalOnBean({MeterRegistry.class, DomainMetricHelper.class})
    public DomainMetricAspect domainMetricAspect(MeterRegistry meterRegistry, DomainMetricHelper domainMetricHelper) {
        return new DomainMetricAspect(meterRegistry, domainMetricHelper);
    }

    @Bean
    @ConditionalOnBean(KafkaMetricHelper.class)
    public KafkaMetricAspect kafkaMetricAspect(KafkaMetricHelper kafkaMetricHelper) {
        return new KafkaMetricAspect(kafkaMetricHelper);
    }
}
