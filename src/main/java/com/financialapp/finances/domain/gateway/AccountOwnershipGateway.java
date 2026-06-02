package com.financialapp.finances.domain.gateway;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.UserId;

import java.util.Set;

/**
 * Outbound port: the set of account CBUs a user owns, as known by the authoritative account owner
 * (ms-banks). Ownership is never trusted from the client. The application uses this to feed
 * {@code TransactionPosting}. The Feign adapter and short-TTL cache live in infrastructure.
 */
public interface AccountOwnershipGateway {
    Set<Cbu> ownedAccounts(UserId userId);
}
