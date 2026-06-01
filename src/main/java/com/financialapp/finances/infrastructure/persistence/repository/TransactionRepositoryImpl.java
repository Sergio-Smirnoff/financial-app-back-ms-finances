package com.financialapp.finances.infrastructure.persistence.repository;

import com.financialapp.finances.domain.common.model.TransactionId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.infrastructure.persistence.entity.TransactionJpaEntity;
import com.financialapp.finances.infrastructure.persistence.jpa.TransactionJpaRepository;
import com.financialapp.finances.infrastructure.persistence.mapper.TransactionPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepository {

    private final TransactionJpaRepository jpa;
    private final TransactionPersistenceMapper mapper;

    @Override
    public Transaction save(Transaction transaction) {
        TransactionJpaEntity saved = jpa.save(mapper.toEntity(transaction));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Transaction> findByIdOwnedBy(TransactionId id, UserId userId) {
        return jpa.findByIdAndUserId(id.value(), userId.value()).map(mapper::toDomain);
    }
}
