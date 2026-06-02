package com.financialapp.finances.infrastructure.gateway;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.OwnedAccount;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.infrastructure.gateway.Impl.BankAccountOwnershipGateway;
import com.financialapp.finances.infrastructure.gateway.dto.BankAccountResponse;
import com.financialapp.finances.infrastructure.gateway.dto.GatewayApiResponse;
import org.junit.jupiter.api.Test;

import java.util.Currency;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BankAccountOwnershipGatewayTest {

    private final BanksAccountsFeignClient feign = mock(BanksAccountsFeignClient.class);
    private final BankAccountOwnershipGateway gateway =
            new BankAccountOwnershipGateway(feign, 60_000L);

    @Test
    void mapsEnvelopeDataToCbuSet() {
        when(feign.listAccounts(42L)).thenReturn(new GatewayApiResponse<>(List.of(
                new BankAccountResponse("0001112223334445556667", "ARS"),
                new BankAccountResponse("9998887776665554443332", "USD"))));

        Set<Cbu> owned = gateway.ownedAccounts(new UserId(42L));

        assertThat(owned).containsExactlyInAnyOrder(
                new Cbu("0001112223334445556667"), new Cbu("9998887776665554443332"));
    }

    @Test
    void mapsEnvelopeDataToOwnedAccountsWithCurrency() {
        when(feign.listAccounts(42L)).thenReturn(new GatewayApiResponse<>(List.of(
                new BankAccountResponse("0001112223334445556667", "ARS"),
                new BankAccountResponse("9998887776665554443332", "USD"))));

        Set<OwnedAccount> owned = gateway.ownedAccountsWithCurrency(new UserId(42L));

        assertThat(owned).containsExactlyInAnyOrder(
                new OwnedAccount(new Cbu("0001112223334445556667"), Currency.getInstance("ARS")),
                new OwnedAccount(new Cbu("9998887776665554443332"), Currency.getInstance("USD")));
    }

    @Test
    void cachesWithinTtlSoBanksIsCalledOnce() {
        when(feign.listAccounts(42L)).thenReturn(new GatewayApiResponse<>(List.of(
                new BankAccountResponse("0001112223334445556667", "ARS"))));

        gateway.ownedAccounts(new UserId(42L));
        gateway.ownedAccountsWithCurrency(new UserId(42L));

        verify(feign, times(1)).listAccounts(42L);
    }

    @Test
    void returnsEmptySetWhenDataIsNull() {
        when(feign.listAccounts(7L)).thenReturn(new GatewayApiResponse<>(null));
        assertThat(gateway.ownedAccounts(new UserId(7L))).isEmpty();
    }
}
