package com.example.moneymissint.service;
import com.example.moneymissint.DTO.UserRequest;
import com.example.moneymissint.DTO.UserResponse;
import com.example.moneymissint.model.User;
import com.example.moneymissint.repository.UserRepository;
import com.example.moneymissint.roles.Status;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class
UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;


    public User getUserOrThrow(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));


    }



    public UserResponse createUser(UserRequest userRequest) {
        User user = new User();
        user.setName(userRequest.name());
        user.setEmail(userRequest.email().toLowerCase());
        String encodedPassword = passwordEncoder.encode(userRequest.password());
        user.setPassword(encodedPassword);
        user.setCurrency(userRequest.currency());


        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists, please try again with a different email");
        } else {
            user.setStatus(Status.ACTIVE);
        }

        User savedUser = userRepository.save(user);

        return new UserResponse(savedUser.getId(), savedUser.getName(), savedUser.getEmail(), savedUser.getCurrency(), savedUser.getStatus());
    }

    public UserResponse updateUser(UserRequest userRequest, Long userId){

        User existingUser = getUserOrThrow(userId);

        if (existingUser.getStatus() != Status.ACTIVE) {
            throw new IllegalStateException("Current status is not ACTIVE");
        }



        existingUser.setName(userRequest.name());
        String normalizedEmail = userRequest.email().toLowerCase();
        if(normalizedEmail.equals(existingUser.getEmail())){
            existingUser.setEmail(normalizedEmail);
        } else if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already exists");
        } else {
            existingUser.setEmail(normalizedEmail);
        }

        String encodedPassword = passwordEncoder.encode(userRequest.password());
        existingUser.setPassword(encodedPassword);
        existingUser.setCurrency(userRequest.currency());

        User savedUser = userRepository.save(existingUser);
        return new UserResponse(savedUser.getId(), savedUser.getName(), savedUser.getEmail(), savedUser.getCurrency(), savedUser.getStatus());
    }


}
