package com.example.moneymissint.Model;
import com.example.moneymissint.roles.Currency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
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

    @Enumerated(EnumType.STRING)
    @Column( nullable = false)
    private Currency currency;
}

