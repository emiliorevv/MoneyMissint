package com.example.moneymissint.service;

import com.example.moneymissint.DTO.TransactionRequest;
import com.example.moneymissint.DTO.TransactionResponse;
import com.example.moneymissint.model.Category;
import com.example.moneymissint.model.Transaction;
import com.example.moneymissint.model.User;
import com.example.moneymissint.repository.CategoryRepository;
import com.example.moneymissint.repository.TransactionRepository;
import com.example.moneymissint.repository.UserRepository;
import com.example.moneymissint.roles.Operation;
import com.example.moneymissint.roles.Status;
import com.example.moneymissint.utils.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
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

    @Mock
    private UserRepository userRepository;



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
    @DisplayName("Create Transaction, it should give error because originUser is the same as destinationUser")
    void createTransaction_ThrowException_SelfTransfer(){
        this.user.setId(1L);

        Long categoryId = 1L;
        Category category = new Category();
        category.setId(categoryId);
        category.setCategoryName("Example Category");
        category.setUser(this.user);

        when(categoryRepository.findById(categoryId)).thenReturn(java.util.Optional.of(category));


        TransactionRequest transactionRequest = new TransactionRequest(new  BigDecimal("1000.00"), Operation.INCOME, categoryId, 1L);

        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(this.user));

        assertThatThrownBy(() -> transactionService.createTransaction(transactionRequest)).isInstanceOf(IllegalArgumentException.class).hasMessage("You cant make transactions to your same account!");

        verify(transactionRepository, never()).save(any(Transaction.class));

    }

    @Test
    @DisplayName("Create transaction, it should give error because the operation is null")
    void createTransaction_ThrowException_NullOperation(){
        this.user.setId(1L);
        Long categoryId = 1L;
        Category category = new Category();
        category.setId(categoryId);
        category.setCategoryName("Example Category");
        category.setUser(this.user);

        when(categoryRepository.findById(categoryId)).thenReturn(java.util.Optional.of(category));

        TransactionRequest transactionRequest = new  TransactionRequest(new  BigDecimal("1000.00"), null, categoryId, null);


        assertThatThrownBy(() -> transactionService.createTransaction(transactionRequest)).isInstanceOf(IllegalArgumentException.class).hasMessage("Operation is null");

        verify(transactionRepository, never()).save(any(Transaction.class));


    }

    @Test
    @DisplayName("Create transaction, it should give error because the category is null")
    void createTransaction_ThrowException_CategoryNotFound(){
        Long categoryId = 99L;

        this.user.setId(1L);
        TransactionRequest transactionRequest = new TransactionRequest(new  BigDecimal("1000.00"), Operation.INCOME, categoryId, null);

        when(categoryRepository.findById(categoryId)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> transactionService.createTransaction(transactionRequest)).isInstanceOf(EntityNotFoundException.class).hasMessage("Category not found");

        verify(transactionRepository, never()).save(any(Transaction.class));

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

    @Test
    @DisplayName("Delete operation test, it should give error because the operation was not found")
    void deleteOperation_ThrowException_OperationNotFound(){
        Long transactionId = 99L;
        when(transactionRepository.findById(transactionId)).thenReturn(java.util    .Optional.empty());
        assertThatThrownBy(() -> transactionService.deleteOperation(transactionId)).isInstanceOf(EntityNotFoundException.class).hasMessage("Transaction not found");
        verify(transactionRepository, never()).delete(any(Transaction.class));
    }

    @Test
    @DisplayName("Delete operation test, it should give error because the operation is not from the originUser")
    void deleteOperation_ThrowException_NotOwner(){
        user.setId(1L);

        User otherUser = new User();
        otherUser.setId(2L);
        Long transactionId = 1L;
        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setOriginUser(otherUser);

        when(transactionRepository.findById(transactionId)).thenReturn(java.util.Optional.of(transaction));
        assertThatThrownBy(() -> transactionService.deleteOperation(transactionId)).isInstanceOf(EntityNotFoundException.class).hasMessage("Transaction not found");
        verify(transactionRepository, never()).delete(any(Transaction.class));

    }


    @Test
    @DisplayName("Update Operation Category, it should update the category of the operation correctly")
    void updateOperationCategory(){
        Long transactionId = 1L;
        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setOriginUser(this.user);
        transaction.setOperation(Operation.INCOME);
        transaction.setTransactionAmount(new BigDecimal("1000.00"));
        transaction.setDestinationUser(null);

        Long categoryId = 1L;
        Category oldCategory = new Category();
        transaction.setCategory(oldCategory);

        oldCategory.setId(categoryId);
        oldCategory.setCategoryName("Example Category");
        oldCategory.setUser(this.user);



        when(transactionRepository.findById(transactionId)).thenReturn(java.util.Optional.of(transaction));


        Long newCategoryId = 2L;
        Category newCategory = new Category();
        newCategory.setId(newCategoryId);
        newCategory.setCategoryName("New Example Category");
        newCategory.setUser(this.user);

        when(categoryRepository.findById(newCategoryId)).thenReturn(java.util.Optional.of(newCategory));

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        TransactionResponse transactionResponse = transactionService.updateTransactionCategory(transactionId, newCategoryId);


        assertThat(transactionResponse.categoryId()).isEqualTo(newCategoryId);
        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    @DisplayName("Update Transaction Category, it should give an error because the category was not found")
    void updateOperationCategory_ThrowException_CategoryNotFound(){
        Long transactionId = 1L;
        Long categoryId = 99L;
        when(categoryRepository.findById(categoryId)).thenReturn(java.util.Optional.empty());
        assertThatThrownBy(() -> transactionService.updateTransactionCategory(transactionId, categoryId)).isInstanceOf(EntityNotFoundException.class).hasMessage("Category not found");
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Update Transaction Category, it should give an error because the operation is not from the originUser")
    void updateOperationCategory_ThrowException_NotOwner(){
        user.setId(1L);
        Long categoryId = 1L;
        Category category = new Category();
         category.setCategoryName("Example Category");
         category.setUser(this.user);
        User otherUser = new User();
        otherUser.setId(2L);
        Long transactionId = 1L;
        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setOriginUser(otherUser);
        when(transactionRepository.findById(transactionId)).thenReturn(java.util.Optional.of(transaction));
        when(categoryRepository.findById(1L)).thenReturn(java.util.Optional.of(category));
        assertThatThrownBy(() -> transactionService.updateTransactionCategory(transactionId, categoryId)).isInstanceOf(EntityNotFoundException.class).hasMessage("The transaction was not found");
        verify(transactionRepository, never()).save(any(Transaction.class));
    }


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
    @DisplayName("Get operation by id test, it should give error because the operation was not found")
    void getOperationById_ThrowException_OperationNotFound(){
        Long transactionId = 99L;
        when(transactionRepository.findById(transactionId)).thenReturn(java.util.Optional.empty());
        assertThatThrownBy(() -> transactionService.getTransactionById(transactionId)).isInstanceOf(EntityNotFoundException.class).hasMessage("Transaction not found");
        verify(transactionRepository, times(1)).findById(transactionId);
    }

    @Test
    @DisplayName("Get operation by id test, it should give error because the operation is not from the originUser")
    void getOperationById_ThrowException_NotOwner(){
        user.setId(1L);
        User otherUser = new User();
        otherUser.setId(2L);
        Long transactionId = 1L;
        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setOriginUser(otherUser);
        when(transactionRepository.findById(transactionId)).thenReturn(java.util.Optional.of(transaction));
        assertThatThrownBy(() -> transactionService.getTransactionById(transactionId)).isInstanceOf(EntityNotFoundException.class).hasMessage("Transaction not found");
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

    @Test
    @DisplayName("Calculate Monthly Income, it should return zero if there were no income received on that month")
    void calculateMonthlyIncome_Zero(){
        this.user.setId(1L);
        when(transactionRepository.monthlyIncome(eq(user), any(), any(), eq(Operation.INCOME))).thenReturn(null);
        BigDecimal income = transactionService.calculateMonthlyIncome();
        assertThat(income).isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(income).isNotNull();


    }

    @Test
    @DisplayName("Calculate Monthly Transactions Received, it should return zero if there were no transactions received on that month")
    void calculateMonthlyTransferReceived_Zero(){
        this.user.setId(1L);
        when(transactionRepository.monthlyTransferReceived(eq(user), any(), any(), eq(Operation.TRANSFER))).thenReturn(null);
        BigDecimal transfersRecieved = transactionService.calculateMonthlyTransfersRecieved();
        assertThat(transfersRecieved).isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(transfersRecieved).isNotNull();
    }

    @Test
    @DisplayName("Calculate Monthly Expenses, it should return zero if there were no expenses on that month")
    void calculateMonthlyExpense_Zero(){
        this.user.setId(1L);
        when(transactionRepository.monthlyExpense(eq(user), any(), any(), eq(Operation.EXPENSE))).thenReturn(null);
        BigDecimal expense = transactionService.calculateMonthlyExpenses();
        assertThat(expense).isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(expense).isNotNull();

    }

    @Test
    @DisplayName("Calculate Monthly Transactions Done, it should return zero if there were no transactions done on that month")
    void calculateMonthlyTransfer_Zero(){
        this.user.setId(1L);
        when(transactionRepository.monthlyTransfer(eq(user), any(), any(), eq(Operation.TRANSFER))).thenReturn(null);
        BigDecimal transfersDone = transactionService.calculateMonthlyTransfers();
        assertThat(transfersDone).isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(transfersDone).isNotNull();
    }


}
