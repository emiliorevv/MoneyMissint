package com.example.moneymissint.controller;
import com.example.moneymissint.DTO.AuthRequest;
import com.example.moneymissint.DTO.AuthResponse;
import com.example.moneymissint.DTO.UserRequest;
import com.example.moneymissint.DTO.UserResponse;
import com.example.moneymissint.service.AuthService;
import com.example.moneymissint.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated

@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    private final AuthService AuthService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid UserRequest userRequest){
        UserResponse userResponse = userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);

    }

    @PutMapping("/{userId}")
    public ResponseEntity <UserResponse> updateUser(@RequestBody @Valid UserRequest userRequest, @PathVariable Long userId){
        UserResponse userResponse = userService.updateUser(userRequest, userId);
        return ResponseEntity.status(HttpStatus.OK).body(userResponse);

    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest){
        AuthResponse authResponse = AuthService.login(authRequest);
        return ResponseEntity.status(HttpStatus.OK).body(authResponse);

    }




}