package com.example.moneymissint.repository;

import com.example.moneymissint.Model.Category;
import com.example.moneymissint.Model.Transaction;
import com.example.moneymissint.Model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByCategory(Category category, Pageable pageable);
    
    Page<Transaction> findAllByOriginUser(User originUser, Pageable pageable);

    Page<Transaction> findAllByDestinationUser(User destinationUser, Pageable pageable);

    Page<Transaction> findByTransactionTime(LocalDateTime transactionTime, Pageable pageable);

}
