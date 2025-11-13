package com.example.moneymissint.repository;

import com.example.moneymissint.model.Category;
import com.example.moneymissint.model.User;
import com.example.moneymissint.roles.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface    CategoryRepository extends JpaRepository<Category, Long> {

    Page<Category> findCategoriesByCategoryName(String categoryName, Pageable pageable);

    Page<Category> findAllByUser(User user, Pageable pageable);

    boolean existsByUserIdAndCategoryNameIgnoreCaseAndIdNot(Long userId, String categoryName, Long categoryId);

    boolean existsByUserIdAndCategoryNameIgnoreCase(Long userId, String categoryName);

    @Query("update Transaction t set t.category = null where t.category.id = :categoryId")
    int clearCategoryByCategoryId(Long categoryId);
    
}
