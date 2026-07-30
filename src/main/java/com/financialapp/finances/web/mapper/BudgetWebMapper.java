package com.financialapp.finances.web.mapper;

import com.financialapp.finances.domain.common.model.CategoryId;
import com.financialapp.finances.domain.common.model.Money;
import com.financialapp.finances.domain.common.model.UserId;
import com.financialapp.finances.domain.model.budget.Budget;
import com.financialapp.finances.domain.model.budget.BudgetPeriod;
import com.financialapp.finances.domain.service.BudgetPaceResult;
import com.financialapp.finances.domain.usecase.budget.BudgetPaceView;
import com.financialapp.finances.domain.usecase.budget.BudgetView;
import com.financialapp.finances.domain.usecase.budget.command.UpsertBudgetCommand;
import com.financialapp.finances.web.dto.request.UpsertBudgetRequest;
import com.financialapp.finances.web.dto.response.BudgetPaceResponse;
import com.financialapp.finances.web.dto.response.BudgetResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Currency;

@Component
public class BudgetWebMapper {

    public UpsertBudgetCommand toUpsertCommand(Long userId, Long categoryId, UpsertBudgetRequest req) {
        BigDecimal threshold = req.alertThresholdPct() != null && !req.alertThresholdPct().isBlank()
                ? new BigDecimal(req.alertThresholdPct())
                : null;
        return new UpsertBudgetCommand(
                new UserId(userId),
                new CategoryId(categoryId),
                new BudgetPeriod(req.year(), req.month()),
                new Money(new BigDecimal(req.amount()), Currency.getInstance(req.currency())),
                threshold
        );
    }

    public BudgetResponse toBudgetResponse(BudgetView view) {
        Budget b = view.budget();
        return new BudgetResponse(
                b.categoryId().value(),
                view.categoryName(),
                b.period().year(),
                b.period().month(),
                b.amount().amount().toPlainString(),
                b.amount().currency().getCurrencyCode(),
                b.alertThresholdPct() != null ? b.alertThresholdPct().toPlainString() : null
        );
    }

    public BudgetPaceResponse toBudgetPaceResponse(BudgetPaceView view) {
        Budget b = view.budget();
        BudgetPaceResult p = view.pace();
        String spentStr = p.spent() != null ? p.spent().amount().toPlainString() : "0.00";
        String remainingStr = p.remaining() != null ? p.remaining().amount().toPlainString() : null;
        return new BudgetPaceResponse(
                b.categoryId().value(),
                view.categoryName(),
                spentStr,
                remainingStr,
                p.pctUsed().toPlainString(),
                p.expectedPctByToday().toPlainString(),
                p.overBudget(),
                b.amount().currency().getCurrencyCode()
        );
    }
}
