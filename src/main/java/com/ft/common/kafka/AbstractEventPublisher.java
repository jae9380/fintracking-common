package com.ft.common.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

@Slf4j
public abstract class AbstractEventPublisher<T> {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    protected AbstractEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    protected abstract String topic();

    public void publish(T event) {
        kafkaTemplate.send(topic(), event);
        log.info("Published event to topic [{}]: {}", topic(), event);
    }
}
