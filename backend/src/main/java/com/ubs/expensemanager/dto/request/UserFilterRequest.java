package com.ubs.expensemanager.dto.request;

import com.ubs.expensemanager.model.UserRole;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString // good for logging the request at the controller
public class UserFilterRequest {
    // filter by user role
    private UserRole role;

    // include inactive users
    private Boolean includeInactive = false;

    // search name or email
    private String search;
}
