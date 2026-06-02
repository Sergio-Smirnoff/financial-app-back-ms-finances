package com.financialapp.finances.infrastructure.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class FeignConfigTest {

    @Test void requestInterceptor_injectsInternalTokenHeader() {
        // Given a configured token
        FeignConfig config = new FeignConfig();
        ReflectionTestUtils.setField(config, "internalToken", "secret-token");
        RequestInterceptor interceptor = config.requestInterceptor();
        RequestTemplate template = new RequestTemplate();
        // When the interceptor runs
        interceptor.apply(template);
        // Then the internal-auth header is present
        assertThat(template.headers().get("X-Internal-Token")).containsExactly("secret-token");
    }
}
