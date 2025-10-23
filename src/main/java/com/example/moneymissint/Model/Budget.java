package com.example.moneymissint.Model;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToOne;
import lombok.Data;
import jakarta.persistence.JoinColumn;

@Entity
@Data

@Table(name = "Budgets")
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(updatable = true)
    private float accountBudget;

    @Column(updatable = true)
    private float savings;


    @Column(updatable = true)
    private float monthlySpendings;

    @Column(updatable = true)
    private float expenses;

    @Column(updatable = true)
    private float income;

    @OneToOne
    @JoinColumn(name = "moneyDenomination_User")
    private User user;



}
