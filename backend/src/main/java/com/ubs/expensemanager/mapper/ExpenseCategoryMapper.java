package com.ubs.expensemanager.mapper;

import com.ubs.expensemanager.dto.response.ExpenseCategoryResponse;
import com.ubs.expensemanager.model.ExpenseCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ExpenseCategoryMapper {

  @Mapping(target = "currencyName", source = "currency.name")
  @Mapping(target = "exchangeRate", source = "currency.exchangeRate")
  ExpenseCategoryResponse toResponse(ExpenseCategory category);

}
