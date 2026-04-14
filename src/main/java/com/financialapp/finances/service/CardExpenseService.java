package com.financialapp.finances.service;

import com.financialapp.finances.exception.ResourceNotFoundException;
import com.financialapp.finances.mapper.CardExpenseMapper;
import com.financialapp.finances.model.dto.request.CardExpenseRequest;
import com.financialapp.finances.model.dto.request.CardExpenseUpdateRequest;
import com.financialapp.finances.model.dto.response.CardExpenseResponse;
import com.financialapp.finances.model.entity.CardExpense;
import com.financialapp.finances.model.entity.CardExpenseInstallment;
import com.financialapp.finances.repository.CardExpenseInstallmentRepository;
import com.financialapp.finances.repository.CardExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardExpenseService {

    private final CardExpenseRepository cardExpenseRepository;
    private final CardExpenseInstallmentRepository installmentRepository;
    private final CardExpenseMapper cardExpenseMapper;

    @Transactional(readOnly = true)
    public Page<CardExpenseResponse> getCardExpenses(Long userId, Boolean active, Long cardId, String currency,
                                                     Pageable pageable) {
        return cardExpenseRepository.findFiltered(userId, active, cardId, currency, pageable)
                .map(cardExpenseMapper::toResponse);
    }

    @Transactional
    public CardExpenseResponse create(Long userId, CardExpenseRequest request) {
        CardExpense expense = CardExpense.builder()
                .userId(userId)
                .cardId(request.getCardId())
                .description(request.getDescription())
                .totalAmount(request.getTotalAmount())
                .currency(request.getCurrency())
                .totalInstallments(request.getTotalInstallments())
                .remainingInstallments(request.getTotalInstallments())
                .installmentAmount(request.getInstallmentAmount())
                .nextDueDate(request.getNextDueDate())
                .active(true)
                .build();
        CardExpense saved = cardExpenseRepository.save(expense);

        List<CardExpenseInstallment> installments = new ArrayList<>();
        LocalDate dueDate = request.getNextDueDate();
        for (int i = 1; i <= request.getTotalInstallments(); i++) {
            installments.add(CardExpenseInstallment.builder()
                    .cardExpense(saved)
                    .installmentNumber(i)
                    .amount(request.getInstallmentAmount())
                    .dueDate(dueDate)
                    .paid(false)
                    .build());
            dueDate = dueDate.plusMonths(1);
        }
        installmentRepository.saveAll(installments);

        log.info("Created card expense id={} with {} installments for userId={}",
                saved.getId(), request.getTotalInstallments(), userId);
        return cardExpenseMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CardExpenseResponse getById(Long id, Long userId) {
        return cardExpenseMapper.toResponse(findOwnedExpense(id, userId));
    }

    @Transactional
    public CardExpenseResponse update(Long id, Long userId, CardExpenseUpdateRequest request) {
        CardExpense expense = findOwnedExpense(id, userId);
        expense.setCardId(request.getCardId());
        expense.setDescription(request.getDescription());
        return cardExpenseMapper.toResponse(cardExpenseRepository.save(expense));
    }

    @Transactional
    public void delete(Long id, Long userId) {
        CardExpense expense = findOwnedExpense(id, userId);
        cardExpenseRepository.delete(expense);
        log.info("Deleted card expense id={} for userId={}", id, userId);
    }

    CardExpense findOwnedExpense(Long id, Long userId) {
        CardExpense expense = cardExpenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CardExpense", id));
        if (!userId.equals(expense.getUserId())) {
            throw new ResourceNotFoundException("CardExpense", id);
        }
        return expense;
    }

    @Transactional
    public void updateNextPaymentDate(CardExpense expense) {
        List<CardExpenseInstallment> unpaid = installmentRepository.findUnpaidByCardExpenseId(expense.getId());
        if (!unpaid.isEmpty()) {
            expense.setNextDueDate(unpaid.getFirst().getDueDate());
        }
        cardExpenseRepository.save(expense);
    }

    @Transactional
    public void markClosedIfFullyPaid(CardExpense expense) {
        if (expense.getRemainingInstallments() <= 0) {
            expense.setActive(false);
            cardExpenseRepository.save(expense);
            log.info("Card expense id={} fully paid — marked inactive", expense.getId());
        }
    }
}
