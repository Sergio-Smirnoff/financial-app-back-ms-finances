package com.financialapp.finances;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.financialapp.finances.config.AlertProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AlertProperties.class)
public class FinancesApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancesApplication.class, args);
    }
}
