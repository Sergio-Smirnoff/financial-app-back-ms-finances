package com.financialapp.finances;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// INTERNAL_AUTH_TOKEN has no default (required by the legacy FeignConfig/InternalAuthFilter); the
// real service gets it from .env. Supply a dummy here so the boot test is self-sufficient.
@SpringBootTest(properties = "INTERNAL_AUTH_TOKEN=test-token")
class SchemaBootIT {

    @Test
    void contextLoads() {
        // Boots the full context: Flyway migrates V1..V17, Hibernate validates entities against
        // the schema, Feign/Kafka beans wire. Fails fast on any entity/column mismatch.
    }
}
