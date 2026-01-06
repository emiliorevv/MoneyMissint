package com.example.moneymissint.controller;
import com.example.moneymissint.DTO.TransactionRequest;
import com.example.moneymissint.DTO.TransactionResponse;

import com.example.moneymissint.roles.Operation;
import com.example.moneymissint.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("Create Transaction Test, it should return created when the request is valid")
    void createTransaction_ShouldReturnCreated_WhenRequestIsValid() throws Exception {
        final BigDecimal amount = BigDecimal.valueOf(1000.00);
        final Operation operation = Operation.INCOME;
        final Long categoryId = 1L;

        final TransactionRequest transactionRequest = new TransactionRequest(amount, operation, categoryId, null);


        TransactionResponse mockedResponse = new TransactionResponse(1L, operation, amount, categoryId, 1L, null, LocalDateTime.now());

        when(transactionService.createTransaction(any(TransactionRequest.class))).thenReturn(mockedResponse);

        mockMvc.perform(post("/api/v1/transactions").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(transactionRequest))).andExpect(status().isCreated()).andExpect(jsonPath("$.transactionId").value(1L)).andExpect(jsonPath("$.amount").value(1000.00));


    }

    @Test
    @DisplayName("Create transaction test, it should return error because the token is expired")
    void createTransaction_ShouldReturnError_WhenTokenIsExpired() throws Exception {
        final BigDecimal amount = BigDecimal.valueOf(1000.00);
        final Operation operation = Operation.INCOME;
        final Long categoryId = 1L;

        final TransactionRequest transactionRequest = new TransactionRequest(amount, operation, categoryId, null);


    }
}
