package com.financialapp.finances.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Base for ms-finances integration tests. Replicates the WireMock boundary strategy used in TP1:
 * the only thing stubbed is the downstream HTTP boundary (ms-banks accounts). Everything else runs
 * for real — the full Spring context boots, controllers are exercised through {@link MockMvc} with
 * real payloads, real use cases run against an in-memory H2 schema, and the
 * {@code BankAccountOwnershipGateway} reaches ms-banks over a real socket served by WireMock.
 *
 * <p>No {@code @MockBean} of use cases or gateways: integration tests must use WireMock, never
 * mocks, for the downstream boundary.
 *
 * <p>Stub files live under classpath {@code wiremock/} ({@code mappings/} + {@code __files/}) for
 * happy paths; per-test {@link #wireMock} {@code stubFor(...)} overrides cover error scenarios.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = "INTERNAL_AUTH_TOKEN=test-token")
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, bootstrapServersProperty = "spring.kafka.bootstrap-servers")
// All subclasses inherit one @DynamicPropertySource, so Spring's context-cache key is identical for
// each — the cached context bakes whichever dynamic port the first IT class started, leaving later
// classes pointing at a stopped server. Evicting the context after each class makes every class
// rebind to its own freshly started WireMock, so we keep dynamicPort() per the standard recipe
// (§6.1) instead of resorting to a fixed port. (ms-banks avoids this only because a single IT class
// extends its base.)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class WireMockIntegrationTest {

    @RegisterExtension
    protected static final WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(options().dynamicPort().usingFilesUnderClasspath("wiremock"))
            .build();

    @DynamicPropertySource
    static void downstreamUrls(DynamicPropertyRegistry registry) {
        registry.add("banks.service.url", wireMock::baseUrl);
        // Defeat the 60s ownership cache so each test fully controls what ms-banks "returns".
        registry.add("finances.ownership.cache-ttl-ms", () -> "0");
    }

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper json;
}
