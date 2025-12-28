package com.example.moneymissint.service;

import com.example.moneymissint.DTO.UserRequest;
import com.example.moneymissint.DTO.UserResponse;
import com.example.moneymissint.model.User;
import com.example.moneymissint.repository.UserRepository;
import com.example.moneymissint.roles.Currency;
import com.example.moneymissint.roles.Status;
import com.example.moneymissint.utils.SecurityUtils;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.Optional;

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

    private User user;


    @BeforeEach
    void setUp() {
        this.user = SecurityUtils.mockedLoginUser(Status.ACTIVE, securityContext, authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Create User Test, it should create user without problems")
    void createUser_Success() {

        UserRequest userRequest = new UserRequest("Emilio","emilio@gmail.com", "123456", Currency.EUR, Status.ACTIVE);

        when(passwordEncoder.encode(userRequest.password())).thenReturn("encodedPassword");

        User userRequestSaved = new User();
        userRequestSaved.setId(1L);
        userRequestSaved.setName(userRequest.name());
        userRequestSaved.setEmail(userRequest.email().toLowerCase());
        userRequestSaved.setPassword("encodedPassword");
        userRequestSaved.setCurrency(userRequest.currency());
        userRequestSaved.setStatus(Status.ACTIVE);

        when(userRepository.existsByEmail(userRequestSaved.getEmail().toLowerCase())).thenReturn(false);

        when(userRepository.save(any(User.class))).thenReturn(userRequestSaved);


        UserResponse userResponse = userService.createUser(userRequest);

        assertThat(userResponse).isNotNull();
        assertThat(userResponse.userId()).isEqualTo(1L);
        assertThat(userResponse.name()).isEqualTo(userRequest.name());
        assertThat(userResponse.email()).isEqualTo(userRequest.email());
        assertThat(userResponse.currency()).isEqualTo(userRequest.currency());
        assertThat(userResponse.status()).isEqualTo(userRequest.status());



        verify(userRepository, times(1)).save(any(User.class));

        verify(passwordEncoder, times(1)).encode("123456");


    }

    @Test
    @DisplayName("Create User Test, test should throw exception because email is duplicated")
    void createUser_ThrowException_EmailDuplicated() {

        UserRequest userRequest = new UserRequest("Emilio", "emilio@gmail.com", "123456", Currency.EUR, Status.ACTIVE);

        User userRequestSaved = new User();
        userRequestSaved.setId(1L);
        userRequestSaved.setName(userRequest.name());
        userRequestSaved.setEmail(userRequest.email().toLowerCase());
        userRequestSaved.setPassword("encodedPassword");
        userRequestSaved.setCurrency(userRequest.currency());
        userRequestSaved.setStatus(Status.ACTIVE);

        when(userRepository.existsByEmail(userRequestSaved.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(userRequest)).isInstanceOf(IllegalArgumentException.class).hasMessage("Email already exists, please try again with a different email");

        verify(userRepository, never()).save(any(User.class));

    }


    @Test
    @DisplayName("Get User by Id, test should return User")
    void getUserById_Success() {


        User user = new User();
        user.setId(1L);
        user.setName("Emilio");
        user.setEmail("emilio@gmail.com");
        user.setPassword("encodedPassword");
        user.setCurrency(Currency.EUR);
        user.setStatus(Status.ACTIVE);

        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponse userResponse = userService.getUserById(userId);

        assertThat(userResponse).isNotNull();
        assertThat(userResponse.userId()).isEqualTo(userId);
        assertThat(userResponse.name()).isEqualTo("Emilio");
        assertThat(userResponse.email()).isEqualTo("emilio@gmail.com");
        assertThat(userResponse.currency()).isEqualTo(Currency.EUR);
        assertThat(userResponse.status()).isEqualTo(Status.ACTIVE);

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

        user.setName("original");
        user.setEmail("original@gmail.com");


        UserRequest updateRequest = new UserRequest("actualizado", "actualizado@gmail.com", "123456", Currency.EUR, Status.ACTIVE);



        when(passwordEncoder.encode(updateRequest.password())).thenReturn("encodedPassword");

        when(userRepository.existsByEmail(updateRequest.email().toLowerCase())).thenReturn(false);


        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse updatedUser = userService.updateUser(updateRequest);

        assertThat(updatedUser.userId()).isEqualTo(user.getId());

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.name()).isEqualTo("actualizado");
        assertThat(updatedUser.email()).isEqualTo("actualizado@gmail.com");
        assertThat(updatedUser.currency()).isEqualTo(Currency.EUR);
        assertThat(updatedUser.status()).isEqualTo(Status.ACTIVE);

        verify(userRepository, times(1)).save(user);

    }


    @Test
    @DisplayName("Update User, user is not active")
    void updateUser_ThrowException_UserNotActive(){

        user.setStatus(Status.INACTIVE);

        assertThatThrownBy(() -> userService.updateUser(new UserRequest("updated", "updated@gmail.com", "123456", Currency.EUR, Status.INACTIVE))).isInstanceOf(IllegalStateException.class).hasMessage("Current status is not ACTIVE");

        verify(userRepository, never()).save(any(User.class));

    }

    @Test
    @DisplayName("Update User, user cannot be updated because email is already in use")
    void updateUser_ThrowException_EmailAlreadyInUse(){

        String originalEmail = "original@gmail.com";
        user.setEmail(originalEmail);

        String occupiedEmail = "occupied@gmail.com";


        UserRequest updateRequest = new UserRequest("emilio", occupiedEmail, "123456", Currency.EUR, Status.ACTIVE);

        when(userRepository.existsByEmail(updateRequest.email().toLowerCase())).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(updateRequest)).isInstanceOf(IllegalArgumentException.class).hasMessage("Email already exists");
        verify(userRepository, never()).save(any(User.class));
    }

}
