package com.ubs.expensemanager.service;

import com.ubs.expensemanager.dto.request.LoginRequest;
import com.ubs.expensemanager.dto.response.LoginResponse;
import com.ubs.expensemanager.exception.InvalidCredentialsException;
import com.ubs.expensemanager.repository.UserMockRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    
    public LoginResponse authenticate(LoginRequest request) {

        UserMockRepository.UserMock user = UserMockRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid Credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid Credentials");
        }

        return new LoginResponse(user.getEmail());
    }
}
