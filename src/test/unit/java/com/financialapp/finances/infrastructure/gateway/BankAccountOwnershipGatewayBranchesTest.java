package com.financialapp.finances.infrastructure.gateway;

import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.infrastructure.gateway.Impl.BankAccountOwnershipGateway;
import com.financialapp.finances.infrastructure.gateway.dto.BankAccountResponse;
import com.financialapp.finances.infrastructure.gateway.dto.GatewayApiResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BankAccountOwnershipGatewayBranchesTest {

    private final BanksAccountsFeignClient feign = mock(BanksAccountsFeignClient.class);

    @Test void returnsEmpty_whenResponseEnvelopeIsNull() {
        // Given the client returns a null envelope
        BankAccountOwnershipGateway gateway = new BankAccountOwnershipGateway(feign, 60_000L);
        when(feign.listAccounts(7L)).thenReturn(null);
        // When / Then no accounts, no NPE
        assertThat(gateway.ownedAccounts(new UserId(7L))).isEmpty();
    }

    @Test void refetches_whenCacheEntryExpired() {
        // Given a zero TTL so every lookup is stale
        BankAccountOwnershipGateway gateway = new BankAccountOwnershipGateway(feign, 0L);
        when(feign.listAccounts(42L)).thenReturn(new GatewayApiResponse<>(List.of(
                new BankAccountResponse("0001112223334445556667", "ARS"))));
        // When called twice
        gateway.ownedAccounts(new UserId(42L));
        gateway.ownedAccounts(new UserId(42L));
        // Then the expired entry is bypassed and banks is hit each time
        verify(feign, times(2)).listAccounts(42L);
    }
}
