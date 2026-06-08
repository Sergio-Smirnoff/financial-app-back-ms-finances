package com.financialapp.finances;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Requires live Postgres + Kafka infra — run manually against a running stack")
@SpringBootTest(properties = "INTERNAL_AUTH_TOKEN=test-token")
class SchemaBootIT {

    @Test
    void contextLoads() {
    }
}
