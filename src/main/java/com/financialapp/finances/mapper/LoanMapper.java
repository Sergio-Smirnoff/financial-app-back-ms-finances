package com.financialapp.finances.mapper;

import com.financialapp.finances.model.dto.response.LoanInstallmentResponse;
import com.financialapp.finances.model.dto.response.LoanResponse;
import com.financialapp.finances.model.entity.Loan;
import com.financialapp.finances.model.entity.LoanInstallment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanMapper {

    LoanResponse toResponse(Loan loan);

    @Mapping(target = "loanId", source = "loan.id")
    LoanInstallmentResponse toInstallmentResponse(LoanInstallment installment);
}
