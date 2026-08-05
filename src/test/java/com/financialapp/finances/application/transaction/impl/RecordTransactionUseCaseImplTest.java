package com.financialapp.finances.application.transaction.impl;
import com.financialapp.commons.core.domain.model.Cbu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialapp.commons.messaging.domain.model.OutboxRecord;
import com.financialapp.finances.domain.common.model.*;
import com.financialapp.finances.domain.event.DomainEvent;
import com.financialapp.finances.domain.event.TransactionCreated;
import com.financialapp.finances.domain.exception.transaction.AccountCurrencyMismatchException;
import com.financialapp.finances.domain.exception.transaction.UnownedTransactionException;
import com.financialapp.finances.domain.gateway.AccountOwnershipGateway;
import com.financialapp.finances.domain.gateway.DomainEventPublisher;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.service.TransactionCurrencyValidator;
import com.financialapp.finances.domain.service.TransactionPosting;
import com.financialapp.finances.domain.usecase.transaction.command.RecordTransactionCommand;
import com.financialapp.finances.infrastructure.messaging.mapper.TransactionCreatedMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.Currency;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RecordTransactionUseCaseImplTest {

    private static final Currency ARS = Currency.getInstance("ARS");
    private static final Currency USD = Currency.getInstance("USD");
    private final TransactionRepository repo = mock(TransactionRepository.class);
    private final AccountOwnershipGateway ownership = mock(AccountOwnershipGateway.class);
    private final DomainEventPublisher publisher = mock(DomainEventPublisher.class);
    private final com.financialapp.finances.domain.gateway.FxRateGateway fxRateGateway = mock(com.financialapp.finances.domain.gateway.FxRateGateway.class);
    private final RecordTransactionUseCaseImpl useCase = new RecordTransactionUseCaseImpl(
            repo, ownership, new TransactionPosting(), new TransactionCurrencyValidator(), publisher, fxRateGateway);

    private final Cbu mine = new Cbu("0001112223334445556667");
    private final Cbu other = new Cbu("9998887776665554443332");

    private RecordTransactionCommand cmd(Cbu from, Cbu to) {
        return new RecordTransactionCommand(new UserId(42L), from, to,
                new Money(new BigDecimal("100.00"), ARS), new CategoryId(5L), "x",
                LocalDate.of(2026, 6, 1));
    }

    private void echoSaveWithId(long id) {
        when(repo.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            return Transaction.reconstitute(new TransactionId(id), t.userId(), t.fromCbu(), t.toCbu(),
                    t.money(), t.categoryId(), t.description(), t.date());
        });
    }

    @Test
    void expenseEmitsOneBalanceEvent() {
        when(ownership.ownedAccountsWithCurrency(new UserId(42L)))
                .thenReturn(Set.of(new OwnedAccount(mine, ARS)));
        echoSaveWithId(77L);

        useCase.execute(cmd(mine, other));   // from owned, to external => expense

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DomainEvent>> cap = ArgumentCaptor.forClass(List.class);
        verify(publisher).publishAll(cap.capture());
        assertThat(cap.getValue()).hasSize(1);
        TransactionCreated e = (TransactionCreated) cap.getValue().get(0);
        assertThat(e.accountCbu()).isEqualTo(mine);
        assertThat(e.signedAmount()).isEqualByComparingTo("-100.00");
    }

    @Test
    void transferBetweenOwnedAccountsEmitsTwoBalanceEvents() {
        when(ownership.ownedAccountsWithCurrency(new UserId(42L)))
                .thenReturn(Set.of(new OwnedAccount(mine, ARS), new OwnedAccount(other, ARS)));
        echoSaveWithId(77L);

        useCase.execute(cmd(mine, other));   // both owned => transfer

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DomainEvent>> cap = ArgumentCaptor.forClass(List.class);
        verify(publisher).publishAll(cap.capture());
        List<DomainEvent> events = cap.getValue();
        assertThat(events).hasSize(2);

        TransactionCreated debit = events.stream()
                .map(e -> (TransactionCreated) e)
                .filter(e -> e.signedAmount().signum() < 0)
                .findFirst()
                .orElseThrow();
        TransactionCreated credit = events.stream()
                .map(e -> (TransactionCreated) e)
                .filter(e -> e.signedAmount().signum() > 0)
                .findFirst()
                .orElseThrow();

        assertThat(debit.accountCbu()).isEqualTo(mine);
        assertThat(debit.signedAmount()).isEqualByComparingTo("-100.00");
        assertThat(credit.accountCbu()).isEqualTo(other);
        assertThat(credit.signedAmount()).isEqualByComparingTo("100.00");

        TransactionCreatedMapper mapper = new TransactionCreatedMapper(new ObjectMapper());
        List<OutboxRecord> debitRecords = mapper.toOutboxRecords(debit);
        List<OutboxRecord> creditRecords = mapper.toOutboxRecords(credit);

        assertThat(debitRecords).hasSize(1);
        assertThat(creditRecords).hasSize(1);

        OutboxRecord debitRecord = debitRecords.get(0);
        OutboxRecord creditRecord = creditRecords.get(0);

        assertThat(debitRecord.eventId()).isNotEqualTo(creditRecord.eventId());

        assertThat(debitRecord.dataJson()).contains("\"accountCbu\":\"" + mine.cbuNumber() + "\"");
        assertThat(debitRecord.dataJson()).contains("\"amount\":-100.00");
        assertThat(creditRecord.dataJson()).contains("\"accountCbu\":\"" + other.cbuNumber() + "\"");
        assertThat(creditRecord.dataJson()).contains("\"amount\":100.00");
    }

    @Test
    void noneOwnedThrowsAndNeitherSavesNorPublishes() {
        when(ownership.ownedAccountsWithCurrency(new UserId(42L))).thenReturn(Set.of());

        assertThatThrownBy(() -> useCase.execute(cmd(mine, other)))
                .isInstanceOf(UnownedTransactionException.class);

        verify(repo, never()).save(any());
        verify(publisher, never()).publishAll(any());
    }

    @Test
    void ownedSideInDifferentCurrencyIsRejectedBeforePersisting() {
        // owned account is USD, transaction is ARS
        when(ownership.ownedAccountsWithCurrency(new UserId(42L)))
                .thenReturn(Set.of(new OwnedAccount(mine, USD)));

        assertThatThrownBy(() -> useCase.execute(cmd(mine, other)))
                .isInstanceOf(AccountCurrencyMismatchException.class);

        verify(repo, never()).save(any());
        verify(publisher, never()).publishAll(any());
    }

    @Test
    void externalCounterpartyCurrencyIsNotChecked() {
        // user owns the destination (ARS, matches); the source is external and not validated
        when(ownership.ownedAccountsWithCurrency(new UserId(42L)))
                .thenReturn(Set.of(new OwnedAccount(other, ARS)));
        echoSaveWithId(77L);

        useCase.execute(cmd(mine, other));   // to=other owned ARS, from=mine external

        verify(publisher).publishAll(any());
    }
}
