package com.example.moneymissint.service;

import com.example.moneymissint.repository.TransactionRepository;
import com.example.moneymissint.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionsService {

    private final TransactionRepository transactionRepository;

    private final UserRepository userRepository;




}
