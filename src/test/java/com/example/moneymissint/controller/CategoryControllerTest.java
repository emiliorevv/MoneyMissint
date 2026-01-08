package com.example.moneymissint.controller;

import com.example.moneymissint.DTO.CategoryRequest;
import com.example.moneymissint.model.Category;
import com.example.moneymissint.model.User;
import com.example.moneymissint.repository.CategoryRepository;
import com.example.moneymissint.repository.UserRepository;
import com.example.moneymissint.roles.Currency;
import com.example.moneymissint.roles.Status;
import com.example.moneymissint.utils.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

@SpringBootTest(properties = {"JWT_SECRET = ultramegasecretpasswordinHere2390481348139440582934324234567"})
@AutoConfigureMockMvc
@Transactional
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;



    @Test
    @DisplayName("Create category, it should return created successfully")
    void createCategory_shouldReturnCreated_WhenRequestIsValid() throws Exception {
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("462354");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        String token = securityUtils.generateValidToken("emilio@gmail.com");

        CategoryRequest categoryRequest = new CategoryRequest("exampleCategory");


        mockMvc.perform(post("/api/v1/categories").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(categoryRequest))).andDo(MockMvcResultHandlers.print()).andExpect(status().isCreated()).andExpect(jsonPath("$.name").exists());

        var savedCategory = categoryRepository.findByCategoryNameIgnoreCase(categoryRequest.nameOfCategory()).orElseThrow();
        assertEquals(categoryRequest.nameOfCategory(), savedCategory.getCategoryName());
        assertEquals(user, savedCategory.getUser());
        assertNotNull(savedCategory.getId());

    }

    @Test
    @DisplayName("Create category, it should return error because data is invalid")
    void createCategory_ThrowException_InvalidData() throws Exception {
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("462354");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        String token = securityUtils.generateValidToken("emilio@gmail.com");

        CategoryRequest categoryRequest = new CategoryRequest("");

        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nameOfCategory").exists());
    }

    @Test
    @DisplayName("Create category, it should return error because the category is duplicated")
    void createCategory_ThrowException_CategoryAlreadyExists() throws Exception{
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("462354");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        String token = securityUtils.generateValidToken("emilio@gmail.com");

        Category category = new Category();
        category.setCategoryName("duplicatedCategory");
        category.setUser(user);
        categoryRepository.save(category);
        CategoryRequest categoryRequest = new CategoryRequest("duplicatedCategory");

        mockMvc.perform(post("/api/v1/categories").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(categoryRequest)))
                .andDo(MockMvcResultHandlers.print()).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Get categories by user, it should return only the categories that the user has")
    void getCategoriesByUser_ShouldReturnOnlyCategoriesByUser() throws Exception{
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("462354");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        String token = securityUtils.generateValidToken("emilio@gmail.com");

        User otherUser = new User();
        otherUser.setName("javier");
        otherUser.setEmail("javier@gmail.com");
        otherUser.setPassword("654321");
        otherUser.setCurrency(Currency.EUR);
        otherUser.setStatus(Status.ACTIVE);
        userRepository.save(otherUser);

        Category category1 = new Category();
        category1.setCategoryName("category1");
        category1.setUser(user);
        categoryRepository.save(category1);

        Category category2 = new Category();
        category2.setCategoryName("category2");
        category2.setUser(user);
        categoryRepository.save(category2);

        Category categoryOtherUser = new Category();
        categoryOtherUser.setCategoryName("categoryOtherUser");
        categoryOtherUser.setUser(otherUser);
        categoryRepository.save(categoryOtherUser);

        mockMvc.perform(get("/api/v1/categories").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(2))).andExpect(jsonPath("$.content[*].name", containsInAnyOrder("category1", "category2"))).andExpect(jsonPath("$.content[*].name",not(containsInAnyOrder("categoryOtherUser"))));
    }

    @Test
    @DisplayName("Update category name, it should return updated successfully")
    void updateCategory_ShouldReturnUpdated_WhenRequestIsValid() throws Exception {
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("462354");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        String token = securityUtils.generateValidToken("emilio@gmail.com");


        Category category = new Category();
        category.setCategoryName("categoryToUpdate");
        category.setUser(user);
        categoryRepository.save(category);

        CategoryRequest categoryRequest = new CategoryRequest("newCategoryName");

        mockMvc.perform(put("/api/v1/categories/"+category.getId()).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(categoryRequest))).andDo(MockMvcResultHandlers.print()).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("newCategoryName"));

        Category updatedCategory = categoryRepository.findById(category.getId()).orElseThrow();
        assertEquals(categoryRequest.nameOfCategory(), updatedCategory.getCategoryName());

    }

    @Test
    @DisplayName("Update category name, it should return error because the category is from another user")
    void updateCategory_ThrowException_CategoryFromOtherUser() throws Exception {
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("462354");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        String token = securityUtils.generateValidToken("emilio@gmail.com");

        User otherUser = new User();
        otherUser.setName("javier");
        otherUser.setEmail("javier@gmail.com");
        otherUser.setPassword("654321");
        otherUser.setCurrency(Currency.EUR);
        otherUser.setStatus(Status.ACTIVE);
        userRepository.save(otherUser);

        Category category = new Category();
        category.setCategoryName("categoryToUpdate");
        category.setUser(otherUser);
        categoryRepository.save(category);

        CategoryRequest categoryRequest = new CategoryRequest("newCategoryName");

        mockMvc.perform(put("/api/v1/categories/"+category.getId()).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(categoryRequest))).andDo(MockMvcResultHandlers.print()).andExpect(status().isNotFound());


    }

    @Test
    @DisplayName("Delete category, it should return no content successfully")
    void deleteCategory_ShouldReturnNoContent_WhenRequestIsValid() throws Exception {
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("462354");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        String token = securityUtils.generateValidToken("emilio@gmail.com");


        Category category = new Category();
        category.setCategoryName("categoryToUpdate");
        category.setUser(user);
        categoryRepository.save(category);

        mockMvc.perform(delete("/api/v1/categories/"+category.getId()).header("Authorization", "Bearer " + token)).andDo(MockMvcResultHandlers.print()).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Delete category, it should return not found, because the cateogry is from another user")
    void deleteCategory_ThrowException_CategoryFromOtherUser() throws Exception {
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("462354");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        String token = securityUtils.generateValidToken("emilio@gmail.com");

        User otherUser = new User();
        otherUser.setName("javier");
        otherUser.setEmail("javier@gmail.com");
        otherUser.setPassword("654321");
        otherUser.setCurrency(Currency.EUR);
        otherUser.setStatus(Status.ACTIVE);
        userRepository.save(otherUser);

        Category category = new Category();
        category.setCategoryName("categoryToUpdate");
        category.setUser(otherUser);
        categoryRepository.save(category);

        mockMvc.perform(delete("/api/v1/categories/"+category.getId()).header("Authorization", "Bearer " + token)).andDo(MockMvcResultHandlers.print()).andExpect(status().isNotFound());

    }
}
