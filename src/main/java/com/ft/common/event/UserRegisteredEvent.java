package com.ft.common.event;

/**
 * 회원가입 완료 이벤트 (이메일 가입 + OAuth2 신규 가입 모두 포함).
 * batch-service가 구독하여 자체 BatchUser 테이블에 userId를 적재한다.
 */
public record UserRegisteredEvent(
        String eventId,
        Long userId,
        String email
) {}
