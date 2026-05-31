package com.financialapp.finances.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic paymentDueTopic() {
        return TopicBuilder.name("payment.due")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic loanReminderTopic() {
        return TopicBuilder.name("loan.reminder")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic installmentReminderTopic() {
        return TopicBuilder.name("installment.reminder")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
