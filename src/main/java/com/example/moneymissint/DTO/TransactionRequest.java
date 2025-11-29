package com.example.moneymissint.DTO;

import com.example.moneymissint.roles.Operation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotBlank(message = "please add a valid amount") @Min(value = 1, message = "please enter a valid amount") @Max(value = 10000000, message = "please enter a valid amount") BigDecimal amount,
        @NotNull(message = "operation cannot be null") Operation operation,
        @NotBlank(message = "categoryId cannot be in blank") Long categoryId,
        Long destinationUserId
) {
}
