package com.example.moneymissint.service;

import com.example.moneymissint.DTO.TransactionRequest;
import com.example.moneymissint.DTO.TransactionResponse;
import com.example.moneymissint.model.Category;
import com.example.moneymissint.model.Transaction;
import com.example.moneymissint.model.User;
import com.example.moneymissint.repository.CategoryRepository;
import com.example.moneymissint.repository.TransactionRepository;
import com.example.moneymissint.roles.Operation;
import com.example.moneymissint.roles.Status;
import com.example.moneymissint.utils.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private CategoryRepository categoryRepository;



    @InjectMocks
    private TransactionService transactionService;

    private User user;


    @BeforeEach
    void setUp() {
        this.user = SecurityUtils.mockedLoginUser(Status.ACTIVE, securityContext, authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Create transaction test, it should create a transaction without problems")
    void createTransaction(){
        this.user.setId(1L);

        Long categoryId = 1L;
        Category category = new Category();
        category.setId(categoryId);
        category.setCategoryName("Example Category");
        category.setUser(this.user);

        TransactionRequest transactionRequest = new TransactionRequest(new  BigDecimal("1000.00"), Operation.INCOME, categoryId, null);

        Transaction newTransaction = new Transaction();
        newTransaction.setId(1L);
        newTransaction.setTransactionAmount(transactionRequest.amount());
        newTransaction.setOperation(transactionRequest.operation());
        newTransaction.setTransactionTime(LocalDateTime.now());
        newTransaction.setCategory(category);
        newTransaction.setOriginUser(this.user);
        newTransaction.setDestinationUser(null);

        when(categoryRepository.findById(categoryId)).thenReturn(java.util.Optional.of(category));

        when(transactionRepository.save(any(Transaction.class))).thenReturn(newTransaction);

        TransactionResponse transactionResponse = transactionService.createTransaction(transactionRequest);

        assertThat(transactionResponse).isNotNull();
        assertThat(transactionResponse.transactionId()).isNotNull();
        assertThat(transactionResponse.amount()).isEqualTo(transactionRequest.amount());
        assertThat(transactionResponse.operation()).isEqualTo(transactionRequest.operation());
        assertThat(transactionResponse.categoryId()).isEqualTo(categoryId);

        verify(transactionRepository, times(1)).save(any(Transaction.class));


    }


    @Test
    @DisplayName("Delete operation test, it should delete the operation correctly")
    void deleteOperation(){
        Long transactionId = 1L;
        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setOriginUser(this.user);

        when(transactionRepository.findById(transactionId)).thenReturn(java.util.Optional.of(transaction));
        transactionService.deleteOperation(transactionId);



        verify(transactionRepository, times(1)).delete(transaction);



    }


    void updateOperation(){}


    @Test
    @DisplayName("Get operation by id test, it should return the operation correctly")
    void getOperationById(){



        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setTransactionAmount(new BigDecimal("1000.00"));
        transaction.setOperation(Operation.INCOME);
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setCategory(new Category());
        transaction.setOriginUser(this.user);
        transaction.setDestinationUser(null);


        Long transactionId = 1L;
        when(transactionRepository.findById(transactionId)).thenReturn(java.util.Optional.of(transaction));


        TransactionResponse transactionResponse = transactionService.getTransactionById(transactionId);
        assertThat(transactionResponse).isNotNull();
        assertThat(transactionResponse.amount()).isEqualTo(transaction.getTransactionAmount());
        assertThat(transactionResponse.operation()).isEqualTo(transaction.getOperation());

        verify(transactionRepository, times(1)).findById(transactionId);

    }

    @Test
    @DisplayName("Calculate balance test, it should calculate the balance correctly")
    void calculateBalance(){

        BigDecimal income = new BigDecimal("1000.00");
        BigDecimal transactionReceived = new BigDecimal("500.00");
        BigDecimal expense = new BigDecimal("150.00");
        BigDecimal transactionDone = new BigDecimal("200.00");

        BigDecimal expectedBalance = new BigDecimal("1150.00");

        when(transactionRepository.monthlyIncome(eq(user), any(), any(), eq(Operation.INCOME))).thenReturn(income);

        when(transactionRepository.monthlyTransferReceived(eq(user), any(), any(), eq(Operation.TRANSFER))).thenReturn(transactionReceived);

        when(transactionRepository.monthlyExpense(eq(user), any(), any(), eq(Operation.EXPENSE))).thenReturn(expense);

        when(transactionRepository.monthlyTransfer(eq(user), any(), any(), eq(Operation.TRANSFER))).thenReturn(transactionDone);

        BigDecimal balance = transactionService.balanceOfTheMonth();

        assertThat(balance).isEqualByComparingTo(expectedBalance);

        assertThat(balance).isNotNull();

        verify(transactionRepository).monthlyIncome(eq(user), any(), any(), eq(Operation.INCOME));
        verify(transactionRepository).monthlyTransferReceived(eq(user), any(), any(), eq(Operation.TRANSFER));
        verify(transactionRepository).monthlyExpense(eq(user), any(), any(), eq(Operation.EXPENSE));
        verify(transactionRepository).monthlyTransfer(eq(user), any(), any(), eq(Operation.TRANSFER));
    }


}
