package com.ft.common.event;

import java.math.BigDecimal;

public record BudgetAlertEvent(
        String eventId,
        Long userId,
        Long budgetId,
        Long categoryId,
        String alertType,
        BigDecimal spentAmount,
        BigDecimal limitAmount
) {}
