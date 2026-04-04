package com.financialapp.finances.mapper;

import com.financialapp.finances.model.dto.response.CardExpenseInstallmentResponse;
import com.financialapp.finances.model.dto.response.CardExpenseResponse;
import com.financialapp.finances.model.entity.CardExpense;
import com.financialapp.finances.model.entity.CardExpenseInstallment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardExpenseMapper {

    CardExpenseResponse toResponse(CardExpense cardExpense);

    @Mapping(target = "cardExpenseId", source = "cardExpense.id")
    CardExpenseInstallmentResponse toInstallmentResponse(CardExpenseInstallment installment);
}
