package com.example.moneymissint.service;


import com.example.moneymissint.DTO.UserRequest;
import com.example.moneymissint.DTO.UserResponse;
import com.example.moneymissint.model.User;
import com.example.moneymissint.repository.UserRepository;
import com.example.moneymissint.roles.Currency;
import com.example.moneymissint.roles.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;


    @InjectMocks
    private UserService userService;



    @Test
    @DisplayName("Create User Test, it should create user when email is unique")
    void createUserTest() {

        final String name = "Emilio";
        final String email = "emilio@gmail.com";
        final String password = "emiliorevueltas";
        final Currency currency = Currency.EUR;
        final Status status = Status.ACTIVE;

        UserRequest userRequest = new UserRequest(name, email, password, currency, status);

        when(passwordEncoder.encode(userRequest.password())).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("Emilio");
        savedUser.setEmail("emilio@gmail.com");
        savedUser.setCurrency(Currency.EUR);
        savedUser.setStatus(Status.ACTIVE);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);


        when(userRepository.existsByEmail(email)).thenReturn(false);

        UserResponse userResponse = userService.createUser(userRequest);

        assertThat(userResponse).isNotNull();
        assertThat(userResponse.userId()).isEqualTo(1L);

        verify(userRepository, times(1)).save(any(User.class));






    }

}
