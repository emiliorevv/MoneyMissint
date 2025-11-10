package com.example.moneymissint.repository;

import com.example.moneymissint.model.Category;
import com.example.moneymissint.model.Transaction;
import com.example.moneymissint.model.User;
import com.example.moneymissint.roles.Operation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByCategory(Category category, Pageable pageable);
    
    Page<Transaction> findAllByOriginUser(User originUser, Pageable pageable);

    Page<Transaction> findAllByDestinationUser(User destinationUser, Pageable pageable);

    Page<Transaction> findByTransactionTime(LocalDateTime transactionTime, Pageable pageable);

    Transaction findByTransactionAmount(BigDecimal transactionAmount);

    @Query("SELECT SUM(t.transactionAmount) FROM Transaction t WHERE t.originUser = :originUser AND t.transactionTime BETWEEN :start AND :end AND t.operation = :operation")
    BigDecimal monthlyExpense(User originUser, LocalDateTime start, LocalDateTime end, Operation operation);

    @Query("SELECT SUM(t.transactionAmount) FROM Transaction t WHERE t.originUser = :originUser AND t.transactionTime BETWEEN :start AND :end AND t.operation = :operation")
    BigDecimal monthlyIncome(User originUser, LocalDateTime start, LocalDateTime end, Operation operation);

}
