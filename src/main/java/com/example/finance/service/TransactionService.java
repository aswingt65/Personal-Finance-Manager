package com.example.finance.service;

import com.example.finance.dto.TransactionRequest;
import com.example.finance.dto.TransactionResponse;
import com.example.finance.model.Transaction;
import com.example.finance.model.User;
import com.example.finance.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public TransactionResponse createTransaction(TransactionRequest request, User user) {
        Transaction txn = new Transaction(
                request.amount(),
                request.description(),
                request.category(),
                request.date(),
                user
        );
        return mapToResponse(transactionRepository.save(txn));
    }

    public List<TransactionResponse> getAllTransactions(User user) {
        return transactionRepository.findByUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TransactionResponse updateTransaction(Long id, TransactionRequest request, User user) {
        Transaction txn = transactionRepository.findById(id)
                .filter(t -> t.getUser().equals(user))
                .orElseThrow();

        txn.setAmount(request.amount());
        txn.setDescription(request.description());
        txn.setCategory(request.category());
        txn.setDate(request.date());
        return mapToResponse(transactionRepository.save(txn));
    }

    public void deleteTransaction(Long id, User user) {
        Transaction txn = transactionRepository.findById(id)
                .filter(t -> t.getUser().equals(user))
                .orElseThrow();
        transactionRepository.delete(txn);
    }

    public Double calculateBalance(User user) {
        return transactionRepository.findByUser(user).stream()
                .mapToDouble(txn -> txn.getCategory().equalsIgnoreCase("Income") ?
                        txn.getAmount().doubleValue() : -txn.getAmount().doubleValue())
                .sum();
    }

    private TransactionResponse mapToResponse(Transaction txn) {
        return new TransactionResponse(
                txn.getId(),
                txn.getAmount(),
                txn.getDescription(),
                txn.getCategory(),
                txn.getDate()
        );
    }
}
