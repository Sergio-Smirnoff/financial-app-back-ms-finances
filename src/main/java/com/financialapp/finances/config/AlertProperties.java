package com.financialapp.finances.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "alerts")
@Getter
@Setter
public class AlertProperties {
    private int daysBeforePayment = 3;
    private int daysBeforeLoan = 3;
    private int daysBeforeInstallment = 3;
}
