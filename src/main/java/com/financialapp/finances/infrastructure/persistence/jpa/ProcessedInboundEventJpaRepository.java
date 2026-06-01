package com.financialapp.finances.infrastructure.persistence.jpa;

import com.financialapp.finances.infrastructure.persistence.entity.ProcessedInboundEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedInboundEventJpaRepository
        extends JpaRepository<ProcessedInboundEventJpaEntity, String> {
}
