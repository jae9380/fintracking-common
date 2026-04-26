package com.ft.common.event;

/**
 * 월간 통계 배치 완료 이벤트.
 * batch-service가 발행하고 notification-service가 구독하여 사용자에게 리포트 알림을 발송한다.
 */
public record BatchReportEvent(
        String eventId,
        Long userId,
        String yearMonth,
        String title,
        String message
) {}
