package com.example.moneymissint.DTO;
import com.example.moneymissint.roles.Currency;
import com.example.moneymissint.roles.Status;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequest(

        @NotBlank(message = "Name cannot be empty") String name,
        @NotBlank(message = "Email cannot be empty") @Email(message = "Invalid format") String email,
        @Size(min = 6, max = 60) @NotBlank(message = "Password cannot be empty") String password,
        @NotNull(message = "Currency cannot be empty") Currency currency,
        @NotNull(message = "Status cannot be empty") Status status) {

}
