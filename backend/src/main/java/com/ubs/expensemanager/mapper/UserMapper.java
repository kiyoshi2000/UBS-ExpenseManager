package com.ubs.expensemanager.mapper;

import com.ubs.expensemanager.dto.response.UserResponse;
import com.ubs.expensemanager.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

  @Mapping(target = "role", expression = "java(user.getRole().asAuthority())")
  UserResponse toResponse(User user);

}