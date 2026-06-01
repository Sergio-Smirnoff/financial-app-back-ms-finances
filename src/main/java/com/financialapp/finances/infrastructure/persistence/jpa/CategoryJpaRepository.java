package com.financialapp.finances.infrastructure.persistence.jpa;

import com.financialapp.finances.infrastructure.persistence.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, Long> {

    /** Top-level categories owned by the user (subcategories load via the {@code children} relationship). */
    List<CategoryJpaEntity> findByUserIdAndParentIsNull(Long userId);

    Optional<CategoryJpaEntity> findByIdAndUserIdAndParentIsNull(Long id, Long userId);
}
