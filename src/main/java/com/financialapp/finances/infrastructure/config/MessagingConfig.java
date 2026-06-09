package com.financialapp.finances.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class MessagingConfig {

    @Bean
    public NewTopic transactionCreatedTopic() {
        return TopicBuilder.name("finances.transaction.created").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic paymentRecordedTopic() {
        return TopicBuilder.name("banks.payment.recorded").partitions(1).replicas(1).build();
    }
}
