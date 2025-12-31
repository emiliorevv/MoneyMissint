package com.example.moneymissint.service;


import com.example.moneymissint.DTO.CategoryRequest;
import com.example.moneymissint.DTO.CategoryResponse;
import com.example.moneymissint.model.Category;
import com.example.moneymissint.model.User;
import com.example.moneymissint.repository.CategoryRepository;
import com.example.moneymissint.repository.TransactionRepository;
import com.example.moneymissint.roles.Status;
import com.example.moneymissint.utils.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private TransactionRepository transactionRepository;

    private User user;



    @InjectMocks
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        this.user = SecurityUtils.mockedLoginUser(Status.ACTIVE, securityContext, authentication);

    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }


    @Test
    @DisplayName("Create category test, it should create the category without problems")
    void createCategory_Success(){
        this.user.setId(1L);

        CategoryRequest categoryRequest = new CategoryRequest("exampleCategory");


        Category category = new Category();
        category.setCategoryName(categoryRequest.nameOfCategory());
        category.setUser(this.user);
        category.setId(1L);

        when(categoryRepository.existsByUserIdAndCategoryNameIgnoreCase(user.getId(), categoryRequest.nameOfCategory().trim())).thenReturn(false);

        when(categoryRepository.save(any(Category.class))).thenReturn(category);



        CategoryResponse categoryResponse = categoryService.createCategory(categoryRequest);

        assertThat(categoryResponse).isNotNull();
        assertThat(categoryResponse.categoryId()).isEqualTo(1L);

        assertThat(categoryResponse.name().equals(categoryRequest.nameOfCategory()));

        verify(categoryRepository, times(1)).save(any(Category.class));

    }

    @Test
    @DisplayName("Create category test, it should give error because the user is inactive")
    void createCategory_ThrowException_UserInactive(){
        this.user.setStatus(Status.INACTIVE);
        this.user.setId(1L);

        CategoryRequest categoryRequest = new CategoryRequest("exampleCategory");

        assertThatThrownBy(() -> categoryService.createCategory(categoryRequest)).isInstanceOf(IllegalStateException.class).hasMessage("User is inactive");

        verify(categoryRepository, never()).save(any(Category.class));

    }

    @Test
    @DisplayName("Create category test, it should give error because the category already exists")
    void createCategory_ThrowException_CategoryAlreadyExists(){
        this.user.setId(1L);
        CategoryRequest categoryRequest = new CategoryRequest("duplicatedCategory");

        Category category = new Category();
        category.setCategoryName(categoryRequest.nameOfCategory());
        category.setUser(this.user);
        category.setId(1L);

        when(categoryRepository.existsByUserIdAndCategoryNameIgnoreCase(user.getId(), category.getCategoryName().trim())).thenReturn(true);
        assertThatThrownBy(() -> categoryService.createCategory(categoryRequest)).isInstanceOf(IllegalStateException.class).hasMessage("Category already exists");

        verify(categoryRepository, never()).save(any(Category.class));
    }


    @Test
    @DisplayName("Rename Category, it should rename the category without problems")
    void renameCategory_Success(){
        this.user.setId(1L);
        Long categoryId = 1L;
        String name = "category name";
        Category category = new Category();
        category.setId(categoryId);
        category.setCategoryName(name);
        category.setUser(this.user);

        CategoryRequest categoryRequest = new CategoryRequest("New Category Name");

        when(categoryRepository.findById(categoryId)).thenReturn(java.util.Optional.of(category));

        when(categoryRepository.existsByUserIdAndCategoryNameIgnoreCaseAndIdNot(user.getId(), categoryRequest.nameOfCategory().trim(), categoryId)).thenReturn(false);

        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse categoryResponse = categoryService.renameCategory(categoryId, categoryRequest);

        assertThat(categoryResponse.name()).isEqualTo(categoryRequest.nameOfCategory());

        assertThat(categoryResponse.categoryId()).isEqualTo(categoryId);

        verify(categoryRepository, times(1)).save(any(Category.class));

    }

    @Test
    @DisplayName("Rename category, it should give an error because user is inactive")
    void renameCategory_ThrowException_UserInactive(){
        this.user.setStatus(Status.INACTIVE);
        this.user.setId(1L);
        Long categoryId = 1L;
        Category category = new Category();
         category.setId(categoryId);
         category.setUser(this.user);
         category.setCategoryName("category name");

        CategoryRequest categoryRequest = new CategoryRequest("New Category Name");

        when(categoryRepository.findById(categoryId)).thenReturn(java.util.Optional.of(category));


        assertThatThrownBy(() -> categoryService.renameCategory(categoryId, categoryRequest)).isInstanceOf(IllegalStateException.class).hasMessage("User is inactive");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Rename category, it should give error because the category is from another user")
    void renameCategory_ThrowException_CategoryFromOtherUser(){
        this.user.setId(1L);
        User otherUser = new User();
         otherUser.setId(2L);
        Long categoryId = 1L;
        Category category = new Category();
         category.setId(categoryId);
         category.setUser(otherUser);
         category.setCategoryName("category name");

         CategoryRequest categoryRequest = new CategoryRequest("New Category Name");


         when(categoryRepository.findById(categoryId)).thenReturn(java.util.Optional.of(category));


         assertThatThrownBy(() -> categoryService.renameCategory(categoryId, categoryRequest)).isInstanceOf(EntityNotFoundException.class).hasMessage("Category not found");

         verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Rename category, it should give error because the category name is already in use")
    void renameCategory_ThrowException_CategoryNameAlreadyInUse(){
        this.user.setId(1L);
        Long categoryId = 1L;
        Category category = new Category();
         category.setId(categoryId);
         category.setUser(this.user);
         category.setCategoryName("category name");
         CategoryRequest categoryRequest = new CategoryRequest("category name");
         when(categoryRepository.findById(categoryId)).thenReturn(java.util.Optional.of(category));

         when(categoryRepository.existsByUserIdAndCategoryNameIgnoreCaseAndIdNot(user.getId(), categoryRequest.nameOfCategory().trim(), categoryId)).thenReturn(true);
         assertThatThrownBy(() -> categoryService.renameCategory(categoryId, categoryRequest)).isInstanceOf(IllegalStateException.class).hasMessage("Category already exists");
         verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Get All categories, it should return all categories successfully")
    void getAllCategories_Success(){
        this.user.setId(1L);
        Category category = new Category();
        Category category2 = new Category();
        List<Category> categories = List.of(category, category2);
        Page<Category> page = new PageImpl<>(categories);


        category2.setUser(this.user);
        category.setUser(this.user);

        when(categoryRepository.findAllByUser(eq(user), any(Pageable.class))).thenReturn(page);

        Page<CategoryResponse> categoriesResponse = categoryService.getAllCategories(PageRequest.of(0, 10));

        assertThat(categoriesResponse.getContent().size()).isEqualTo(2);
        verify(categoryRepository, times(1)).findAllByUser(eq(user), any(Pageable.class));

    }

    @Test
    @DisplayName("Get All categories, it should return empty list because user is inactive")
    void getAllCategories_ThrowException_UserInactive(){
        this.user.setStatus(Status.INACTIVE);
        this.user.setId(1L);
        assertThatThrownBy(() -> categoryService.getAllCategories(PageRequest.of(0, 10))).isInstanceOf(IllegalStateException.class).hasMessage("User is inactive");
        verify(categoryRepository, never()).findAllByUser(eq(user), any(Pageable.class));
    }

    @Test
    @DisplayName("Delete category, it should delete the category successfully")
    void deleteCategory_Success(){
        this.user.setId(1L);
        Long categoryId = 1L;
        Category category = new Category();
        category.setId(categoryId);
        category.setUser(this.user);
        category.setCategoryName("category name");

        when(categoryRepository.findById(categoryId)).thenReturn(java.util.Optional.of(category));
        categoryService.deleteCategory(categoryId);

        verify(transactionRepository, times(1)).clearCategoryByCategoryId(categoryId);
        verify(categoryRepository, times(1)).delete(category);

    }

    @Test
    @DisplayName("Delete category, it should give error because user is inactive")
    void deleteCategory_ThrowException_UserInactive(){
        this.user.setStatus(Status.INACTIVE);
        this.user.setId(1L);
        Long categoryId = 1L;
        Category category = new Category();
        category.setId(categoryId);
        category.setUser(this.user);
        category.setCategoryName("category name");

        when(categoryRepository.findById(categoryId)).thenReturn(java.util.Optional.of(category));
        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId)).isInstanceOf(IllegalStateException.class).hasMessage("User is inactive");
        verify(categoryRepository, never()).delete(category);
    }

    @Test
    @DisplayName("Delete category, it should give error because category is from another user")
    void deleteCategory_ThrowException_CategoryFromOtherUser(){
        this.user.setId(1L);
        User otherUser = new User();
         otherUser.setId(2L);
        Long categoryId = 1L;
        Category category = new Category();
         category.setId(categoryId);
         category.setUser(otherUser);
         category.setCategoryName("category name");
         when(categoryRepository.findById(categoryId)).thenReturn(java.util.Optional.of(category));
         assertThatThrownBy(() -> categoryService.deleteCategory(categoryId)).isInstanceOf(EntityNotFoundException.class).hasMessage("Category not found");
         verify(categoryRepository, never()).delete(category);
    }




}
