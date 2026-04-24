package com.ft.common.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionCreatedEvent(
        String eventId,
        Long userId,
        Long accountId,
        Long toAccountId,
        Long transactionId,
        BigDecimal amount,
        String type,
        Long categoryId,
        String categoryName,
        LocalDateTime transactedAt
) {}
