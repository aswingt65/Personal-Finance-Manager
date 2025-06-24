package com.example.finance.controller;

import com.example.finance.dto.TransactionRequest;
import com.example.finance.dto.TransactionResponse;
import com.example.finance.model.User;
import com.example.finance.service.TransactionService;
import com.example.finance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@RequestBody TransactionRequest request, Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        return ResponseEntity.ok(transactionService.createTransaction(request, user));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAll(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        return ResponseEntity.ok(transactionService.getAllTransactions(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(@PathVariable Long id,
                                                      @RequestBody TransactionRequest request,
                                                      Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        return ResponseEntity.ok(transactionService.updateTransaction(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id, Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        TransactionResponse deletedTransaction = transactionService.deleteTransaction(id, user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Transaction deleted successfully");
        response.put("transaction", deletedTransaction);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/balance")
    public ResponseEntity<Double> getBalance(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName()).orElseThrow();
        return ResponseEntity.ok(transactionService.calculateBalance(user));
    }
}
