package com.example.finance.controller;

import com.example.finance.dto.TransactionRequest;
import com.example.finance.dto.TransactionResponse;
import com.example.finance.model.User;
import com.example.finance.repository.UserRepository;
import com.example.finance.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionControllerTest {

    @InjectMocks
    private TransactionController transactionController;

    @Mock
    private TransactionService transactionService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    private User mockUser;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser = new User("John Doe", "john@example.com", "encodedPassword");
        today = LocalDate.now();
    }

    @Test
    void testCreateTransaction() {
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("1000.00"),
                "Salary",
                "Income",
                today
        );

        TransactionResponse expectedResponse = new TransactionResponse(
                1L,
                new BigDecimal("1000.00"),
                "Salary",
                "Income",
                today
        );

        when(authentication.getName()).thenReturn(mockUser.getEmail());
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
        when(transactionService.createTransaction(request, mockUser)).thenReturn(expectedResponse);

        ResponseEntity<TransactionResponse> response = transactionController.create(request, authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void testGetAllTransactions() {
        List<TransactionResponse> mockList = List.of(
                new TransactionResponse(
                        1L,
                        new BigDecimal("200.00"),
                        "Food",
                        "Expense",
                        today
                )
        );

        when(authentication.getName()).thenReturn(mockUser.getEmail());
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
        when(transactionService.getAllTransactions(mockUser)).thenReturn(mockList);

        ResponseEntity<List<TransactionResponse>> response = transactionController.getAll(authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(mockList, response.getBody());
    }

    @Test
    void testUpdateTransaction() {
        Long id = 1L;
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("500.00"),
                "Rent",
                "Expense",
                today
        );

        TransactionResponse updatedResponse = new TransactionResponse(
                id,
                new BigDecimal("500.00"),
                "Rent",
                "Expense",
                today
        );

        when(authentication.getName()).thenReturn(mockUser.getEmail());
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
        when(transactionService.updateTransaction(id, request, mockUser)).thenReturn(updatedResponse);

        ResponseEntity<TransactionResponse> response = transactionController.update(id, request, authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(updatedResponse, response.getBody());
    }

    @Test
    void testDeleteTransaction() {
        Long id = 1L;

        TransactionResponse deletedResponse = new TransactionResponse(
                id,
                new BigDecimal("600.00"),
                "Grocery",
                "Expense",
                LocalDate.of(2025, 6, 21)
        );

        when(authentication.getName()).thenReturn(mockUser.getEmail());
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
        when(transactionService.deleteTransaction(id, mockUser)).thenReturn(deletedResponse);

        ResponseEntity<Map<String, Object>> response = transactionController.delete(id, authentication);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Transaction deleted successfully", response.getBody().get("message"));

        @SuppressWarnings("unchecked")
        TransactionResponse responseTxn = (TransactionResponse) response.getBody().get("transaction");
        assertEquals(deletedResponse, responseTxn);
    }

    @Test
    void testDeleteTransaction_UnauthorizedAccess_Throws403() {
        Long id = 1L;

        when(authentication.getName()).thenReturn(mockUser.getEmail());
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
        when(transactionService.deleteTransaction(id, mockUser))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to delete this transaction"));

        ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () ->
                transactionController.delete(id, authentication)
        );

        assertEquals(HttpStatus.FORBIDDEN, thrown.getStatusCode());
        assertEquals("You are not authorized to delete this transaction", thrown.getReason());
    }

    @Test
    void testUpdateTransaction_UnauthorizedAccess_Throws403() {
        Long id = 1L;
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("100.00"),
                "Books",
                "Expense",
                today
        );

        when(authentication.getName()).thenReturn(mockUser.getEmail());
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
        when(transactionService.updateTransaction(id, request, mockUser))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to update this transaction"));

        ResponseStatusException thrown = assertThrows(ResponseStatusException.class, () ->
                transactionController.update(id, request, authentication)
        );

        assertEquals(HttpStatus.FORBIDDEN, thrown.getStatusCode());
        assertEquals("You are not authorized to update this transaction", thrown.getReason());
    }

    @Test
    void testGetBalance() {
        when(authentication.getName()).thenReturn(mockUser.getEmail());
        when(userRepository.findByEmail(mockUser.getEmail())).thenReturn(Optional.of(mockUser));
        when(transactionService.calculateBalance(mockUser)).thenReturn(1500.0);

        ResponseEntity<Double> response = transactionController.getBalance(authentication);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1500.0, response.getBody());
    }
}
