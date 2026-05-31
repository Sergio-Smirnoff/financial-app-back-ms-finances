package com.financialapp.finances.mapper;

import com.financialapp.finances.model.dto.response.TransactionResponse;
import com.financialapp.finances.model.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "accountId", source = "accountId")
    @Mapping(target = "transferGroupId", source = "transferGroupId")
    TransactionResponse toResponse(Transaction transaction);
}
