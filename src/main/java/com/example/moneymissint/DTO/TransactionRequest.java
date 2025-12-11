package com.example.moneymissint.DTO;

import com.example.moneymissint.roles.Operation;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotNull(message = "please add a valid amount")
        @DecimalMin(value = "0.01", message = "please enter a valid amount, amount is too small")
        @DecimalMax(value = "10000000.0", message = "please enter a valid amount, amount is too large")
        BigDecimal amount,


        @NotNull(message = "operation cannot be null")
        Operation operation,


        @NotNull(message = "categoryId cannot be in blank")
        Long categoryId,


        Long destinationUserId
) {
}
