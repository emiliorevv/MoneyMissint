package com.example.moneymissint.repository;

import com.example.moneymissint.Model.Category;
import com.example.moneymissint.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Page<Category> findCategoriesByCategoryName(String categoryName, Pageable pageable);

    Page<Category> findAllByUser(User user, Pageable pageable);
    
    
}
