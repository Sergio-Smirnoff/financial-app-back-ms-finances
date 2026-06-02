package com.financialapp.finances.infrastructure.persistence.repository;

import com.financialapp.finances.domain.common.model.Cbu;
import com.financialapp.finances.domain.common.model.TransactionId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.transaction.Transaction;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.infrastructure.persistence.entity.TransactionJpaEntity;
import com.financialapp.finances.infrastructure.persistence.jpa.TransactionJpaRepository;
import com.financialapp.finances.infrastructure.persistence.mapper.TransactionPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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

    @Override
    public List<Transaction> findByUser(UserId userId) {
        return jpa.findByUserIdOrderByDateDescIdDesc(userId.value())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Transaction> findByUserAndDateBetween(UserId userId, LocalDate from, LocalDate to) {
        return jpa.findByUserIdAndDateBetweenOrderByDateDescIdDesc(userId.value(), from, to)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Transaction> findByAccount(Cbu accountCbu, Integer limit, LocalDate from, LocalDate to) {
        Limit lim = limit == null ? Limit.unlimited() : Limit.of(limit);
        return jpa.findByAccount(accountCbu.cbuNumber(), from, to, lim)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsDuplicate(UserId userId, Cbu fromCbu, Cbu toCbu,
                                   BigDecimal amount, String currency, LocalDate date, String description) {
        return jpa.existsDuplicate(
                userId.value(), fromCbu.cbuNumber(), toCbu.cbuNumber(), amount, currency, date, description);
    }

    @Override
    public void delete(Transaction transaction) {
        jpa.deleteById(transaction.id().value());
    }
}
