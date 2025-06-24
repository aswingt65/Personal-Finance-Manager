package com.example.finance.service;

import com.example.finance.dto.TransactionRequest;
import com.example.finance.dto.TransactionResponse;
import com.example.finance.model.Transaction;
import com.example.finance.model.User;
import com.example.finance.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.FORBIDDEN;

class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private TransactionRepository transactionRepository;

    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser = new User("John Doe", "john@example.com", "encodedPassword");
    }

    @Test
    void testCreateTransaction() {
        TransactionRequest request = new TransactionRequest(
                new BigDecimal("1000.00"),
                "Salary",
                "Income",
                LocalDate.now()
        );

        Transaction mockTxn = mock(Transaction.class);
        when(mockTxn.getId()).thenReturn(1L);
        when(mockTxn.getAmount()).thenReturn(request.amount());
        when(mockTxn.getDescription()).thenReturn(request.description());
        when(mockTxn.getCategory()).thenReturn(request.category());
        when(mockTxn.getDate()).thenReturn(request.date());

        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTxn);

        TransactionResponse response = transactionService.createTransaction(request, mockUser);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Salary", response.description());
    }

    @Test
    void testGetAllTransactions() {
        Transaction txn = new Transaction(
                new BigDecimal("100.00"),
                "Groceries",
                "Expense",
                LocalDate.now(),
                mockUser
        );
        txn.setId(1L);

        when(transactionRepository.findByUser(mockUser)).thenReturn(List.of(txn));

        List<TransactionResponse> responses = transactionService.getAllTransactions(mockUser);

        assertEquals(1, responses.size());
        assertEquals("Groceries", responses.get(0).description());
    }

    @Test
    void testUpdateTransaction_success() {
        Long id = 1L;

        TransactionRequest request = new TransactionRequest(
                new BigDecimal("500.00"),
                "Updated Rent",
                "Expense",
                LocalDate.now()
        );

        Transaction existingTxn = new Transaction(
                new BigDecimal("100.00"),
                "Old Desc",
                "Income",
                LocalDate.now(),
                mockUser
        );
        existingTxn.setId(id);

        when(transactionRepository.findById(id)).thenReturn(Optional.of(existingTxn));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        TransactionResponse response = transactionService.updateTransaction(id, request, mockUser);

        assertNotNull(response);
        assertEquals("Updated Rent", response.description());
        assertEquals(new BigDecimal("500.00"), response.amount());
    }

    @Test
    void testUpdateTransaction_unauthorized_throwsForbidden() {
        Long id = 2L;

        User otherUser = new User("Eve", "eve@example.com", "pass");
        Transaction otherTxn = new Transaction(
                new BigDecimal("999.00"),
                "Secret",
                "Income",
                LocalDate.now(),
                otherUser
        );
        otherTxn.setId(id);

        TransactionRequest request = new TransactionRequest(
                new BigDecimal("100.00"),
                "Malicious update",
                "Expense",
                LocalDate.now()
        );

        when(transactionRepository.findById(id)).thenReturn(Optional.of(otherTxn));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                transactionService.updateTransaction(id, request, mockUser)
        );

        assertEquals(FORBIDDEN, ex.getStatusCode());
        assertEquals("You are not authorized to update this transaction", ex.getReason());
    }

    @Test
    void testDeleteTransaction_returnsDeletedTransaction() {
        Transaction txn = new Transaction();
        txn.setAmount(new BigDecimal("1000.00"));
        txn.setDescription("Old Rent");
        txn.setCategory("Expense");
        txn.setDate(LocalDate.of(2025, 6, 22));
        txn.setUser(mockUser);
        txn.setId(1L);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(txn));

        TransactionResponse result = transactionService.deleteTransaction(1L, mockUser);

        verify(transactionRepository).delete(txn);
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Old Rent", result.description());
        assertEquals(new BigDecimal("1000.00"), result.amount());
        assertEquals("Expense", result.category());
        assertEquals(LocalDate.of(2025, 6, 22), result.date());
    }

    @Test
    void testDeleteTransaction_unauthorized_throwsForbidden() {
        Long id = 3L;

        User otherUser = new User("Hacker", "hacker@example.com", "bad");
        Transaction otherTxn = new Transaction(
                new BigDecimal("1.00"),
                "Steal",
                "Expense",
                LocalDate.now(),
                otherUser
        );
        otherTxn.setId(id);

        when(transactionRepository.findById(id)).thenReturn(Optional.of(otherTxn));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                transactionService.deleteTransaction(id, mockUser)
        );

        assertEquals(FORBIDDEN, ex.getStatusCode());
        assertEquals("You are not authorized to delete this transaction", ex.getReason());
        verify(transactionRepository, never()).delete(any());
    }

    @Test
    void testCalculateBalance() {
        List<Transaction> txns = List.of(
                new Transaction(new BigDecimal("1000.00"), "Salary", "Income", LocalDate.now(), mockUser),
                new Transaction(new BigDecimal("200.00"), "Groceries", "Expense", LocalDate.now(), mockUser),
                new Transaction(new BigDecimal("100.00"), "Dining", "Expense", LocalDate.now(), mockUser)
        );

        when(transactionRepository.findByUser(mockUser)).thenReturn(txns);

        Double balance = transactionService.calculateBalance(mockUser);

        assertEquals(700.00, balance);
    }
}
