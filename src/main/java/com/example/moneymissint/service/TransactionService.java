package com.example.moneymissint.service;
import com.example.moneymissint.DTO.TransactionRequest;
import com.example.moneymissint.DTO.TransactionResponse;
import com.example.moneymissint.model.Transaction;
import com.example.moneymissint.model.User;
import com.example.moneymissint.repository.CategoryRepository;
import com.example.moneymissint.repository.TransactionRepository;
import com.example.moneymissint.repository.UserRepository;
import com.example.moneymissint.roles.Operation;
import com.example.moneymissint.roles.Status;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;

    private final UserRepository userRepository;

    private final CategoryRepository categoryRepository;



    private Transaction getTransactionOrThrow(Long transactionId) {
        return transactionRepository.findById(transactionId).orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

    }

    public User validateUsers(Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (user.getStatus() == Status.INACTIVE) {
            throw new IllegalStateException("User is Inactive");
        }

        return user;

    }

    public void validateOperation(Transaction transaction) {
        if (transaction.getOriginUser() == null){
            throw new IllegalArgumentException("Origin User is null");
        }

        if (transaction.getOperation() == null){
            throw new IllegalArgumentException("Operation is null");
        }

        if (transaction.getCategory() == null){
            throw new IllegalArgumentException("Category is null");
        }

    }




    public TransactionResponse createTransaction(TransactionRequest transactionRequest, Long originUserId) {
        Transaction transaction = new Transaction();
        transaction.setOperation(transactionRequest.operation());
        transaction.setTransactionAmount(transactionRequest.amount());
        transaction.setCategory(categoryRepository.findById(transactionRequest.categoryId()).orElseThrow(()-> new EntityNotFoundException("Category not found")));


        transaction.setOriginUser(validateUsers(originUserId));


        if (transactionRequest.destinationUserId() != null){
            transaction.setDestinationUser(validateUsers(transactionRequest.destinationUserId()));
        } else {
            transaction.setDestinationUser(null);
        }

        if (transaction.getOriginUser().equals(transaction.getDestinationUser())) {

            throw new IllegalArgumentException("You cant make transactions to your same account!");
        }


        LocalDateTime dateTime = LocalDateTime.now();
        transaction.setTransactionTime(dateTime);

        validateOperation(transaction);

        Transaction savedTransaction = transactionRepository.save(transaction);

        return new TransactionResponse(savedTransaction.getId(), savedTransaction.getOperation(), savedTransaction.getTransactionAmount(),
                savedTransaction.getCategory().getId(), savedTransaction.getOriginUser().getId(),
                (savedTransaction.getDestinationUser() != null) ? savedTransaction.getDestinationUser().getId() : null, savedTransaction.getTransactionTime());

    }

    public void deleteOperation(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        transactionRepository.delete(transaction);
    }


    public BigDecimal calculateMonthlyIncome (User userId) {


        LocalDateTime start = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDateTime end = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth());

         BigDecimal monthlyIncome = transactionRepository.monthlyIncome(userId,start,end, Operation.INCOME);

         if (monthlyIncome == null){
             monthlyIncome = new BigDecimal(0);
         }

         return monthlyIncome.setScale(2, RoundingMode.HALF_UP);


    }


    public BigDecimal calculateMonthlyExpenses(User user) {


        LocalDateTime start = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDateTime end = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth());

        BigDecimal monthlyExpenses = transactionRepository.monthlyExpense(user,start, end, Operation.EXPENSE);

        if (monthlyExpenses == null){
            monthlyExpenses = new BigDecimal(0);
        }

        return monthlyExpenses.setScale(2, RoundingMode.HALF_UP);

    }

    public BigDecimal calculateMonthlyTransfers(User user){
        LocalDateTime start = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDateTime end = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth());

        BigDecimal monthlyTransfer = transactionRepository.monthlyTransfer(user, start, end, Operation.TRANSFER);

        if (monthlyTransfer == null){
            monthlyTransfer = new BigDecimal(0);
        }

        return monthlyTransfer.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal balance (User user) {

        return calculateMonthlyIncome(user).subtract(calculateMonthlyExpenses(user));
    }

    public TransactionResponse updateTransactionCategory(Long transactionId, Long categoryId) {

        Transaction newtransaction = getTransactionOrThrow(transactionId);
        newtransaction.setCategory(categoryRepository.findById(categoryId).orElseThrow(()-> new EntityNotFoundException("Category not found")));

        Transaction transaction = transactionRepository.save(newtransaction);

        return new TransactionResponse(transaction.getId(), transaction.getOperation(), transaction.getTransactionAmount(), (transaction.getCategory().getId() != null) ? transaction.getCategory().getId() : null, transaction.getOriginUser().getId(), (transaction.getDestinationUser().getId() != null) ? transaction.getDestinationUser().getId() : null, transaction.getTransactionTime());
        //hola

    }

    public TransactionResponse getTransactionById(Long transactionId) {
        Transaction transaction = getTransactionOrThrow(transactionId);

        return new TransactionResponse(transaction.getId(), transaction.getOperation(), transaction.getTransactionAmount(), (transaction.getCategory().getId() != null) ? transaction.getCategory().getId() : null, transaction.getOriginUser().getId(), (transaction.getDestinationUser().getId() != null) ? transaction.getDestinationUser().getId() : null, transaction.getTransactionTime());

    }




}








