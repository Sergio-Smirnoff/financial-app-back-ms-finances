package com.financialapp.finances.web.error;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.exception.category.SubcategoryNotInCategoryException;
import com.financialapp.finances.domain.exception.transaction.SameAccountTransactionException;
import com.financialapp.finances.domain.exception.transaction.UnownedTransactionException;
import com.financialapp.finances.web.dto.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class DomainExceptionHandlerTest {

    private final DomainExceptionHandler handler = new DomainExceptionHandler();

    @Test
    void domainErrorMapsByCategory() {
        ResponseEntity<ApiResponse<Void>> notFound =
                handler.handleDomain(new SubcategoryNotInCategoryException(new CategoryId(1L), new CategoryId(2L)));
        assertThat(notFound.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<ApiResponse<Void>> unprocessable =
                handler.handleDomain(new UnownedTransactionException(new UserId(1L),
                        new Cbu("0001112223334445556667"), new Cbu("9998887776665554443332")));
        assertThat(unprocessable.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);

        ResponseEntity<ApiResponse<Void>> badRequest =
                handler.handleDomain(new SameAccountTransactionException(new Cbu("0001112223334445556667")));
        assertThat(badRequest.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void notFoundForIllegalArgumentAndGenericFor500() {
        assertThat(handler.handleIllegalArgument(new IllegalArgumentException("nope")).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(handler.handleGeneric(new RuntimeException("boom")).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
