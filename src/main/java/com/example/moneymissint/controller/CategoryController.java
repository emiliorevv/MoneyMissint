package com.example.moneymissint.controller;

import com.example.moneymissint.DTO.CategoryRequest;
import com.example.moneymissint.DTO.CategoryResponse;
import com.example.moneymissint.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated

@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping({"/{userId}"})
    public ResponseEntity<CategoryResponse> createCategory(@PathVariable Long userId, @RequestBody @Valid CategoryRequest categoryRequest){
        CategoryResponse categoryResponse = categoryService.createCategory(userId, categoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryResponse);
    }

    @PutMapping("/{categoryId}/{userId}")
    public ResponseEntity<CategoryResponse> renameCategory(@PathVariable Long categoryId, @PathVariable Long userId, @RequestBody @Valid CategoryRequest categoryRequest){
        CategoryResponse categoryResponse = categoryService.renameCategory(categoryId, categoryRequest, userId);
        return ResponseEntity.status(HttpStatus.OK).body(categoryResponse);
    }

    @DeleteMapping("/{categoryId}/{userId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId, @PathVariable Long userId) {
        categoryService.deleteCategory(userId, categoryId);
        return ResponseEntity.status((HttpStatus.NO_CONTENT)).body(null);
    }
}
