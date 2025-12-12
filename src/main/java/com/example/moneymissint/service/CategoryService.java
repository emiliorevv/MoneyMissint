package com.example.moneymissint.service;
import com.example.moneymissint.DTO.CategoryRequest;
import com.example.moneymissint.DTO.CategoryResponse;
import com.example.moneymissint.model.Category;
import com.example.moneymissint.model.User;
import com.example.moneymissint.repository.CategoryRepository;
import com.example.moneymissint.repository.TransactionRepository;
import com.example.moneymissint.roles.Status;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    private Category getCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
    }

    public CategoryResponse createCategory(Long userId, CategoryRequest categoryRequest) {

        if (userService.getUserOrThrow(userId).getStatus() != Status.ACTIVE){
            throw new IllegalStateException("User is inactive");
        }

        Category category = new Category();
        category.setUser(userService.getUserOrThrow(userId));


        String normalizedCategoryName = categoryRequest.nameOfCategory().trim();
        if (categoryRepository.existsByUserIdAndCategoryNameIgnoreCase(userId, normalizedCategoryName)){
            throw new IllegalStateException("Category already exists");
        }

        category.setCategoryName(normalizedCategoryName);

        Category newCategory = categoryRepository.save(category);

        return new CategoryResponse(newCategory.getId(), newCategory.getCategoryName());

    }

    public CategoryResponse renameCategory(Long categoryId, CategoryRequest categoryRequest, Long userId){

        if (userService.getUserOrThrow(userId).getStatus() != Status.ACTIVE){
            throw new IllegalStateException("User is inactive");
        }

        Category category = getCategoryOrThrow(categoryId);
        if (!category.getUser().getId().equals(userId)){
            throw new IllegalStateException("Category does not belong to user");
        }



        String normalizedCategoryName = categoryRequest.nameOfCategory().trim();
        if (categoryRepository.existsByUserIdAndCategoryNameIgnoreCaseAndIdNot(userId, normalizedCategoryName, categoryId)){
            throw new IllegalStateException("Category already exists");
        }

        category.setCategoryName(normalizedCategoryName);

        Category newCategory = categoryRepository.save(category);

        return new CategoryResponse(newCategory.getId(), newCategory.getCategoryName());

        }

        public Page<CategoryResponse> getAllCategories (Long userId, Pageable pageable){
            User user = userService.getUserOrThrow(userId);

            Page<Category> categories = categoryRepository.findAllByUser(user, pageable);

            return categories.map(category -> new CategoryResponse(category.getId(), category.getCategoryName()));

        }



    public void deleteCategory(Long userId, Long categoryId){
        Category category = getCategoryOrThrow(categoryId);

        if (userService.getUserOrThrow(userId).getStatus() != Status.ACTIVE){
            throw new IllegalStateException("User is inactive");
        }

        if (!category.getUser().getId().equals(userId)){
            throw new IllegalStateException("Category does not belong to user");
        }

        transactionRepository.clearCategoryByCategoryId(categoryId);
        categoryRepository.delete(category);
    }

}
