package com.ubs.expensemanager.service;

import com.ubs.expensemanager.dto.request.UserCreateRequest;
import com.ubs.expensemanager.dto.request.UserFilterRequest;
import com.ubs.expensemanager.dto.request.UserUpdateRequest;
import com.ubs.expensemanager.dto.response.UserResponse;
import com.ubs.expensemanager.exception.ManagerRequiredException;
import com.ubs.expensemanager.exception.ResourceNotFoundException;
import com.ubs.expensemanager.mapper.UserMapper;
import com.ubs.expensemanager.model.Department;
import com.ubs.expensemanager.model.User;
import com.ubs.expensemanager.model.UserRole;
import com.ubs.expensemanager.repository.DepartmentRepository;
import com.ubs.expensemanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository repository;

    @Mock
    DepartmentRepository departmentRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    UserMapper userMapper;

    @InjectMocks
    UserService userService;

    Department itDepartment;
    User manager;
    User employee;

    @BeforeEach
    void setUp() {
        itDepartment = Department.builder()
                .id(1L)
                .name("IT")
                .monthlyBudget(new BigDecimal("50000.00"))
                .dailyBudget(new BigDecimal("2000.00"))
                .currency("USD")
                .build();

        manager = User.builder()
                .id(1L)
                .email("manager@ubs.com")
                .role(UserRole.MANAGER)
                .name("Manager")
                .department(itDepartment)
                .build();

        employee = User.builder()
                .id(2L)
                .email("employee@ubs.com")
                .role(UserRole.EMPLOYEE)
                .name("Employee")
                .manager(manager)
                .department(itDepartment)
                .build();

        // Setup default mapper behavior (lenient because not all tests use it)
        lenient().when(userMapper.toResponse(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            return UserResponse.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .role("ROLE_" + user.getRole().name())
                    .active(user.isActive())
                    .manager(user.getManager() != null ? UserResponse.ManagerInfo.builder()
                            .id(user.getManager().getId())
                            .email(user.getManager().getEmail())
                            .name(user.getManager().getName())
                            .build() : null)
                    .build();
        });
    }

    @Test
    void createUser_success_employeeWithManager() {
        UserCreateRequest request = UserCreateRequest.builder()
                .email("employee@ubs.com")
                .password("123")
                .name("Employee")
                .role(UserRole.EMPLOYEE)
                .departmentId(1L)
                .managerEmail("manager@ubs.com")
                .build();

        when(repository.existsByEmail(request.getEmail())).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(itDepartment));
        when(repository.findByEmail("manager@ubs.com")).thenReturn(Optional.of(manager));
        when(passwordEncoder.encode("123")).thenReturn("hashed");
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.createUser(request);

        assertEquals(UserRole.EMPLOYEE, saved.getRole());
        assertEquals(manager, saved.getManager());
        assertEquals(itDepartment, saved.getDepartment());
        assertEquals("hashed", saved.getPassword());
    }

    @Test
    void createUser_employeeWithoutManager_throwsException() {
        UserCreateRequest request = UserCreateRequest.builder()
                .email("employee@ubs.com")
                .password("123")
                .role(UserRole.EMPLOYEE)
                .departmentId(1L)
                .build();

        when(repository.existsByEmail(request.getEmail())).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(itDepartment));

        assertThrows(
                ManagerRequiredException.class,
                () -> userService.createUser(request)
        );
    }

    @Test
    void updateUser_success_nameAndManager() {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("employee@ubs.com")      // obrigatório
                .password("123456")             // obrigatório
                .role(UserRole.EMPLOYEE)        // obrigatório
                .name("Employee Updated")
                .departmentId(1L)
                .managerEmail("manager@ubs.com") // pode mudar
                .build();

        when(repository.findById(2L)).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(itDepartment));
        when(repository.findByEmail("manager@ubs.com")).thenReturn(Optional.of(manager));
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.update(2L, request);

        assertEquals("Employee Updated", response.getName());
        assertEquals("manager@ubs.com", response.getManager().getEmail());
    }


    @Test
    void updateUser_changeEmail_throwsException() {
        UserUpdateRequest request = UserUpdateRequest.builder()
                .email("other@ubs.com")          // diferente do atual
                .password("123456")
                .role(UserRole.EMPLOYEE)
                .name("Employee")
                .departmentId(1L)
                .managerEmail("manager@ubs.com")
                .build();

        when(repository.findById(2L)).thenReturn(Optional.of(employee));

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.update(2L, request)
        );
    }

    @Test
    void findById_notFound_throwsException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> userService.findById(99L)
        );
    }

    @Test
    void findAll_withoutFilters_callsRepositoryWithSpecification() {
        UserFilterRequest filters = new UserFilterRequest();

        Page<User> page = new PageImpl<>(List.of(employee));

        when(repository.findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        Page<UserResponse> result =
                userService.findAll(filters, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());

        verify(repository).findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(Pageable.class)
        );
    }


    @Test
    void findAll_withRoleFilter_callsRepositoryWithSpecification() {
        UserFilterRequest filters = new UserFilterRequest();
        filters.setRole(UserRole.EMPLOYEE);

        Page<User> page = new PageImpl<>(List.of(employee));

        when(repository.findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        Page<UserResponse> result =
                userService.findAll(filters, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
        assertEquals("ROLE_" + UserRole.EMPLOYEE.name(), result.getContent().get(0).getRole());

        verify(repository).findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(Pageable.class)
        );
    }

    @Test
    void findAll_excludingInactiveUsers_callsRepository() {
        UserFilterRequest filters = new UserFilterRequest();
        filters.setIncludeInactive(false);

        Page<User> page = new PageImpl<>(List.of(employee));

        when(repository.findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        Page<UserResponse> result =
                userService.findAll(filters, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(repository).findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(Pageable.class)
        );
    }

    @Test
    void deactivateUser_success() {
        when(repository.findById(2L)).thenReturn(Optional.of(employee));

        userService.deactivate(2L);

        assertFalse(employee.isActive());
    }
}