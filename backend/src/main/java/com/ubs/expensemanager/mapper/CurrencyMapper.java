package com.ubs.expensemanager.mapper;

import com.ubs.expensemanager.dto.response.CurrencyResponse;
import com.ubs.expensemanager.model.Currency;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CurrencyMapper {

  CurrencyResponse toResponse(Currency currency);

}
