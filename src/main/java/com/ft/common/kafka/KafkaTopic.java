package com.ft.common.kafka;

public final class KafkaTopic {

    public static final String TRANSACTION_CREATED = "transaction.created";
    public static final String TRANSACTION_DELETED = "transaction.deleted";
    public static final String BUDGET_ALERT = "budget.alert";
    public static final String USER_REGISTERED = "user.registered";
    public static final String BATCH_REPORT = "batch.report";

    private KafkaTopic() {}
}
