package com.example.finance.controller;

import com.example.finance.dto.AuthResponse;
import com.example.finance.dto.LoginRequest;
import com.example.finance.dto.RegisterRequest;
import com.example.finance.model.User;
import com.example.finance.repository.UserRepository;
import com.example.finance.security.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("Password@123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");

        ResponseEntity<AuthResponse> response = authController.register(request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("User registered successfully", response.getBody().message());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("Password@123");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(new User()));

        ResponseEntity<AuthResponse> response = authController.register(request);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Email already in use", response.getBody().message());
        assertNull(response.getBody().token());
    }

    @Test
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest("john@example.com", "Password@123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new TestingAuthenticationToken("john@example.com", "Password@123"));

        when(jwtTokenUtil.generateToken("john@example.com")).thenReturn("dummyToken");

        ResponseEntity<AuthResponse> response = authController.login(request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("dummyToken", response.getBody().token());
        assertEquals("Login successful", response.getBody().message());
    }

    @Test
    void testLoginInvalidCredentials() {
        LoginRequest request = new LoginRequest("john@example.com", "wrongPassword");

        doThrow(new BadCredentialsException("Bad credentials")).when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        ResponseEntity<AuthResponse> response = authController.login(request);

        assertEquals(401, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Invalid email or password", response.getBody().message());
        assertNull(response.getBody().token());
    }
}
