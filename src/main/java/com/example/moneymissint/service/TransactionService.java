package com.example.moneymissint.service;
import com.example.moneymissint.model.Category;
import com.example.moneymissint.model.Transaction;
import com.example.moneymissint.model.User;
import com.example.moneymissint.repository.CategoryRepository;
import com.example.moneymissint.repository.TransactionRepository;
import com.example.moneymissint.repository.UserRepository;
import com.example.moneymissint.roles.Operation;
import com.example.moneymissint.roles.Status;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;

    private final UserRepository userRepository;


    private final CategoryRepository categoryRepository;



    public Transaction getTransactionOrThow(Long transactionId) {
        return transactionRepository.findById(transactionId).orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
    }

    private Transaction validateUsers(Transaction transaction) {
        if (transaction.getOriginUser() == null) {
            throw new IllegalArgumentException("Origin User is null");
        }

        if (transaction.getOriginUser().getStatus().equals(Status.INACTIVE)) {
            throw new IllegalStateException("Origin user status is INACTIVE");

        }
        return  transaction;
    }

    public Transaction createTransaction(Transaction transaction) {


        if (transaction.getOriginUser() == null){
            throw new IllegalArgumentException("Origin User is null");
        }

        if (transaction.getOperation() == null){
            throw new IllegalArgumentException("Operation is null");
        }

        if (transaction.getDestinationUser() == null){
            throw new IllegalArgumentException("Destination User is null");
        }



        if (transaction.getTransactionAmount() == null || transaction.getTransactionAmount().compareTo(BigDecimal.ZERO) <= 0){

            throw new IllegalArgumentException("Invalid transaction amount!");
        }

        User originUser = userRepository.findById
                (Objects.requireNonNull(transaction.getOriginUser().getId())).
                orElseThrow(()-> new EntityNotFoundException("Origin user not found"));


        User destinationUser = userRepository.findById(Objects.requireNonNull(transaction.getDestinationUser().getId())).
                orElseThrow(() -> new EntityNotFoundException("Destination user not found"));

        Category category = categoryRepository.findById(Objects.requireNonNull(transaction.getCategory().getId())).
                orElseThrow(()-> new EntityNotFoundException("Category not found"));

        if (originUser.getStatus().equals(Status.INACTIVE)) {
            throw new IllegalStateException("Origin user status is INACTIVE");
        }

        if (destinationUser.getStatus().equals(Status.INACTIVE)) {
            throw new IllegalStateException("Destination user status is INACTIVE");
        }


        if (originUser.getId().equals(destinationUser.getId())) {

            throw new IllegalArgumentException("You cant make transactions to your same account!");
        }

        LocalDateTime dateTime = LocalDateTime.now();
        transaction.setTransactionTime(dateTime);
        transaction.setOriginUser(originUser);
        transaction.setDestinationUser(destinationUser);
        transaction.setCategory(category);

        return  transactionRepository.save(transaction);
    }


    public Transaction createOperation(Transaction transaction) {
        if (transaction.getOriginUser() == null){
            throw new IllegalArgumentException("Origin User is null");
        }

        if (transaction.getOperation() == null){
            throw new IllegalArgumentException("Operation is null");
        }

        if (transaction.getTransactionAmount() == null || transaction.getTransactionAmount().compareTo(BigDecimal.ZERO) <= 0){

            throw new IllegalArgumentException("Invalid transaction amount!");
        }

        User originUser = userRepository.findById
                        (Objects.requireNonNull(transaction.getOriginUser().getId())).
                orElseThrow(()-> new EntityNotFoundException("Origin user not found"));


        Category category = categoryRepository.findById(Objects.requireNonNull(transaction.getCategory().getId())).
                orElseThrow(()-> new EntityNotFoundException("Category not found"));

        if (originUser.getStatus().equals(Status.INACTIVE)) {
            throw new IllegalStateException("Origin user status is INACTIVE");
        }

        LocalDateTime dateTime = LocalDateTime.now();
        transaction.setTransactionTime(dateTime);
        transaction.setOriginUser(originUser);
        transaction.setCategory(category);

        return  transactionRepository.save(transaction);

    }



    public Void  deleteOperation(Transaction transaction) {

        validateUsers(transaction);

        transactionRepository.delete(transaction);

        return null;
    }

    public BigDecimal calculateMonthlyIncome (Transaction transaction) {
       validateUsers(transaction);

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().minusDays(30);

         BigDecimal monthlyIncome = transactionRepository.findAllByTransactionTimeBetweenAndOperation_Income(start,end, Operation.INCOME).getTransactionAmount();

         return monthlyIncome.setScale(2, RoundingMode.HALF_UP);


    }


    public BigDecimal calculateMonthlyExpenses(Transaction transaction) {
        validateUsers(transaction);

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().minusDays(30);

        BigDecimal monthlyExpenses = transactionRepository.findAllByTransactionTimeBetweenAndOperation_Expense(start, end, Operation.EXPENSE).getTransactionAmount();

        return monthlyExpenses.setScale(2, RoundingMode.HALF_UP);

    }

    public BigDecimal balance (Transaction transaction) {
       validateUsers(transaction);

        return calculateMonthlyIncome(transaction).subtract(calculateMonthlyExpenses(transaction));
    }

    public Transaction updateTransactionCategory(Transaction transaction) {
       validateUsers(transaction);

       Category category = transaction.getCategory();

       if (category == null) {
           throw new IllegalArgumentException("Category is null");
       }

       transaction.setCategory(category);

       return transactionRepository.save(transaction);
    }



}








