package com.example.moneymissint.Model;
import  lombok.Data;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Transactions")
public class Transactions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @Column(name = "Transaction Amount", nullable = false, length = 11)
    private float transactionAmount;

    @CreationTimestamp
    @Column(name = "Time of the transaction", updatable = false)
    private LocalDateTime transactionTime;


    @Column(length = 30, nullable = false, unique = true)
    private String userId;

    @Column(name = "Currency of the transaction", nullable = false)
    private String currency;




}
