package com.example.moneymissint.repository;

import com.example.moneymissint.model.Category;
import com.example.moneymissint.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.Optional;

@Repository
public interface    CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByCategoryNameIgnoreCase(String categoryName);


    Page<Category> findAllByUser(User user, Pageable pageable);

    boolean existsByUserIdAndCategoryNameIgnoreCaseAndIdNot(Long userId, String categoryName, Long categoryId);

    boolean existsByUserIdAndCategoryNameIgnoreCase(Long userId, String categoryName);


}
