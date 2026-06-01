package com.financialapp.finances;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.financialapp.finances.config.AlertProperties;
import com.financialapp.finances.infrastructure.config.CurrenciesProperties;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients(basePackages = {
        "com.financialapp.finances.client",
        "com.financialapp.finances.infrastructure.gateway"
})
@EnableConfigurationProperties({AlertProperties.class, CurrenciesProperties.class})
public class FinancesApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancesApplication.class, args);
    }
}
