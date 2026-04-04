package com.financialapp.finances.service;

import com.financialapp.finances.model.dto.response.UpcomingPaymentResponse;
import com.financialapp.finances.model.entity.CardExpenseInstallment;
import com.financialapp.finances.model.entity.LoanInstallment;
import com.financialapp.finances.repository.CardExpenseInstallmentRepository;
import com.financialapp.finances.repository.LoanInstallmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UpcomingPaymentService {

    private final LoanInstallmentRepository loanInstallmentRepository;
    private final CardExpenseInstallmentRepository cardExpenseInstallmentRepository;

    @Transactional(readOnly = true)
    public List<UpcomingPaymentResponse> getUpcomingPayments(Long userId, LocalDate from, LocalDate to, String currency) {
        List<LoanInstallment> loanInstallments =
                loanInstallmentRepository.findUpcomingUnpaidByUser(userId, from, to, currency);

        List<CardExpenseInstallment> cardExpenseInstallments =
                cardExpenseInstallmentRepository.findUpcomingUnpaidByUser(userId, from, to, currency);

        List<UpcomingPaymentResponse> result = new ArrayList<>();

        for (LoanInstallment li : loanInstallments) {
            result.add(UpcomingPaymentResponse.builder()
                    .sourceId(li.getLoan().getId())
                    .type("LOAN")
                    .description(li.getLoan().getDescription())
                    .amount(li.getAmount())
                    .currency(li.getLoan().getCurrency())
                    .dueDate(li.getDueDate())
                    .installmentNumber(li.getInstallmentNumber())
                    .totalInstallments(li.getLoan().getTotalInstallments())
                    .paid(li.isPaid())
                    .build());
        }

        for (CardExpenseInstallment cei : cardExpenseInstallments) {
            result.add(UpcomingPaymentResponse.builder()
                    .sourceId(cei.getCardExpense().getId())
                    .type("CARD_EXPENSE")
                    .description(cei.getCardExpense().getDescription())
                    .amount(cei.getAmount())
                    .currency(cei.getCardExpense().getCurrency())
                    .dueDate(cei.getDueDate())
                    .installmentNumber(cei.getInstallmentNumber())
                    .totalInstallments(cei.getCardExpense().getTotalInstallments())
                    .paid(cei.isPaid())
                    .build());
        }

        result.sort(Comparator.comparing(UpcomingPaymentResponse::getDueDate));
        return result;
    }
}
