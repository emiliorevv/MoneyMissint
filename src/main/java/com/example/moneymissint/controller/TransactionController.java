package com.example.moneymissint.controller;


import com.example.moneymissint.DTO.TransactionRequest;
import com.example.moneymissint.DTO.TransactionResponse;
import com.example.moneymissint.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RequiredArgsConstructor
@RestController
@Validated

@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;


    @PostMapping("/create/{originUserId}")
    public ResponseEntity<TransactionResponse> createTransaction (@RequestBody @Valid TransactionRequest transactionRequest, @PathVariable Long originUserId){
        TransactionResponse transactionResponse = transactionService.createTransaction(transactionRequest, originUserId);
        return ResponseEntity.status((HttpStatus.CREATED)).body(transactionResponse);

    }

    @DeleteMapping("/delete/{transactionId}")
    public ResponseEntity<Void> deleteTransaction (  @PathVariable Long transactionId){
        transactionService.deleteOperation(transactionId);
        return ResponseEntity.status((HttpStatus.NO_CONTENT)).body(null);
    }

    @GetMapping("/monthly-income/{userId}")
    public ResponseEntity<BigDecimal> getMonthlyIncome (@PathVariable Long userId){
        BigDecimal monthlyIncome = transactionService.calculateMonthlyIncome(transactionService.validateUsers(userId));
        return ResponseEntity.status(HttpStatus.OK).body(monthlyIncome);

    }

    @GetMapping("/monthly-expenses/{userId}")
    public ResponseEntity<BigDecimal> getMonthlyExpenses (@PathVariable Long userId) {
        BigDecimal monthlyExpenses = transactionService.calculateMonthlyExpenses(transactionService.validateUsers(userId));
        return ResponseEntity.status(HttpStatus.OK).body(monthlyExpenses);
    }

    @GetMapping("/balance/{userId}")
    public ResponseEntity<BigDecimal> getBalance (@PathVariable Long userId) {
        BigDecimal balance = transactionService.balance(transactionService.validateUsers(userId));
        return ResponseEntity.status(HttpStatus.OK).body(balance);
    }

    @PutMapping("/update-category/{transactionId}/{categoryId}")
    public ResponseEntity<TransactionResponse> updateTransactionCategory(@PathVariable Long transactionId, @PathVariable Long categoryId){
        TransactionResponse transactionResponse = transactionService.updateTransactionCategory(transactionId, categoryId);
        return ResponseEntity.status(HttpStatus.OK).body(transactionResponse);
    }

    @GetMapping("{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionById (@PathVariable Long transactionId){
        TransactionResponse transactionResponse = transactionService.getTransactionById(transactionId);
        return ResponseEntity.status(HttpStatus.OK).body(transactionResponse);
    }

}
