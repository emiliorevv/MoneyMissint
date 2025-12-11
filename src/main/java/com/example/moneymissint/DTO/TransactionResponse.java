package com.example.moneymissint.DTO;

import com.example.moneymissint.roles.Operation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long transactionId,
        Operation operation,
        BigDecimal amount,
        Long categoryId,
        Long originUserId,
        Long destinationUserId,
        LocalDateTime transactionTime
) {
}
