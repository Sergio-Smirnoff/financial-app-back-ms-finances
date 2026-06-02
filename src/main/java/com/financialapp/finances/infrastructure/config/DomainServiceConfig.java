package com.financialapp.finances.infrastructure.config;

import com.financialapp.finances.domain.service.TransactionClassifier;
import com.financialapp.finances.domain.service.TransactionCurrencyValidator;
import com.financialapp.finances.domain.service.TransactionPosting;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exposes framework-free domain services as Spring beans so the application layer can depend on
 * them via constructor injection without the domain itself importing Spring.
 */
@Configuration
public class DomainServiceConfig {

    @Bean
    public TransactionPosting transactionPosting() {
        return new TransactionPosting();
    }

    @Bean
    public TransactionClassifier transactionClassifier() {
        return new TransactionClassifier();
    }

    @Bean
    public TransactionCurrencyValidator transactionCurrencyValidator() {
        return new TransactionCurrencyValidator();
    }
}
