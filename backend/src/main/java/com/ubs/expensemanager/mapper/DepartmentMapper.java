package com.ubs.expensemanager.mapper;

import com.ubs.expensemanager.dto.response.DepartmentResponse;
import com.ubs.expensemanager.model.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DepartmentMapper {

  @Mapping(source = "currency.id", target = "currencyId")
  @Mapping(source = "currency.name", target = "currencyName")
  DepartmentResponse toResponse(Department department);

}
