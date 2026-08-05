package com.financialapp.finances.infrastructure.persistence.repository;

import com.financialapp.commons.core.domain.model.PageResult;
import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.commons.core.domain.model.Cbu;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.TransactionId;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.transaction.*;
import com.financialapp.finances.domain.repository.TransactionRepository;
import com.financialapp.finances.domain.service.TransactionClassifier;
import com.financialapp.finances.infrastructure.persistence.entity.TransactionJpaEntity;
import com.financialapp.finances.infrastructure.persistence.jpa.TransactionJpaRepository;
import com.financialapp.finances.infrastructure.persistence.mapper.TransactionPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepository {

    private final TransactionJpaRepository jpa;
    private final TransactionPersistenceMapper mapper;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SystemCategoryResolver systemCategoryResolver;
    private final TransactionClassifier classifier = new TransactionClassifier();

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

    @Override
    public PageResult<Transaction> findFiltered(TransactionFilter filter, CursorPage page) {
        if (filter.kind() != null && filter.ownedAccounts().isEmpty()) {
            return new PageResult<>(List.of(), false, null, 0L);
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", filter.userId().value());

        StringBuilder whereSql = new StringBuilder(" WHERE t.user_id = :userId");

        if (filter.accountCbu() != null) {
            whereSql.append(" AND (t.from_cbu = :accountCbu OR t.to_cbu = :accountCbu)");
            params.addValue("accountCbu", filter.accountCbu().cbuNumber());
        }

        if (filter.categoryId() != null) {
            whereSql.append(" AND t.category_id = :categoryId");
            params.addValue("categoryId", filter.categoryId().value());
        }

        if (filter.dateRange() != null) {
            whereSql.append(" AND t.date >= :fromDate AND t.date <= :toDate");
            params.addValue("fromDate", filter.dateRange().from());
            params.addValue("toDate", filter.dateRange().to());
        }

        if (filter.onlyUncategorised()) {
            Optional<Long> unassignedOpt = systemCategoryResolver.findUnassignedCategoryId();
            if (unassignedOpt.isEmpty()) {
                return new PageResult<>(List.of(), false, null, 0L);
            }
            whereSql.append(" AND t.category_id = :unassignedId");
            params.addValue("unassignedId", unassignedOpt.get());
        }

        if (filter.amountMin() != null) {
            whereSql.append(" AND t.amount >= :amountMin");
            params.addValue("amountMin", filter.amountMin().amount());
        }

        if (filter.amountMax() != null) {
            whereSql.append(" AND t.amount <= :amountMax");
            params.addValue("amountMax", filter.amountMax().amount());
        }

        if (filter.kind() != null) {
            KindOwnershipCriteria criteria = classifier.criteriaFor(filter.kind());
            Set<String> ownedCbus = filter.ownedAccounts().stream().map(Cbu::cbuNumber).collect(Collectors.toSet());
            if (ownedCbus.isEmpty()) {
                return new PageResult<>(List.of(), false, null, 0L);
            }
            params.addValue("ownedCbus", ownedCbus);
            if (criteria.fromOwned()) {
                whereSql.append(" AND t.from_cbu IN (:ownedCbus)");
            } else {
                whereSql.append(" AND t.from_cbu NOT IN (:ownedCbus)");
            }
            if (criteria.toOwned()) {
                whereSql.append(" AND t.to_cbu IN (:ownedCbus)");
            } else {
                whereSql.append(" AND t.to_cbu NOT IN (:ownedCbus)");
            }
        }

        // Count total elements before applying cursor condition
        String countSql = "SELECT COUNT(*) FROM finances.transactions t" + whereSql;
        Long totalElements = jdbcTemplate.queryForObject(countSql, params, Long.class);
        if (totalElements == null) totalElements = 0L;

        // Cursor condition
        LocalDate cursorDate = page.decodedDate();
        Long cursorId = page.decodedId();
        if (cursorDate != null && cursorId != null) {
            whereSql.append(" AND (t.date < :cursorDate OR (t.date = :cursorDate AND t.id < :cursorId))");
            params.addValue("cursorDate", cursorDate);
            params.addValue("cursorId", cursorId);
        }

        int pageSize = page.size();
        params.addValue("pageSize", pageSize + 1);

        String selectSql = "SELECT t.id, t.user_id, t.from_cbu, t.to_cbu, t.amount, t.currency, t.category_id, t.description, t.date, t.payment_method, t.note " +
                "FROM finances.transactions t" + whereSql + " ORDER BY t.date DESC, t.id DESC LIMIT :pageSize";

        List<Transaction> rows = jdbcTemplate.query(selectSql, params, (rs, rowNum) -> {
            String pmStr = rs.getString("payment_method");
            PaymentMethod pm = pmStr != null ? PaymentMethod.valueOf(pmStr) : PaymentMethod.OTHER;
            return Transaction.reconstitute(
                    new TransactionId(rs.getLong("id")),
                    new UserId(rs.getLong("user_id")),
                    new Cbu(rs.getString("from_cbu")),
                    new Cbu(rs.getString("to_cbu")),
                    new Money(rs.getBigDecimal("amount"), Currency.getInstance(rs.getString("currency"))),
                    new CategoryId(rs.getLong("category_id")),
                    rs.getString("description"),
                    rs.getDate("date").toLocalDate(),
                    pm,
                    rs.getString("note")
            );
        });

        boolean hasNext = rows.size() > pageSize;
        List<Transaction> content = hasNext ? rows.subList(0, pageSize) : rows;
        String nextCursor = null;
        if (hasNext && !content.isEmpty()) {
            Transaction last = content.get(content.size() - 1);
            nextCursor = CursorPage.encode(last.date(), last.id().value());
        }

        return new PageResult<>(content, hasNext, nextCursor, totalElements);
    }

    @Override
    public long countUncategorised(UserId userId) {
        Optional<Long> unassignedIdOpt = systemCategoryResolver.findUnassignedCategoryId();
        if (unassignedIdOpt.isEmpty()) {
            return 0L;
        }
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId.value())
                .addValue("unassignedId", unassignedIdOpt.get());
        String sql = "SELECT COUNT(*) FROM finances.transactions WHERE user_id = :userId AND category_id = :unassignedId";
        Long count = jdbcTemplate.queryForObject(sql, params, Long.class);
        return count != null ? count : 0L;
    }
}
