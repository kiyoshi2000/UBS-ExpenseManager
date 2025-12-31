package com.ubs.expensemanager.repository.specification;

import com.ubs.expensemanager.model.User;
import com.ubs.expensemanager.model.UserRole;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specifications for filtering User entities.
 *
 * <p> This class provides reusable specifications that can be combined to build dynamic queries
 * for the User entity using Spring Data JPA Criteria API. </p>
 */
public class UserSpecifications {
    /**
     * Creates a specification to filter users by role.
     *
     * @param role the user role to filter by, or {@code null} to not apply this filter
     * @return a specification that matches users with the specified role, or {@code null} if role is {@code null}
     */
    public static Specification<User> withRole(UserRole role) {
        return (root, query, cb) ->
                role == null ? null : cb.equal(root.get("role"), role);
    }

    /**
     * Creates a specification to filter users by their active status.
     * <p> By default, only active users are returned. If {@code includeInactive} is {@code true},
     * both active and inactive users will be included in the results. </p>
     *
     * @param includeInactive {@code true} to include inactive users in results,
     * {@code false} or {@code null} to return only active users
     * @return a specification that filters by active status, or {@code null} if
     * inactive users should be included
     */
    public static Specification<User> isActive(Boolean includeInactive) {
        return (root, query, cb) -> {
            if (includeInactive == null || includeInactive) {
                return null;
            }
            return cb.isTrue(root.get("active"));
        };
    }


     /**
     * Creates a specification to filter users by role.
     *
     * @param search string to  filter name/email by, or {@code null} to not apply this filter
     * @return a specification that matches users with the search
     */
    public static Specification<User> nameOrEmailContains(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return null;
            }

            String pattern = "%" + search.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern)
            );
        };
    }

}
