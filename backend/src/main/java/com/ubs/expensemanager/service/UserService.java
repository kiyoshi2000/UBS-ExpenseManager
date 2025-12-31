package com.ubs.expensemanager.service;

import com.ubs.expensemanager.dto.request.UserFilterRequest;
import com.ubs.expensemanager.dto.request.UserUpdateRequest;
import com.ubs.expensemanager.dto.request.UserCreateRequest;
import com.ubs.expensemanager.dto.response.UserResponse;
import com.ubs.expensemanager.exception.*;
import com.ubs.expensemanager.model.User;
import com.ubs.expensemanager.model.UserRole;
import com.ubs.expensemanager.repository.UserRepository;
import com.ubs.expensemanager.repository.specification.UserSpecifications;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Validates whether a manager can be associated with the given user and,
     * if valid, assigns it.
     *
     * <p>Rules enforced:</p>
     * <ul>
     *   <li>If the role is {@link UserRole#EMPLOYEE}, a manager email is mandatory.</li>
     *   <li>The manager must exist in the system.</li>
     *   <li>The manager must have the {@link UserRole#MANAGER} role.</li>
     *   <li>A user cannot be assigned as their own manager.</li>
     * </ul>
     *
     * @param user the user being created or updated
     * @param managerEmail the email of the manager to be assigned; may be {@code null}
     * when the role does not require a manager
     * @param role the role of the user, used to determine whether a manager is required
     *
     * @throws ManagerRequiredException if an EMPLOYEE is created without a manager
     * @throws InvalidManagerRoleException if the specified manager does not have MANAGER role
     * @throws SelfManagerException if the user is assigned as their own manager
     * @throws ResourceNotFoundException if no user is found with the given manager email
     */
    private void validateAndSetManager(User user, String managerEmail, UserRole role) {
        // If manager email isn't provided and the user is an employee throw error
        if (managerEmail == null)
            if (role == UserRole.EMPLOYEE)
                throw new ManagerRequiredException();
            else // do not validate if the email is empty and it's not an employee
                return;

        User manager = repository.findByEmail(managerEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Manager not found with email: " + managerEmail));

        if (manager.getRole() != UserRole.MANAGER)
            throw new InvalidManagerRoleException();

        // This will only be triggered on UPDATES, because if you try to create the user
        // with his own email as a manager, you will be stopped in manager not found
        if (user.getId() != null && manager.getId().equals(user.getId()))
            throw new SelfManagerException();

        user.setManager(manager);
    }

    /**
     * Creates a new user (used by AuthService during registration).
     */
    public User createUser(UserCreateRequest request) {
        if (repository.existsByEmail(request.getEmail()))
            throw new UserExistsException("The email '" + request.getEmail() + "' is already registered");

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .name(request.getName())
                .build();

        validateAndSetManager(user, request.getManagerEmail(), request.getRole());

        return repository.save(user);
    }

    /**
     * Updates an existing user.
     */
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "There is no user with id " + id));

        if (!user.getEmail().equals(request.getEmail())) {
            throw new IllegalArgumentException("Email cannot be changed");
        }

        if (user.getRole() != request.getRole()) {
            throw new IllegalArgumentException("Role cannot be changed");
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());

        validateAndSetManager(user, request.getManagerEmail(), user.getRole());

        User updatedUser = repository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }

    public List<UserResponse> findAll(UserFilterRequest filters) {
        Specification<User> spec = Specification.where(null);

        spec = spec.and(UserSpecifications.withRole(filters.getRole()));
        spec = spec.and(UserSpecifications.isActive(filters.getIncludeInactive()));

        return repository.findAll(spec).stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public UserResponse findById(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("There is no user with id " + id));
        return UserResponse.fromEntity(user);
    }

    @Transactional
    public void deactivate(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("There is no user with id " + id));

        // Don't allow deactivation if manager has subordinates
        if (user.getRole() == UserRole.MANAGER) {
            boolean hasSubordinates = repository.existsByManagerAndActiveTrue(user);
            if (hasSubordinates)
                throw new ManagerHasSubordinatesException();
        }

        user.setActive(false);
    }

    @Transactional
    public UserResponse reactivate(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("There is no user with id " + id));

        if (user.isActive())
            throw new UserAlreadyActiveException();

        user.setActive(true);
        return UserResponse.fromEntity(user);
    }
}
