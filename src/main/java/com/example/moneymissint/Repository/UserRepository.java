package com.example.moneymissint.Repository;


import com.example.moneymissint.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface UserRepository extends JpaRepository<User, Long> {

    public List<User>
}
