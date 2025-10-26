package com.example.moneymissint.model;
import com.example.moneymissint.roles.Operation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "Transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column( nullable = false, precision = 19, scale = 2)
    private BigDecimal transactionAmount;

    @Enumerated(EnumType.STRING)
    @Column (nullable = false, updatable = false)
    private Operation operation;

    @CreationTimestamp
    @Column( updatable = false)
    private LocalDateTime transactionTime;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id" )
    private Category category;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_user_id")
    private User originUser;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_user_id")
    private User destinationUser;
}
