package com.financialapp.finances.repository;

import com.financialapp.finances.model.entity.Category;
import com.financialapp.finances.model.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.active = true " +
           "AND (c.system = true OR c.userId = :userId) " +
           "AND (:type IS NULL OR c.type = :type) " +
           "AND (:isSystem IS NULL OR c.system = :isSystem) " +
           "ORDER BY c.system DESC, c.name ASC")
    List<Category> findVisibleParentCategories(
            @Param("userId") Long userId,
            @Param("type") CategoryType type,
            @Param("isSystem") Boolean isSystem);

    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.active = true " +
           "AND (c.system = true OR c.userId = :userId) " +
           "ORDER BY c.system DESC, c.name ASC")
    List<Category> findVisibleSubcategories(
            @Param("parentId") Long parentId,
            @Param("userId") Long userId);

    @Query("SELECT c FROM Category c WHERE c.parent.id IN :parentIds AND c.active = true " +
           "AND (c.system = true OR c.userId = :userId) " +
           "ORDER BY c.system DESC, c.name ASC")
    List<Category> findVisibleSubcategoriesForParents(
            @Param("parentIds") Collection<Long> parentIds,
            @Param("userId") Long userId);

    @Query("SELECT c FROM Category c WHERE c.active = true " +
           "AND (c.system = true OR c.userId = :userId) " +
           "AND (:type IS NULL OR c.type = :type) " +
           "AND (:isSystem IS NULL OR c.system = :isSystem) " +
           "ORDER BY c.parent.id NULLS FIRST, c.name ASC")
    List<Category> findAllVisibleFlat(
            @Param("userId") Long userId,
            @Param("type") CategoryType type,
            @Param("isSystem") Boolean isSystem);

    boolean existsByIdAndSystemFalseAndUserId(Long id, Long userId);

    boolean existsByIdAndParentIsNotNull(Long id);
}
