package com.example.moneymissint.service;

import com.example.moneymissint.DTO.UserRequest;
import com.example.moneymissint.DTO.UserResponse;
import com.example.moneymissint.model.User;
import com.example.moneymissint.repository.UserRepository;
import com.example.moneymissint.roles.Currency;
import com.example.moneymissint.roles.Status;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.Mockito.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;



    @InjectMocks
    private UserService userService;

    final static String name = "Emilio";
    final static String email = "emilio@gmail.com";
    final static String password = "emiliorevueltas";
    final static Currency currency = Currency.EUR;
    final static Status status = Status.ACTIVE;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockedLoginUser(Status status){
        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setStatus(status);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        SecurityContextHolder.setContext(securityContext);
    }


    @Test
    @DisplayName("Create User Test, it should create user without problems")
    void createUser_Success() {

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

    @Test
    @DisplayName("Create User Test, test should throw exception because email is duplicated")
    void createUser_ThrowException_EmailDuplicated() {

        UserRequest userRequest = new UserRequest(name, email, password, currency, status);

        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(userRequest)).isInstanceOf(IllegalArgumentException.class).hasMessage("Email already exists, please try again with a different email");

        verify(userRepository, never()).save(any(User.class));

    }


    @Test
    @DisplayName("Get User by Id, test should return User")
    void getUserById_Success() {
        User user = new User();
        Long userId = 1L;
        user.setId(userId);
        user.setName("Emilio");
        user.setEmail("emilio.rev@gmail.com");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));

        UserResponse userResponse = userService.getUserById(userId);

        assertThat(userResponse).isNotNull();
        assertThat(userResponse.userId()).isEqualTo(userId);
        assertThat(userResponse.name()).isEqualTo(user.getName());
        assertThat(userResponse.email()).isEqualTo(user.getEmail());
        assertThat(userResponse.currency()).isEqualTo(user.getCurrency());
        assertThat(userResponse.status()).isEqualTo(user.getStatus());

        verify(userRepository, times(1)).findById(userId);


    }

    @Test
    @DisplayName("Get User by Id, test should throw exception because user not found")
    void getUserById_ThrowException_UserNotFound() {
        Long userId = 99L;

        when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId)).isInstanceOf(EntityNotFoundException.class).hasMessage("User not found");

        verify(userRepository, times(1)).findById(userId);

    }

    @Test
    @DisplayName("Update user, user should be updated correctly")
    void updateUser_Success(){

         mockedLoginUser(Status.ACTIVE);

        UserRequest updateRequest = new UserRequest(name, email, password, currency, status);

        when(passwordEncoder.encode(updateRequest.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse updatedUser = userService.updateUser(updateRequest);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.name()).isEqualTo(name);
        assertThat(updatedUser.email()).isEqualTo(email);
        assertThat(updatedUser.currency()).isEqualTo(currency);
        assertThat(updatedUser.status()).isEqualTo(status);

        verify(userRepository, times(1)).save(any(User.class));

    }



    @Test
    @DisplayName("Update User, user is not active")
    void updateUser_ThrowException_UserNotActive(){
        mockedLoginUser(Status.INACTIVE);

        assertThatThrownBy(() -> userService.updateUser(new UserRequest(name, email, password, currency, status))).isInstanceOf(IllegalStateException.class).hasMessage("Current status is not ACTIVE");

        verify(userRepository, never()).save(any(User.class));

    }

    @Test
    @DisplayName("Update User, user cannot be updated because email is already in use")
    void updateUser_ThrowException_EmailAlreadyInUse(){
        mockedLoginUser(Status.ACTIVE);

        String newEmail = "occupied@test.com";

        when(userRepository.existsByEmail(newEmail)).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(new UserRequest(name, newEmail, password, currency, status))).isInstanceOf(IllegalArgumentException.class).hasMessage("Email already exists");
        verify(userRepository, never()).save(any(User.class));
    }

}
