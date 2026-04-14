package com.financialapp.finances.service;

import com.financialapp.finances.exception.BusinessException;
import com.financialapp.finances.exception.ResourceNotFoundException;
import com.financialapp.finances.mapper.CardExpenseMapper;
import com.financialapp.finances.model.dto.response.CardExpenseInstallmentResponse;
import com.financialapp.finances.model.entity.CardExpense;
import com.financialapp.finances.model.entity.CardExpenseInstallment;
import com.financialapp.finances.repository.CardExpenseInstallmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardExpenseInstallmentService {

    private final CardExpenseInstallmentRepository installmentRepository;
    private final CardExpenseService cardExpenseService;
    private final CardExpenseMapper cardExpenseMapper;

    /**
     * @deprecated Use {@link #payInstallment(Long, Long, Long)} with explicit installmentId instead.
     */
    @Deprecated
    @Transactional
    public CardExpenseInstallmentResponse payNextInstallment(Long cardExpenseId, Long userId) {
        CardExpense expense = cardExpenseService.findOwnedExpense(cardExpenseId, userId);
        List<CardExpenseInstallment> unpaid = installmentRepository.findUnpaidByCardExpenseId(cardExpenseId);
        if (unpaid.isEmpty()) {
            throw new BusinessException("No unpaid installments found for card expense id=" + cardExpenseId);
        }
        return payInstallment(cardExpenseId, unpaid.getFirst().getId(), userId);
    }

    @Transactional(readOnly = true)
    public List<CardExpenseInstallmentResponse> getInstallments(Long cardExpenseId, Long userId) {
        cardExpenseService.findOwnedExpense(cardExpenseId, userId);
        return installmentRepository.findByCardExpenseIdOrderByInstallmentNumberAsc(cardExpenseId)
                .stream()
                .map(cardExpenseMapper::toInstallmentResponse)
                .toList();
    }

    @Transactional
    public CardExpenseInstallmentResponse payInstallment(Long cardExpenseId, Long installmentId, Long userId) {
        CardExpense expense = cardExpenseService.findOwnedExpense(cardExpenseId, userId);

        if (!expense.isActive()) {
            throw new BusinessException("Cannot pay an installment of an inactive card expense");
        }

        CardExpenseInstallment installment = installmentRepository.findById(installmentId)
                .orElseThrow(() -> new ResourceNotFoundException("CardExpenseInstallment", installmentId));

        if (!installment.getCardExpense().getId().equals(cardExpenseId)) {
            throw new ResourceNotFoundException("CardExpenseInstallment", installmentId);
        }

        if (installment.isPaid()) {
            throw new BusinessException("This installment has already been paid");
        }

        // Validate no previous unpaid installments
        if (installmentRepository.countUnpaidBefore(cardExpenseId, installment.getInstallmentNumber()) > 0) {
            throw new BusinessException(
                    "Cannot pay installment #" + installment.getInstallmentNumber() +
                    " — a previous installment is still unpaid");
        }

        installment.setPaid(true);
        installment.setPaidDate(LocalDate.now());
        installmentRepository.save(installment);

        expense.setRemainingInstallments(expense.getRemainingInstallments() - 1);
        cardExpenseService.updateNextPaymentDate(expense);
        cardExpenseService.markClosedIfFullyPaid(expense);

        log.info("Paid installment #{} of card expense id={} for userId={}",
                installment.getInstallmentNumber(), cardExpenseId, userId);
        return cardExpenseMapper.toInstallmentResponse(installment);
    }
}
