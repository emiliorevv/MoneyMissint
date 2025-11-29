package com.example.moneymissint.DTO;
import com.example.moneymissint.roles.Currency;
import com.example.moneymissint.roles.Status;

public record UserResponse(
        Long userId,
        String name,
        String email,
        Currency currency,
        Status status
    ) {}
