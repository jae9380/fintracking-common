package com.ft.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Auth
    AUTH_REFRESH_TOKEN_INVALID(401, "유효하지 않은 토큰입니다."),
    AUTH_REFRESH_TOKEN_EXPIRED(401, "만료된 토큰입니다."),
    AUTH_EMAIL_EXISTS(409, "이미 존재하는 이메일입니다."),
    AUTH_USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    AUTH_INVALID_PASSWORD(401, "비밀번호가 올바르지 않습니다."),
    AUTH_PASSWORD_REQUIRED(400, "비밀번호는 비어있을 수 없습니다."),
    AUTH_EMAIL_REQUIRED(400, "이메일은 비어있을 수 없습니다."),
    AUTH_EMAIL_INVALID_FORMAT(400, "올바른 이메일 형식이 아닙니다."),
    AUTH_REFRESH_TOKEN_REQUIRED(400, "토큰은 비어있을 수 없습니다."),
    AUTH_NEW_REFRESH_TOKEN_REQUIRED(400, "새 토큰은 비어있을 수 없습니다."),
    AUTH_INVALID_EXPIRATION(400, "만료 시간은 현재 시간 이후여야 합니다."),
    AUTH_INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),
    AUTH_OAUTH2_FAILED(400, "Kakao OAuth2 인증에 실패했습니다."),
    AUTH_OAUTH2_EMAIL_MISSING(400, "Kakao 계정에 이메일 정보가 없습니다. 카카오 계정 설정에서 이메일 제공에 동의해 주세요."),

    // Account
    ACCOUNT_NOT_FOUND(404, "계좌를 찾을 수 없습니다."),
    ACCOUNT_NO_ACCESS(403, "해당 계좌에 대한 접근 권한이 없습니다."),
    ACCOUNT_INVALID_TYPE(400, "잘못된 계좌 유형입니다."),
    ACCOUNT_INSUFFICIENT_BALANCE(400, "잔액이 부족합니다."),
    ACCOUNT_INVALID_AMOUNT(400, "금액은 0보다 커야 합니다."),
    ACCOUNT_INVALID_NAME(400, "계좌명은 비어있을 수 없습니다."),
    ACCOUNT_INVALID_NUMBER(400, "계좌번호는 비어있을 수 없습니다."),
    ACCOUNT_OWNER_MISMATCH(403, "계좌 소유자가 아닙니다."),

    // Transaction
    TRANSACTION_NOT_FOUND(404, "거래 내역을 찾을 수 없습니다."),
    TRANSACTION_NO_ACCESS(403, "해당 거래에 대한 접근 권한이 없습니다."),
    TRANSACTION_INVALID_TYPE(400, "잘못된 거래 유형입니다."),
    TRANSACTION_INVALID_CATEGORY_NAME(400, "카테고리명은 비어있을 수 없습니다."),
    TRANSACTION_INVALID_DATE(400,"거래일은 비어있을 수 없습니다."),
    TRANSACTION_CATEGORY_REQUIRED(400, "수입/지출 거래는 카테고리가 필요합니다."),
    TRANSACTION_INVALID_AMOUNT(400, "거래 금액은 0보다 커야 합니다."),

    // Budget
    BUDGET_NOT_FOUND(404, "예산 정보를 찾을 수 없습니다."),
    BUDGET_DUPLICATE(409, "이미 존재하는 예산입니다."),
    BUDGET_NO_ACCESS(403, "해당 예산에 대한 접근 권한이 없습니다."),
    BUDGET_INVALID_AMOUNT(400, "예산 금액은 0보다 커야 합니다."),
    BUDGET_EXPENSE_INVALID_AMOUNT(400, "지출 금액은 0 이상이어야 합니다."),

    // Notification
    NOTIFICATION_NOT_FOUND(404, "알림을 찾을 수 없습니다."),
    NOTIFICATION_NO_ACCESS(403, "해당 알림에 대한 접근 권한이 없습니다."),
    NOTIFICATION_SEND_FAILED(400, "메일 발송을 실패했습니다."),


    // Batch
    BATCH_MAX_RETRY_EXCEEDED(500, "배치 작업 최대 재시도 횟수를 초과했습니다."),
    BATCH_JOB_EXECUTION_FAILED(500, "배치 Job 실행에 실패했습니다.");

    private final int status;
    private final String message;
}
