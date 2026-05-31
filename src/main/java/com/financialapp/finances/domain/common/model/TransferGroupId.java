package com.financialapp.finances.domain.common.model;

import java.util.UUID;

/**
 * Links the two legs of a transfer. A pure identity id object over a UUID — it holds no
 * account references; each leg carries its own {@link Cbu}. A {@code Transfer} factory (later
 * slice) stamps both legs with one shared id.
 */
public record TransferGroupId(UUID id) {

    public TransferGroupId {
        if (id == null) {
            throw new IllegalArgumentException("transferGroupId must not be null");
        }
    }

    public static TransferGroupId newId() {
        return new TransferGroupId(UUID.randomUUID());
    }
} 
