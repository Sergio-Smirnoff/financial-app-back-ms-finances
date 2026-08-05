package com.financialapp.finances.infrastructure.gateway.Impl;

import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.finances.domain.common.model.OwnedAccount;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.gateway.AccountOwnershipGateway;
import com.financialapp.finances.infrastructure.gateway.BanksAccountsFeignClient;
import com.financialapp.finances.infrastructure.gateway.dto.BankAccountResponse;
import com.financialapp.finances.infrastructure.gateway.dto.GatewayApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class BankAccountOwnershipGateway implements AccountOwnershipGateway {

    private record CacheEntry(Set<OwnedAccount> accounts, long expiresAt) {}

    private final BanksAccountsFeignClient feign;
    private final long ttlMillis;
    private final ConcurrentHashMap<Long, CacheEntry> cache = new ConcurrentHashMap<>();

    public BankAccountOwnershipGateway(
            BanksAccountsFeignClient feign,
            @Value("${finances.ownership.cache-ttl-ms:60000}") long ttlMillis) {
        this.feign = feign;
        this.ttlMillis = ttlMillis;
    }

    @Override
    public Set<OwnedAccount> ownedAccountsWithCurrency(UserId userId) {
        long now = System.currentTimeMillis();
        CacheEntry cached = cache.get(userId.value());
        if (cached != null && cached.expiresAt() > now) {
            return cached.accounts();
        }
        Set<OwnedAccount> accounts = fetch(userId);
        cache.put(userId.value(), new CacheEntry(accounts, now + ttlMillis));
        return accounts;
    }

    @Override
    public Set<Cbu> ownedAccounts(UserId userId) {
        return ownedAccountsWithCurrency(userId).stream()
                .map(OwnedAccount::cbu)
                .collect(Collectors.toUnmodifiableSet());
    }

    private Set<OwnedAccount> fetch(UserId userId) {
        GatewayApiResponse<List<BankAccountResponse>> response = feign.listAccounts(userId.value());
        List<BankAccountResponse> data = response == null ? null : response.data();
        if (data == null) {
            return Set.of();
        }
        return data.stream()
                .map(a -> new OwnedAccount(new Cbu(a.cbu()), Currency.getInstance(a.currency())))
                .collect(Collectors.toUnmodifiableSet());
    }
}
