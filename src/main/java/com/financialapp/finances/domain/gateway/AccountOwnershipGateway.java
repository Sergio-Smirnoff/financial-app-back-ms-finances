package com.financialapp.finances.domain.gateway;

import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.finances.domain.common.model.OwnedAccount;
import com.financialapp.finances.domain.common.model.UserId;

import java.util.Set;

/**
 * Outbound port: the accounts a user owns, as known by the authoritative account owner (ms-banks).
 * Ownership is never trusted from the client. The Feign adapter and short-TTL cache live in
 * infrastructure.
 */
public interface AccountOwnershipGateway {

    /** Just the owned CBUs — feeds {@code TransactionPosting} and classification. */
    Set<Cbu> ownedAccounts(UserId userId);

    /** Owned accounts paired with their authoritative currency — used to validate a transaction's currency. */
    Set<OwnedAccount> ownedAccountsWithCurrency(UserId userId);
}
