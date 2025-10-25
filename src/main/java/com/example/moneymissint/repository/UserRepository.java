package com.example.moneymissint.repository;

import com.example.moneymissint.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository

public interface UserRepository extends JpaRepository<User, Long> {

    Page<User> findUserByName(String name, Pageable pageable);




}
