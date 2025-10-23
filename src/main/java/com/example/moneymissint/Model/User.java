package com.example.moneymissint.Model;
import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "Users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, unique = true, nullable = false)
    private String name;

    @Column(length = 100, unique = true, nullable = false)
    private String email;

    @Column(length = 60, name = "password_hash", nullable = false)
    private String password;

    @Column(length = 3, nullable = false)
    private String moneyDenomination;
}

