package com.example.moneymissint.controller;

import com.example.moneymissint.DTO.UserRequest;
import com.example.moneymissint.model.User;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(properties = {"JWT_SECRET = ultramegasecretpasswordinHere2390481348139440582934324234567"})
@AutoConfigureMockMvc
@Transactional
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityUtils securityUtils;



    @Test
    @DisplayName("Create user test, it should return Created successfully")
    void createUser_ShouldReturnCreated_WhenRequestIsValid() throws Exception {

        UserRequest userRequest = new UserRequest("emilio", "emilio@gmail.com", "123456", Currency.EUR, Status.ACTIVE);

        mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(userRequest))).andExpect(status().isCreated()).andExpect(jsonPath("$.name").value(userRequest.name()));

        var savedUser = userRepository.findByEmail(userRequest.email()).orElseThrow();

        assertNotNull(savedUser);

        assertTrue(passwordEncoder.matches("123456", savedUser.getPassword()));

        assertTrue(userRepository.existsByEmail(userRequest.email()));


    }

    @Test
    @DisplayName("Create user test, it should return error because user email is already on use")
    void createUser_ThrowException_EmailAlreadyInUse() throws Exception {
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("462354");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        UserRequest userRequest = new UserRequest("javier", "emilio@gmail.com", "emilio123", Currency.EUR, Status.ACTIVE);

        mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(userRequest))).andExpect(status().isBadRequest()).andExpect(content().string(containsString("Email already exists")));


    }

    @Test
    @DisplayName("Create user test, it should return error because data is invalid ")
    void createUser_ThrowException_InvalidData() throws Exception {
        UserRequest invalidUserRequest = new UserRequest("", "", "213", Currency.EUR, Status.ACTIVE);

        mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).
                content(objectMapper.writeValueAsString(invalidUserRequest))).
                andExpect(status().isBadRequest()).andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.password").exists());

    }


    @Test
    @DisplayName("Get user by id, it should return the user correctly" )
    void getUserById_ShouldReturnUser_WhenRequestIsValid() throws Exception {
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("123456");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        String token = securityUtils.generateValidToken("emilio@gmail.com");

        mockMvc.perform(get("/api/v1/users/" + user.getId()).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value(user.getName())).andExpect(jsonPath("$.email").value("emilio@gmail.com"));

    }

    @Test
    @DisplayName("Get user by id, it should return not found error because user doesnt exist")
    void getUserById_ThrowException_UserNotFound() throws Exception {
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("123456");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        String token = securityUtils.generateValidToken("emilio@gmail.com");

        mockMvc.perform(get("/api/v1/users/23043424").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get user by id, it should return forbbiden because the token is expired")
    void getUserById_ThrowException_TokenExpired() throws Exception {
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("123456");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        String token = securityUtils.generateExpiredToken("emilio@gmail.com");

        mockMvc.perform(get("/api/v1/users/"+ user.getId()).header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andExpect(status().isForbidden());

    }
    @Test
    @DisplayName("Update user test, it should update the user correctly")
    void updateUser_ShouldReturnUpdated_WhenRequestIsValid() throws Exception {
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("123456");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        String token = securityUtils.generateValidToken("emilio@gmail.com");

        UserRequest updatedUser = new UserRequest("jose", "jose@gmail.com", "123456", Currency.USD, Status.ACTIVE);

        mockMvc.perform(put("/api/v1/users").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(updatedUser))).andExpect(status().isOk()).andExpect(jsonPath("$.name").value(updatedUser.name())).andExpect(jsonPath("$.email").value(updatedUser.email()));

        User userInDB = userRepository.findById(user.getId()).orElseThrow();

        assertEquals(updatedUser.name(), userInDB.getName());
        assertEquals(updatedUser.email(), userInDB.getEmail());
        assertEquals(updatedUser.currency(), userInDB.getCurrency());
        assertNotEquals(updatedUser.password(), userInDB.getPassword());
    }

    @Test
    @DisplayName("Update user test, it should return error because the email is from another user")
    void updateUser_ThrowException_EmailAlreadyInUse() throws Exception {
        User user = new User();
        user.setName("emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("123456");
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

        String token = securityUtils.generateValidToken("javier@gmail.com");

        UserRequest userRequest = new UserRequest("javier", "emilio@gmail.com", "123456", Currency.EUR, Status.ACTIVE);


        mockMvc.perform(put("/api/v1/users").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(userRequest))).andDo(MockMvcResultHandlers.print()).andExpect(status().isBadRequest());
    }





}
