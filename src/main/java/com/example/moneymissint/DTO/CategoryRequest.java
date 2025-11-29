package com.example.moneymissint.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryRequest(

        @NotBlank(message = "Name cannot be empty") String nameOfCategory
        ) {}

