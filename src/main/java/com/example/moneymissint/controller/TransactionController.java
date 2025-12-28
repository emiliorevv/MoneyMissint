package com.example.moneymissint.controller;


import com.example.moneymissint.DTO.TransactionRequest;
import com.example.moneymissint.DTO.TransactionResponse;
import com.example.moneymissint.roles.Operation;
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


    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction (@RequestBody @Valid TransactionRequest transactionRequest){
        TransactionResponse transactionResponse = transactionService.createTransaction(transactionRequest);
        return ResponseEntity.status((HttpStatus.CREATED)).body(transactionResponse);

    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransaction (  @PathVariable Long transactionId){
        transactionService.deleteOperation(transactionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/monthly-stats")
    public ResponseEntity<BigDecimal> getMonthlyStatistics(@RequestParam Operation operation){

        return switch (operation) {
            case INCOME -> {
                BigDecimal monthlyIncome = transactionService.calculateMonthlyIncome();
                yield ResponseEntity.status(HttpStatus.OK).body(monthlyIncome);
            }
            case EXPENSE -> {
                BigDecimal monthlyExpenses = transactionService.calculateMonthlyExpenses();
                yield ResponseEntity.status(HttpStatus.OK).body(monthlyExpenses);
            }

            case TRANSFER -> {
                BigDecimal monthlyTransfer = transactionService.calculateMonthlyTransfers();
                yield ResponseEntity.status(HttpStatus.OK).body(monthlyTransfer);
            }
        };
    }

    @GetMapping("/balance}")
    public ResponseEntity<BigDecimal> getBalance(){
        BigDecimal balance = transactionService.balanceOfTheMonth();
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
