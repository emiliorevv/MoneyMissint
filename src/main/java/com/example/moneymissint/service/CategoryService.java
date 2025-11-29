package com.example.moneymissint.service;
import com.example.moneymissint.model.Category;
import com.example.moneymissint.model.User;
import com.example.moneymissint.repository.CategoryRepository;
import com.example.moneymissint.repository.TransactionRepository;
import com.example.moneymissint.roles.Status;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class CategoryService {

    private final CategoryRepository categoryRepository;
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
        String normalizedCategoryName = categoryName.trim();
        if (categoryRepository.existsByUserIdAndCategoryNameIgnoreCase(userId, normalizedCategoryName)){
            throw new IllegalStateException("Category already exists");
        }
        Category category = new Category();
        category.setCategoryName(normalizedCategoryName);
        category.setUser(user);
        return categoryRepository.save(category);
    }

    public Category renameCategory(Long categoryId, @NotBlank String categoryName, Long userId){

        Category category = getCategoryOrThrow(categoryId);
        if (!category.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Category does not belong to user");
        }

        String normalizedCategoryName = categoryName.trim();

        if (categoryRepository.existsByUserIdAndCategoryNameIgnoreCaseAndIdNot(userId, normalizedCategoryName, categoryId)){
            throw new IllegalStateException("Category already exists");
        }


        category.setCategoryName(normalizedCategoryName);

        return category;
    }

    public void  deleteCategory(Long userId, Long categoryId){
        Category category = getCategoryOrThrow(categoryId);
        if (!category.getUser().getId().equals(userId)){
            throw new IllegalStateException("Category does not belong to user");
        }

        transactionRepository.clearCategoryByCategoryId(categoryId);
        categoryRepository.delete(category);
    }

}
