package com.ft.common.metric.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 도메인 서비스 레이어 메트릭 수집 어노테이션.
 * AOP로 성공/실패 카운터와 실행 시간을 자동으로 기록한다.
 *
 * <pre>
 * 생성 메트릭:
 *   ft_domain_requests_total{domain, layer, api, result}
 *   ft_domain_request_duration_seconds{domain, layer, api}
 * </pre>
 *
 * 사용 예:
 * <pre>
 * {@code @Monitored(domain = "transaction", layer = "service", api = "create")}
 * public TransactionResult create(...) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Monitored {

    /** 도메인 이름 (예: "transaction", "account", "budget") */
    String domain();

    /** 레이어 이름 (예: "service", "infrastructure") */
    String layer();

    /** API/오퍼레이션 이름 (예: "create", "find_all", "update_balance") */
    String api();
}
