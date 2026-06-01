package com.financialapp.finances.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_inbound_event", schema = "finances")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProcessedInboundEventJpaEntity {

    @Id
    @Column(name = "dedup_key", length = 128)
    private String dedupKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
