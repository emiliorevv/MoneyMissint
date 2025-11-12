package com.example.moneymissint.service;

import com.example.moneymissint.model.Category;
import com.example.moneymissint.model.User;
import com.example.moneymissint.repository.CategoryRepository;
import com.example.moneymissint.repository.TransactionRepository;
import com.example.moneymissint.repository.UserRepository;
import com.example.moneymissint.roles.Status;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private final UserRepository userRepository;

    private final TransactionRepository transactionRepository;

    private final UserService userService;

    public Category getCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
    }

    public Category createCategory(Long userId, @NotBlank String categoryName) {
       User user = userService.getUserOrThrow(userId);
        if (user.getStatus() != Status.ACTIVE){
            throw new IllegalStateException("User is inactive");
        }

        Category category = new Category();
        category.setCategoryName(categoryName);
        category.setUser(user);
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long categoryId, String categoryName){
        Category category = getCategoryOrThrow(categoryId);
        category.setCategoryName(categoryName);

        return category;
    }

    public void  deleteCategory(Long categoryId){
        Category category = getCategoryOrThrow(categoryId);
        categoryRepository.delete(category);
    }

}
