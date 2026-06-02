package com.financialapp.finances.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class InternalAuthFilterTest {

    private final FilterChain chain = mock(FilterChain.class);

    private InternalAuthFilter filter(String configuredToken) {
        InternalAuthFilter f = new InternalAuthFilter();
        ReflectionTestUtils.setField(f, "internalToken", configuredToken);
        return f;
    }

    private HttpServletRequest request(String path, String headerToken) {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn(path);
        when(req.getHeader("X-Internal-Token")).thenReturn(headerToken);
        return req;
    }

    @ParameterizedTest
    @ValueSource(strings = {"/actuator/health", "/swagger-ui/index.html", "/v3/api-docs"})
    void passesThrough_whenPathIsExempt(String path) throws Exception {
        // Given an exempt path / When filtered / Then it proceeds without a token check
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter(null).doFilterInternal(request(path, null), response, chain);
        verify(chain).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Test
    void passesThrough_whenTokenMatches() throws Exception {
        // Given a matching token / When filtered / Then it proceeds
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter("secret").doFilterInternal(request("/api/v1/finances/transactions", "secret"), response, chain);
        verify(chain).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Test
    void rejects_whenConfiguredTokenIsNull() throws Exception {
        assertRejected(filter(null), request("/api/v1/finances/transactions", "anything"));
    }

    @Test
    void rejects_whenConfiguredTokenIsEmpty() throws Exception {
        assertRejected(filter(""), request("/api/v1/finances/transactions", ""));
    }

    @Test
    void rejects_whenTokenDoesNotMatch() throws Exception {
        assertRejected(filter("secret"), request("/api/v1/finances/transactions", "wrong"));
    }

    private void assertRejected(InternalAuthFilter filter, HttpServletRequest req) throws IOException, jakarta.servlet.ServletException {
        // When filtered with a bad token / Then 401 and the chain is never invoked
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(req, response, chain);
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(response.getContentAsString()).contains("Unauthorized");
        verify(chain, never()).doFilter(any(), any());
    }
}
