package com.financialapp.finances;

import com.financialapp.finances.infrastructure.config.CurrenciesProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ConfigurationBindingTest.Config.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "INTERNAL_AUTH_TOKEN=test-token")
@ActiveProfiles("test")
class ConfigurationBindingTest {

    @Autowired CurrenciesProperties properties;

    @Test
    void bindsTheSupportedCurrencyWhitelistFromYaml() {
        assertThat(properties.supported()).containsExactlyInAnyOrder("ARS", "USD");
    }

    @EnableConfigurationProperties(CurrenciesProperties.class)
    static class Config {}
}
