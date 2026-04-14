package com.financialapp.finances.service;

import com.financialapp.finances.exception.BusinessException;
import com.financialapp.finances.exception.ResourceNotFoundException;
import com.financialapp.finances.mapper.LoanMapper;
import com.financialapp.finances.model.dto.response.LoanInstallmentResponse;
import com.financialapp.finances.model.entity.Loan;
import com.financialapp.finances.model.entity.LoanInstallment;
import com.financialapp.finances.repository.LoanInstallmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanInstallmentService {

    private final LoanInstallmentRepository installmentRepository;
    private final LoanService loanService;
    private final LoanMapper loanMapper;

    @Transactional(readOnly = true)
    public List<LoanInstallmentResponse> getInstallments(Long loanId, Long userId) {
        loanService.findOwnedLoan(loanId, userId);
        return installmentRepository.findByLoanIdOrderByInstallmentNumberAsc(loanId)
                .stream()
                .map(loanMapper::toInstallmentResponse)
                .toList();
    }

    @Transactional
    public LoanInstallmentResponse payInstallment(Long loanId, Long installmentId, Long userId) {
        Loan loan = loanService.findOwnedLoan(loanId, userId);

        if (!loan.isActive()) {
            throw new BusinessException("Cannot pay an installment of an inactive loan");
        }

        LoanInstallment installment = installmentRepository.findById(installmentId)
                .orElseThrow(() -> new ResourceNotFoundException("LoanInstallment", installmentId));

        if (!installment.getLoan().getId().equals(loanId)) {
            throw new ResourceNotFoundException("LoanInstallment", installmentId);
        }

        if (installment.isPaid()) {
            throw new BusinessException("This installment has already been paid");
        }

        // Validate no previous unpaid installments
        if (installmentRepository.countUnpaidBefore(loanId, installment.getInstallmentNumber()) > 0) {
            throw new BusinessException(
                    "Cannot pay installment #" + installment.getInstallmentNumber() +
                    " — a previous installment is still unpaid");
        }

        installment.setPaid(true);
        installment.setPaidDate(LocalDate.now());
        installmentRepository.save(installment);

        loan.setPaidInstallments(loan.getPaidInstallments() + 1);
        loanService.updateNextPaymentDate(loan);
        loanService.markLoanClosedIfFullyPaid(loan);

        log.info("Paid installment #{} of loan id={} for userId={}",
                installment.getInstallmentNumber(), loanId, userId);
        return loanMapper.toInstallmentResponse(installment);
    }
}
