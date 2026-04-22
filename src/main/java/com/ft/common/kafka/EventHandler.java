package com.ft.common.kafka;

public interface EventHandler<T> {

    void handle(T event);
}
