package com.financialapp.finances.support;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/**
 * Base for integration tests that need the downstream ms-banks accounts HTTP boundary stubbed.
 * A dynamic-port WireMock server with stub files under classpath {@code wiremock/}; the Feign
 * client base URL is redirected at it via {@link DynamicPropertySource}.
 */
public abstract class WireMockIntegrationTest {

    @RegisterExtension
    protected static final WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(options().dynamicPort().usingFilesUnderClasspath("wiremock"))
            .build();

    @DynamicPropertySource
    static void downstreamUrls(DynamicPropertyRegistry registry) {
        registry.add("banks.service.url", wireMock::baseUrl);
    }
}
