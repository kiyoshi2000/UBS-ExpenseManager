package com.ubs.expensemanager.mapper;

import com.ubs.expensemanager.dto.request.ExpenseCreateRequest;
import com.ubs.expensemanager.dto.request.ExpenseUpdateRequest;
import com.ubs.expensemanager.dto.response.ExpenseResponse;
import com.ubs.expensemanager.model.Currency;
import com.ubs.expensemanager.model.Expense;
import com.ubs.expensemanager.model.ExpenseCategory;
import com.ubs.expensemanager.model.ExpenseStatus;
import com.ubs.expensemanager.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ExpenseMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "status", source = "status")
  @Mapping(target = "user", source = "currentUser")
  @Mapping(target = "currency", source = "currency")
  @Mapping(target = "expenseCategory", source = "expenseCategory")
  Expense toEntity(ExpenseCreateRequest expenseCreateRequest, Currency currency, ExpenseCategory expenseCategory, User currentUser, ExpenseStatus status);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "status", source = "status")
  @Mapping(target = "currency", source = "currency")
  @Mapping(target = "expenseCategory", source = "expenseCategory")
  Expense updateEntity(@MappingTarget Expense expense, ExpenseUpdateRequest expenseUpdateRequest, Currency currency, ExpenseCategory expenseCategory, ExpenseStatus status);

  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "userName", source = "user.name")
  @Mapping(target = "userEmail", source = "user.email")
  @Mapping(target = "expenseCategoryId", source = "expenseCategory.id")
  @Mapping(target = "expenseCategoryName", source = "expenseCategory.name")
  @Mapping(target = "currencyName", source = "currency.name")
  @Mapping(target = "exchangeRate", source = "currency.exchangeRate")
  ExpenseResponse toResponse(Expense expense);

}
