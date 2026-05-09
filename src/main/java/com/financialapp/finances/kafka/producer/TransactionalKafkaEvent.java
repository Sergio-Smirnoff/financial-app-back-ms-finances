package com.financialapp.finances.kafka.producer;

public record TransactionalKafkaEvent(String topic, String key, Object payload) {}
