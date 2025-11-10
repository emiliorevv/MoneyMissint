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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;
import java.time.temporal.TemporalAdjusters;
@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;

    private final UserRepository userRepository;


    private final CategoryRepository categoryRepository;



    public Transaction getTransactionOrThrow(Long transactionId) {
        return transactionRepository.findById(transactionId).orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

    }

    public User validateUsers(Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (user.getStatus() == Status.INACTIVE) {
            throw new IllegalStateException("User is Inactive");
        }

        return user;

    }

    public Transaction validateOperation(Transaction transaction) {
        if (transaction.getOriginUser() == null){
            throw new IllegalArgumentException("Origin User is null");
        }

        if (transaction.getOperation() == null){
            throw new IllegalArgumentException("Operation is null");
        }

        if (transaction.getCategory() == null){
            throw new IllegalArgumentException("Category is null");
        }

        return transaction;
    }




    public Transaction createTransaction(Transaction transaction) {


        if (transaction.getDestinationUser() == null){
            throw new IllegalArgumentException("Destination User is null");
        }

        validateOperation(transaction);
        validateUsers(transaction.getOriginUser().getId());
        validateUsers(transaction.getDestinationUser().getId());









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
        validateUsers(transaction.getOriginUser().getId());

        validateOperation(transaction);



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



    public Boolean deleteOperation(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        transactionRepository.delete(transaction);
        return true;
    }


    public BigDecimal calculateMonthlyIncome (User userId) {
        validateUsers(userId.getId());

        LocalDateTime start = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDateTime end = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth());

         BigDecimal monthlyIncome = transactionRepository.monthlyIncome(userId,start,end, Operation.INCOME);

         if (monthlyIncome == null){
             monthlyIncome = new BigDecimal(0);
         }

         return monthlyIncome.setScale(2, RoundingMode.HALF_UP);


    }


    public BigDecimal calculateMonthlyExpenses(User userId) {

        validateUsers(userId.getId());
        LocalDateTime start = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDateTime end = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth());

        BigDecimal monthlyExpenses = transactionRepository.monthlyExpense(userId,start, end, Operation.EXPENSE);

        if (monthlyExpenses == null){
            monthlyExpenses = new BigDecimal(0);
        }

        return monthlyExpenses.setScale(2, RoundingMode.HALF_UP);

    }

    public BigDecimal balance (User userId) {
        validateUsers(userId.getId());

        return calculateMonthlyIncome(userId).subtract(calculateMonthlyExpenses(userId));
    }

    public Transaction updateTransactionCategory(Long  transactionId, Long categoryId) {

        Transaction transaction = getTransactionOrThrow(transactionId);
        Category category = categoryRepository.findById(categoryId).orElseThrow(()-> new EntityNotFoundException("Category was not found"));

        transaction.setCategory(category);
        return transactionRepository.save(transaction);

    }




}








