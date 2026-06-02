package com.financialapp.finances.web.error;

import com.financialapp.finances.web.dto.response.ApiResponse;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DomainExceptionHandlerBranchesTest {

    private final DomainExceptionHandler handler = new DomainExceptionHandler();

    @Test void handleValidation_returns400WithFieldMessages() {
        // Given a MethodArgumentNotValidException with one field error
        BindingResult binding = mock(BindingResult.class);
        when(binding.getFieldErrors()).thenReturn(List.of(new FieldError("req", "name", "must not be blank")));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(binding);
        // When handled
        ResponseEntity<ApiResponse<Void>> resp = handler.handleValidation(ex);
        // Then 400 with the field messages
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody().getErrors()).containsExactly("must not be blank");
    }

    @Test void handleConstraintViolation_returns400() {
        // Given a constraint violation
        ResponseEntity<ApiResponse<Void>> resp =
                handler.handleConstraintViolation(new ConstraintViolationException("bad", Set.of()));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private FeignException feignWithStatus(int status) {
        Request request = Request.create(Request.HttpMethod.GET, "http://banks/api",
                Map.of(), new byte[0], StandardCharsets.UTF_8, new RequestTemplate());
        return FeignException.errorStatus("listAccounts",
                feign.Response.builder().status(status).request(request).build());
    }

    @Test void handleFeign_passesThroughDownstreamStatus_whenPositive() {
        // Given a downstream 404 / Then it is passed through
        ResponseEntity<ApiResponse<Void>> resp = handler.handleFeign(feignWithStatus(404));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    /** A transport-level Feign error carries no HTTP status (status() <= 0). */
    private static final class TransportFeignException extends FeignException {
        private TransportFeignException(Request request) {
            super(-1, "connection refused", request);
        }
    }

    @Test void handleFeign_returnsBadGateway_whenStatusNonPositive() {
        // Given a transport-level Feign error with no HTTP status (status <= 0)
        ResponseEntity<ApiResponse<Void>> resp = handler.handleFeign(new TransportFeignException(feignRequest()));
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    private Request feignRequest() {
        return Request.create(Request.HttpMethod.GET, "http://banks/api",
                Map.of(), new byte[0], StandardCharsets.UTF_8, new RequestTemplate());
    }
}
