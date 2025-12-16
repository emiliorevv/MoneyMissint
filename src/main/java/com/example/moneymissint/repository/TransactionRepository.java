package com.example.moneymissint.repository;
import com.example.moneymissint.model.Transaction;
import com.example.moneymissint.model.User;
import com.example.moneymissint.roles.Operation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT SUM(t.transactionAmount) FROM Transaction t WHERE t.originUser = :originUser AND t.transactionTime BETWEEN :start AND :end AND t.operation = :operation")
    BigDecimal monthlyExpense(User originUser, LocalDateTime start, LocalDateTime end, Operation operation);

    @Query("SELECT SUM(t.transactionAmount) FROM Transaction t WHERE t.originUser = :originUser AND t.transactionTime BETWEEN :start AND :end AND t.operation = :operation")
    BigDecimal monthlyIncome(User originUser, LocalDateTime start, LocalDateTime end, Operation operation);

    @Query("SELECT SUM(t.transactionAmount) FROM Transaction t WHERE t.originUser = :originUser AND t.transactionTime BETWEEN :start AND :end AND t.operation = :operation")
    BigDecimal monthlyTransfer(User originUser, LocalDateTime start, LocalDateTime end, Operation operation);


    @Modifying
    @Query("update Transaction t set t.category = null where t.category.id = :categoryId")
    int clearCategoryByCategoryId(Long categoryId);

}
