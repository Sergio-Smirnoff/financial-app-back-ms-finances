package com.financialapp.finances.service;

import com.financialapp.finances.exception.BusinessException;
import com.financialapp.finances.exception.ResourceNotFoundException;
import com.financialapp.finances.mapper.LoanMapper;
import com.financialapp.finances.model.dto.request.LoanRequest;
import com.financialapp.finances.model.dto.request.LoanUpdateRequest;
import com.financialapp.finances.model.dto.response.LoanResponse;
import com.financialapp.finances.model.entity.Loan;
import com.financialapp.finances.model.entity.LoanInstallment;
import com.financialapp.finances.repository.LoanInstallmentRepository;
import com.financialapp.finances.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {

    private final LoanRepository loanRepository;
    private final LoanInstallmentRepository installmentRepository;
    private final LoanMapper loanMapper;

    @Transactional(readOnly = true)
    public List<LoanResponse> getLoans(Long userId, Boolean active, String currency) {
        return loanRepository.findFiltered(userId, active, currency)
                .stream()
                .map(loanMapper::toResponse)
                .toList();
    }

    @Transactional
    public LoanResponse create(Long userId, LoanRequest request) {
        Loan loan = Loan.builder()
                .userId(userId)
                .description(request.getDescription())
                .entity(request.getEntity())
                .totalAmount(request.getTotalAmount())
                .currency(request.getCurrency())
                .totalInstallments(request.getTotalInstallments())
                .paidInstallments(0)
                .installmentAmount(request.getInstallmentAmount())
                .nextPaymentDate(request.getFirstPaymentDate())
                .active(true)
                .build();
        Loan savedLoan = loanRepository.save(loan);

        List<LoanInstallment> installments = new ArrayList<>();
        LocalDate dueDate = request.getFirstPaymentDate();
        for (int i = 1; i <= request.getTotalInstallments(); i++) {
            installments.add(LoanInstallment.builder()
                    .loan(savedLoan)
                    .installmentNumber(i)
                    .amount(request.getInstallmentAmount())
                    .dueDate(dueDate)
                    .paid(false)
                    .build());
            dueDate = dueDate.plusMonths(1);
        }
        installmentRepository.saveAll(installments);
        log.info("Created loan id={} with {} installments for userId={}", savedLoan.getId(),
                request.getTotalInstallments(), userId);
        return loanMapper.toResponse(savedLoan);
    }

    @Transactional(readOnly = true)
    public LoanResponse getById(Long id, Long userId) {
        return loanMapper.toResponse(findOwnedLoan(id, userId));
    }

    @Transactional
    public LoanResponse update(Long id, Long userId, LoanUpdateRequest request) {
        Loan loan = findOwnedLoan(id, userId);
        loan.setDescription(request.getDescription());
        loan.setEntity(request.getEntity());
        return loanMapper.toResponse(loanRepository.save(loan));
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Loan loan = findOwnedLoan(id, userId);
        loanRepository.delete(loan);
        log.info("Deleted loan id={} for userId={}", id, userId);
    }

    Loan findOwnedLoan(Long id, Long userId) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan", id));
        if (!userId.equals(loan.getUserId())) {
            throw new ResourceNotFoundException("Loan", id);
        }
        return loan;
    }

    @Transactional
    public void markLoanClosedIfFullyPaid(Loan loan) {
        if (loan.getPaidInstallments() >= loan.getTotalInstallments()) {
            loan.setActive(false);
            loan.setNextPaymentDate(null);
            loanRepository.save(loan);
            log.info("Loan id={} fully paid — marked inactive", loan.getId());
        }
    }

    @Transactional
    public void updateNextPaymentDate(Loan loan) {
        List<LoanInstallment> unpaid = installmentRepository.findUnpaidByLoanId(loan.getId());
        if (!unpaid.isEmpty()) {
            loan.setNextPaymentDate(unpaid.getFirst().getDueDate());
        } else {
            loan.setNextPaymentDate(null);
        }
        loanRepository.save(loan);
    }
}
