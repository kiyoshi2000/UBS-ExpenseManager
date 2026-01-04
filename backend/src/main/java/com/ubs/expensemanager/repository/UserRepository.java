package com.ubs.expensemanager.repository;

import com.ubs.expensemanager.model.User;
import com.ubs.expensemanager.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends
        JpaRepository<User, Long>,
        JpaSpecificationExecutor<User>
{
    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);
    Boolean existsByEmailAndIdNot(String email, Long id);
    Boolean existsByManagerAndActiveTrue(User manager);

    List<User> findByRole(UserRole role);
}