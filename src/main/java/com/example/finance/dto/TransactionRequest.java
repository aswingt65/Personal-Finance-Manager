package com.example.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        BigDecimal amount,
        String description,
        String category,
        LocalDate date
) {}
