package com.financialapp.finances.infrastructure.persistence.jpa;

import com.financialapp.finances.infrastructure.persistence.entity.TransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, Long> {
    Optional<TransactionJpaEntity> findByIdAndUserId(Long id, Long userId);
}
