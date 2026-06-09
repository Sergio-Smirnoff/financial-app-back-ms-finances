package com.financialapp.finances;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

@EnabledIfEnvironmentVariable(named = "INTEGRATION_INFRA", matches = "true")
@SpringBootTest(properties = "INTERNAL_AUTH_TOKEN=test-token")
class SchemaBootIT {

    @Test
    void contextLoads() {
    }
}
