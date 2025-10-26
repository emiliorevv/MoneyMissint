package com.example.moneymissint.repository;

import com.example.moneymissint.model.Category;
import com.example.moneymissint.model.User;
import com.example.moneymissint.roles.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Page<Category> findCategoriesByCategoryName(String categoryName, Pageable pageable);

    Page<Category> findAllByUser(User user, Pageable pageable);

    Page<Category> findAllByStatusIs(Status status, Pageable pageable);
    
}
