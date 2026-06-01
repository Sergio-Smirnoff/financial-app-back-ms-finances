package com.financialapp.finances.web.mapper;

import com.financialapp.finances.application.transaction.CategoryNameLookup;
import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.model.transaction.ClassifiedTransaction;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.model.transaction.TransactionKind;
import com.financialapp.finances.web.dto.response.AccountTransactionResponse;
import com.financialapp.finances.web.dto.response.TransactionResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TransactionWebMapperTest {

    private static final Currency ARS = Currency.getInstance("ARS");
    private final CategoryNameLookup names = mock(CategoryNameLookup.class);
    private final TransactionWebMapper mapper = new TransactionWebMapper(names);

    private final Cbu mine = new Cbu("0001112223334445556667");
    private final Cbu other = new Cbu("9998887776665554443332");

    private Transaction tx() {
        return Transaction.reconstitute(new TransactionId(77L), new UserId(42L), mine, other,
                new Money(new BigDecimal("100.00"), ARS), new CategoryId(5L), "Rent", LocalDate.of(2026, 6, 1));
    }

    @Test
    void userViewCarriesKindAndMagnitude() {
        TransactionResponse r = mapper.toUserResponse(new ClassifiedTransaction(tx(), TransactionKind.EXPENSE));
        assertThat(r.id()).isEqualTo(77L);
        assertThat(r.fromCbu()).isEqualTo("0001112223334445556667");
        assertThat(r.kind()).isEqualTo(TransactionKind.EXPENSE);
        assertThat(r.amount()).isEqualByComparingTo("100.00");
    }

    @Test
    void accountViewSignsForTheAccountAndResolvesNames() {
        when(names.resolve(5L)).thenReturn(new CategoryNameLookup.CategoryNames("Housing", "Rent"));
        AccountTransactionResponse r = mapper.toAccountResponse(tx(), mine);
        assertThat(r.transactionId()).isEqualTo(77L);
        assertThat(r.accountCbu()).isEqualTo("0001112223334445556667");
        assertThat(r.amount()).isEqualByComparingTo("-100.00"); // signedFor(mine) = debit
        assertThat(r.category()).isEqualTo("Housing");
        assertThat(r.subcategory()).isEqualTo("Rent");
    }
}
