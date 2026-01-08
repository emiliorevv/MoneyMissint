package com.example.moneymissint.controller;

import com.example.moneymissint.DTO.TransactionRequest;
import com.example.moneymissint.DTO.TransactionResponse;
import com.example.moneymissint.model.Category;
import com.example.moneymissint.model.Transaction;
import com.example.moneymissint.model.User;
import com.example.moneymissint.repository.CategoryRepository;
import com.example.moneymissint.repository.TransactionRepository;
import com.example.moneymissint.repository.UserRepository;
import com.example.moneymissint.roles.Currency;
import com.example.moneymissint.roles.Operation;
import com.example.moneymissint.roles.Status;
import com.example.moneymissint.utils.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest(properties = {"JWT_SECRET = ultramegasecretpasswordinHere2390481348139440582934324234567"})
@AutoConfigureMockMvc
@Transactional
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("Create Transaction Test, it should return created when the request is valid")
    void createTransaction_ShouldReturnCreated_WhenRequestIsValid() throws Exception {
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("462354");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        String token = securityUtils.generateValidToken("emilio@gmail.com");

        Category category = new Category();
        category.setCategoryName("category1");
        category.setUser(user);
        categoryRepository.save(category);

        TransactionRequest transactionRequest = new TransactionRequest(BigDecimal.valueOf(1000.00), Operation.INCOME, category.getId(), null);

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/transactions").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(transactionRequest))).andDo(MockMvcResultHandlers.print()).andExpect(status().isCreated()).andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        TransactionResponse transactionResponse = objectMapper.readValue(jsonResponse, TransactionResponse.class);

        var savedTransaction = transactionRepository.getTransactionById(transactionResponse.transactionId()).orElseThrow();
        assertEquals(savedTransaction.getOriginUser().getId(), user.getId());
        assertEquals(savedTransaction.getCategory().getId(), category.getId());


    }

    @Test
    @DisplayName("Create transaction test, it should return error because the token is expired")
    void createTransaction_ShouldReturnError_WhenTokenIsExpired() throws Exception {
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("462354");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        String token = securityUtils.generateExpiredToken("emilio@gmail.com");

        Category category = new Category();
        category.setCategoryName("category1");
        category.setUser(user);
        categoryRepository.save(category);

        TransactionRequest transactionRequest = new TransactionRequest(BigDecimal.valueOf(1000.00), Operation.INCOME, category.getId(), null);

        mockMvc.perform(post("/api/v1/transactions").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(transactionRequest))).andDo(MockMvcResultHandlers.print()).andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("Create transaction test, it should return bad request, because data is invalid")
    void createTransaction_ShouldReturnBadRequest_WhenDataIsInvalid() throws Exception {
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("462354");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        String token = securityUtils.generateValidToken("emilio@gmail.com");

        Category category = new Category();
        category.setCategoryName("category1");
        category.setUser(user);
        categoryRepository.save(category);

        TransactionRequest transactionRequest = new TransactionRequest( null, null, category.getId(), null);

        mockMvc.perform(post("/api/v1/transactions").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(transactionRequest)))
                .andDo(MockMvcResultHandlers.print()).andExpect(status().isBadRequest()).andExpect(jsonPath("$.amount").exists()).andExpect(jsonPath("$.operation").exists());


    }

    @Test
    @DisplayName("Create transaction test, it should return bad request because you cant make self transfers")
    void createTransaction_ShouldReturnBadRequest_WhenOriginUserIsTheSameAsDestinationUser() throws Exception{
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("462354");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        String token = securityUtils.generateValidToken("emilio@gmail.com");

        Category category = new Category();
        category.setCategoryName("category1");
        category.setUser(user);
        categoryRepository.save(category);

        TransactionRequest transactionRequest = new TransactionRequest(BigDecimal.valueOf(1000.00), Operation.INCOME, category.getId(), user.getId());

        mockMvc.perform(post("/api/v1/transactions").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(transactionRequest)))
                .andDo(MockMvcResultHandlers.print()).andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("Delete operation test, it should return no content successfully")
    void deleteOperation_ShouldReturnNoContent_WhenRequestIsValid() throws Exception {
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("462354");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        String token = securityUtils.generateValidToken("emilio@gmail.com");

        Category category = new Category();
        category.setCategoryName("category1");
        category.setUser(user);
        categoryRepository.save(category);


        Transaction transaction = new Transaction();
        transaction.setOperation(Operation.INCOME);
        transaction.setTransactionAmount(BigDecimal.valueOf(1000.00));
        transaction.setOriginUser(user);
        transaction.setDestinationUser(null);
        transaction.setCategory(category);
        transactionRepository.save(transaction);

        mockMvc.perform(delete("/api/v1/transactions/" + transaction.getId()).header("Authorization", "Bearer " + token)).andDo(MockMvcResultHandlers.print()).andExpect(status().isNoContent());
        assertTrue(transactionRepository.findById(transaction.getId()).isEmpty());
    }

    @Test
    @DisplayName("Delete operation test, it should return not found because transaction is from another user")
    void deleteOperation_ShouldReturnNotFound_WhenRequestIsFromAnotherUser() throws Exception {
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("462354");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        User otherUser = new User();
        otherUser.setName("javier");
        otherUser.setEmail("javier@gmail.com");
        otherUser.setPassword("654321");
        otherUser.setCurrency(Currency.EUR);
        otherUser.setStatus(Status.ACTIVE);
        userRepository.save(otherUser);

        String token = securityUtils.generateValidToken("emilio@gmail.com");

        Category category = new Category();
        category.setCategoryName("category1");
        category.setUser(otherUser);
        categoryRepository.save(category);



        Transaction transaction = new Transaction();
        transaction.setOperation(Operation.INCOME);
        transaction.setTransactionAmount(BigDecimal.valueOf(1000.00));
        transaction.setOriginUser(otherUser);
        transaction.setDestinationUser(null);
        transaction.setCategory(category);
        transactionRepository.save(transaction);

        mockMvc.perform(delete("/api/v1/transactions/" + transaction.getId()).header("Authorization", "Bearer " + token)).andDo(MockMvcResultHandlers.print()).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get transaction by id, it should return the transaction successfully")
    void getTransactionById_ShouldReturnTransaction_WhenRequestIsValid() throws Exception {
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("462354");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        String token = securityUtils.generateValidToken("emilio@gmail.com");

        Category category = new Category();
        category.setCategoryName("category1");
        category.setUser(user);
        categoryRepository.save(category);


        Transaction transaction = new Transaction();
        transaction.setOperation(Operation.INCOME);
        transaction.setTransactionAmount(BigDecimal.valueOf(1000.00));
        transaction.setOriginUser(user);
        transaction.setDestinationUser(null);
        transaction.setCategory(category);
        transactionRepository.save(transaction);

        mockMvc.perform(get("/api/v1/transactions/" + transaction.getId()).header("Authorization", "Bearer " + token)).andDo(MockMvcResultHandlers.print()).andExpect(status().isOk()).andExpect(jsonPath("$.transactionId").value(transaction.getId())).andExpect(jsonPath("$.amount").value(transaction.getTransactionAmount()));
    }

    @Test
    @DisplayName("Get transaction by id, it should return not found because transaction is from another user")
    void getTransactionById_ShouldReturnNotFound_WhenRequestIsFromAnotherUser() throws Exception {
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("462354");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        User otherUser = new User();
        otherUser.setName("javier");
        otherUser.setEmail("javier@gmail.com");
        otherUser.setPassword("654321");
        otherUser.setCurrency(Currency.EUR);
        otherUser.setStatus(Status.ACTIVE);
        userRepository.save(otherUser);

        String token = securityUtils.generateValidToken("emilio@gmail.com");

        Category category = new Category();
        category.setCategoryName("category1");
        category.setUser(otherUser);
        categoryRepository.save(category);



        Transaction transaction = new Transaction();
        transaction.setOperation(Operation.INCOME);
        transaction.setTransactionAmount(BigDecimal.valueOf(1000.00));
        transaction.setOriginUser(otherUser);
        transaction.setDestinationUser(null);
        transaction.setCategory(category);
        transactionRepository.save(transaction);

        mockMvc.perform(get("/api/v1/transactions/" + transaction.getId()).header("Authorization", "Bearer " + token)).andDo(MockMvcResultHandlers.print()).andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Update transaction Category, it should return updated Successfully")
    void updateTransactionCategory_ShouldReturnUpdated_WhenRequestIsValid() throws Exception {
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("462354");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        String token = securityUtils.generateValidToken("emilio@gmail.com");

        Category category = new Category();
        category.setCategoryName("category1");
        category.setUser(user);
        categoryRepository.save(category);


        Transaction transaction = new Transaction();
        transaction.setOperation(Operation.INCOME);
        transaction.setTransactionAmount(BigDecimal.valueOf(1000.00));
        transaction.setOriginUser(user);
        transaction.setDestinationUser(null);
        transaction.setCategory(category);
        transactionRepository.save(transaction);

        Category category2 = new Category();
        category2.setCategoryName("category2");
        category2.setUser(user);
        categoryRepository.save(category2);

        mockMvc.perform(put("/api/v1/transactions/update-category/{transactionId}/{categoryId}" ,transaction.getId(),category2.getId()).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(category2))).andDo(MockMvcResultHandlers.print()).andExpect(status().isOk());
        Transaction updatedTransaction = transactionRepository.findById(transaction.getId()).orElseThrow();


        assertEquals(updatedTransaction.getCategory().getCategoryName(), category2.getCategoryName());

    }

    @Test
    @DisplayName("Update Transaction category, it should return not found because the transaction is not from the user")
    void updateTransactionCategory_ShouldReturnNotFound_WhenRequestIsFromAnotherUser() throws Exception {
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("462354");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        Category otherCategory = new Category();
        otherCategory.setCategoryName("category2");
        otherCategory.setUser(user);
        categoryRepository.save(otherCategory);

        User otherUser = new User();
        otherUser.setName("javier");
        otherUser.setEmail("javier@gmail.com");
        otherUser.setPassword("654321");
        otherUser.setCurrency(Currency.EUR);
        otherUser.setStatus(Status.ACTIVE);
        userRepository.save(otherUser);

        String token = securityUtils.generateValidToken("emilio@gmail.com");

        Category category = new Category();
        category.setCategoryName("category1");
        category.setUser(otherUser);
        categoryRepository.save(category);


        Transaction transaction = new Transaction();
        transaction.setOperation(Operation.INCOME);
        transaction.setTransactionAmount(BigDecimal.valueOf(1000.00));
        transaction.setOriginUser(otherUser);
        transaction.setDestinationUser(null);
        transaction.setCategory(category);
        transactionRepository.save(transaction);

        mockMvc.perform(put("/api/v1/transactions/update-category/{transactionId}/{categoryId}" ,transaction.getId(),otherCategory.getId()).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(otherCategory))).andDo(MockMvcResultHandlers.print()).andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Update transaction category, it should return not found because the category was not found")
    void updateTransactionCategory_ShouldReturnNotFound_WhenCategoryWasNotFound() throws Exception {
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("462354");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        String token = securityUtils.generateValidToken("emilio@gmail.com");

        Category category = new Category();
        category.setCategoryName("category1");
        category.setUser(user);
        categoryRepository.save(category);


        Transaction transaction = new Transaction();
        transaction.setOperation(Operation.INCOME);
        transaction.setTransactionAmount(BigDecimal.valueOf(1000.00));
        transaction.setOriginUser(user);
        transaction.setDestinationUser(null);
        transaction.setCategory(category);
        transactionRepository.save(transaction);

        mockMvc.perform(put("/api/v1/transactions/update-category/{transactionId}/{categoryId}" ,transaction.getId(),1000000000000000000L).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(category))).andDo(MockMvcResultHandlers.print()).andExpect(status().isNotFound());
        Transaction updatedTransaction = transactionRepository.findById(transaction.getId()).orElseThrow();
        assertEquals(updatedTransaction.getCategory().getCategoryName(), category.getCategoryName());

    }

    @Test
    @DisplayName("Get monthly stats test, should sum only income for the current user")
    void getMonthlyStats_ShouldReturnIncomeOnly_WhenRequestIsValid() throws Exception {
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("462354");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        Category category = new Category();
        category.setCategoryName("category2");
        category.setUser(user);
        categoryRepository.save(category);

        User otherUser = new User();
        otherUser.setName("javier");
        otherUser.setEmail("javier@gmail.com");
        otherUser.setPassword("654321");
        otherUser.setCurrency(Currency.EUR);
        otherUser.setStatus(Status.ACTIVE);
        userRepository.save(otherUser);

        String token = securityUtils.generateValidToken("emilio@gmail.com");

        Category otherCategory = new Category();
        otherCategory.setCategoryName("category1");
        otherCategory.setUser(otherUser);
        categoryRepository.save(otherCategory);

        Transaction transaction = new Transaction();
        transaction.setOperation(Operation.INCOME);
        transaction.setTransactionAmount(BigDecimal.valueOf(1000.00));
        transaction.setOriginUser(user);
        transaction.setDestinationUser(null);
        transaction.setCategory(category);
        transactionRepository.save(transaction);

        Transaction transaction2 = new Transaction();
        transaction2.setOperation(Operation.INCOME);
        transaction2.setTransactionAmount(BigDecimal.valueOf(5000.00));
        transaction2.setOriginUser(user);
        transaction2.setDestinationUser(null);
        transaction2.setCategory(category);
        transactionRepository.save(transaction2);
        transaction2.setTransactionTime(LocalDateTime.now().minusMonths(2));
        transactionRepository.save(transaction2);

        String sql = "UPDATE transactions SET transaction_time =? WHERE id = ?";
        jdbcTemplate.update(sql, LocalDateTime.now().minusMonths(2), transaction2.getId());


        Transaction transaction3 = new Transaction();
        transaction3.setOperation(Operation.INCOME);
        transaction3.setTransactionAmount(BigDecimal.valueOf(1200.00));
        transaction3.setOriginUser(otherUser);
        transaction3.setDestinationUser(null);
        transaction3.setCategory(otherCategory);
        transactionRepository.save(transaction3);

        Transaction transaction4 = new Transaction();
        transaction4.setOperation(Operation.EXPENSE);
        transaction4.setTransactionAmount(BigDecimal.valueOf(22000.00));
        transaction4.setOriginUser(user);
        transaction4.setDestinationUser(null);
        transaction4.setCategory(category);
        transactionRepository.save(transaction4);

       mockMvc.perform(get("/api/v1/transactions/monthly-stats").param("operation", "INCOME").header("Authorization", "Bearer " + token)).andDo(MockMvcResultHandlers.print()).andExpect(status().isOk()).andExpect(content().string("1000.00"));

    }

    @Test
    @DisplayName("Get monthly stats test, should sum only expense for the current user")
    void getMonthlyStats_ShouldReturnExpenseOnly_WhenRequestIsValid() throws Exception {
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("462354");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        Category category = new Category();
        category.setCategoryName("category2");
        category.setUser(user);
        categoryRepository.save(category);

        User otherUser = new User();
        otherUser.setName("javier");
        otherUser.setEmail("javier@gmail.com");
        otherUser.setPassword("654321");
        otherUser.setCurrency(Currency.EUR);
        otherUser.setStatus(Status.ACTIVE);
        userRepository.save(otherUser);

        String token = securityUtils.generateValidToken("emilio@gmail.com");

        Category otherCategory = new Category();
        otherCategory.setCategoryName("category1");
        otherCategory.setUser(otherUser);
        categoryRepository.save(otherCategory);

        Transaction transaction = new Transaction();
        transaction.setOperation(Operation.EXPENSE);
        transaction.setTransactionAmount(BigDecimal.valueOf(1340.00));
        transaction.setOriginUser(user);
        transaction.setDestinationUser(null);
        transaction.setCategory(category);
        transactionRepository.save(transaction);

        Transaction transaction2 = new Transaction();
        transaction2.setOperation(Operation.EXPENSE);
        transaction2.setTransactionAmount(BigDecimal.valueOf(5000.00));
        transaction2.setOriginUser(user);
        transaction2.setDestinationUser(null);
        transaction2.setCategory(category);
        transactionRepository.save(transaction2);
        transaction2.setTransactionTime(LocalDateTime.now().minusMonths(2));
        transactionRepository.save(transaction2);

        String sql = "UPDATE transactions SET transaction_time =? WHERE id = ?";
        jdbcTemplate.update(sql, LocalDateTime.now().minusMonths(2), transaction2.getId());


        Transaction transaction3 = new Transaction();
        transaction3.setOperation(Operation.EXPENSE);
        transaction3.setTransactionAmount(BigDecimal.valueOf(1200.00));
        transaction3.setOriginUser(otherUser);
        transaction3.setDestinationUser(null);
        transaction3.setCategory(otherCategory);
        transactionRepository.save(transaction3);

        Transaction transaction4 = new Transaction();
        transaction4.setOperation(Operation.INCOME);
        transaction4.setTransactionAmount(BigDecimal.valueOf(22000.00));
        transaction4.setOriginUser(user);
        transaction4.setDestinationUser(null);
        transaction4.setCategory(category);
        transactionRepository.save(transaction4);

        Transaction transaction5 = new Transaction();
        transaction5.setOperation(Operation.TRANSFER);
        transaction5.setTransactionAmount(BigDecimal.valueOf(2000.00));
        transaction5.setOriginUser(user);
        transaction5.setDestinationUser(otherUser);
        transaction5.setCategory(category);
        transactionRepository.save(transaction5);

        mockMvc.perform(get("/api/v1/transactions/monthly-stats").param("operation", "EXPENSE").header("Authorization", "Bearer " + token)).andDo(MockMvcResultHandlers.print()).andExpect(status().isOk()).andExpect(content().string("1340.00"));

    }

    @Test
    @DisplayName("Get monthly stats test, should sum only transfer for the current user")
    void getMonthlyStats_ShouldReturnTransferOnly_WhenRequestIsValid() throws Exception {
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("462354");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        Category category = new Category();
        category.setCategoryName("category2");
        category.setUser(user);
        categoryRepository.save(category);

        User otherUser = new User();
        otherUser.setName("javier");
        otherUser.setEmail("javier@gmail.com");
        otherUser.setPassword("654321");
        otherUser.setCurrency(Currency.EUR);
        otherUser.setStatus(Status.ACTIVE);
        userRepository.save(otherUser);

        String token = securityUtils.generateValidToken("emilio@gmail.com");

        Category otherCategory = new Category();
        otherCategory.setCategoryName("category1");
        otherCategory.setUser(otherUser);
        categoryRepository.save(otherCategory);

        Transaction transaction = new Transaction();
        transaction.setOperation(Operation.TRANSFER);
        transaction.setTransactionAmount(BigDecimal.valueOf(2240.00));
        transaction.setOriginUser(user);
        transaction.setDestinationUser(null);
        transaction.setCategory(category);
        transactionRepository.save(transaction);

        Transaction transaction2 = new Transaction();
        transaction2.setOperation(Operation.TRANSFER);
        transaction2.setTransactionAmount(BigDecimal.valueOf(5000.00));
        transaction2.setOriginUser(user);
        transaction2.setDestinationUser(null);
        transaction2.setCategory(category);
        transactionRepository.save(transaction2);
        transaction2.setTransactionTime(LocalDateTime.now().minusMonths(2));
        transactionRepository.save(transaction2);

        String sql = "UPDATE transactions SET transaction_time =? WHERE id = ?";
        jdbcTemplate.update(sql, LocalDateTime.now().minusMonths(2), transaction2.getId());


        Transaction transaction3 = new Transaction();
        transaction3.setOperation(Operation.TRANSFER);
        transaction3.setTransactionAmount(BigDecimal.valueOf(1200.00));
        transaction3.setOriginUser(otherUser);
        transaction3.setDestinationUser(null);
        transaction3.setCategory(otherCategory);
        transactionRepository.save(transaction3);

        Transaction transaction4 = new Transaction();
        transaction4.setOperation(Operation.EXPENSE);
        transaction4.setTransactionAmount(BigDecimal.valueOf(22000.00));
        transaction4.setOriginUser(user);
        transaction4.setDestinationUser(null);
        transaction4.setCategory(category);
        transactionRepository.save(transaction4);

        Transaction transaction5 = new Transaction();
        transaction5.setOperation(Operation.INCOME);
        transaction5.setTransactionAmount(BigDecimal.valueOf(2000.00));
        transaction5.setOriginUser(user);
        transaction5.setDestinationUser(otherUser);
        transaction5.setCategory(category);
        transactionRepository.save(transaction5);

        mockMvc.perform(get("/api/v1/transactions/monthly-stats").param("operation", "TRANSFER").header("Authorization", "Bearer " + token)).andDo(MockMvcResultHandlers.print()).andExpect(status().isOk()).andExpect(content().string("2240.00"));

    }


}
