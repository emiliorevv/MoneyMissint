package com.example.moneymissint.service;
import com.example.moneymissint.model.User;
import com.example.moneymissint.repository.UserRepository;
import com.example.moneymissint.roles.Currency;
import com.example.moneymissint.roles.Status;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;


@Service
@Transactional
@RequiredArgsConstructor
public class
UserService {

    private final UserRepository userRepository;


    public User getUserOrThrow(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public User createUser(User user) {
        user.setEmail(user.getEmail().toLowerCase());
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists, please try again with a different email");
        } else {
            user.setStatus(Status.ACTIVE);
        }
        
        return userRepository.save(user);
    }

    public User updateName(Long userId, String name) {
        User existingUser = getUserOrThrow(userId);

        if (existingUser.getStatus() != Status.ACTIVE) {
            throw new IllegalStateException("User is not active");
        } else {
            existingUser.setName(name);
        }

        return userRepository.save(existingUser);
    }

    public User updatePassword(Long userId, String password) {
        User existingUser = getUserOrThrow(userId);

        if (existingUser.getStatus() != Status.ACTIVE) {
            throw new IllegalStateException("User is not active");
        } else {
            existingUser.setPassword(password);
        }

        return userRepository.save(existingUser);
    }

    public User updateEmail(Long userId, String email) {

        User existingUser = getUserOrThrow(userId);

        if (existingUser.getStatus() != Status.ACTIVE) {
            throw new IllegalStateException("User is not active");
        }

         String normalizedEmail = existingUser.getEmail().toLowerCase();

        if (existingUser.getEmail().equalsIgnoreCase(email)) {
            return existingUser;
        }
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already exists");
        }else {
            existingUser.setEmail(normalizedEmail);
        }
        return userRepository.save(existingUser);
    }

    public User deactivateUser(Long userId) {
        User existingUser = getUserOrThrow(userId);

        if (existingUser.getStatus() != Status.ACTIVE){
            throw new IllegalStateException("User is already inactive");
        }

        existingUser.setStatus(Status.INACTIVE);

        return userRepository.save(existingUser);
    }

    public User activateUser(Long userId) {
        User existingUser = getUserOrThrow(userId);

        if (existingUser.getStatus() != Status.INACTIVE){
            throw new IllegalStateException("User is already active");
        }

        existingUser.setStatus(Status.ACTIVE);

        return userRepository.save(existingUser);

    }

    public User changeCurrency(Long userId, Currency currency) {
        User existingUser = getUserOrThrow(userId);

        if (existingUser.getStatus() != Status.ACTIVE) {
            throw new IllegalStateException("Current status is not ACTIVE");
        }

        existingUser.setCurrency(currency);

        return userRepository.save(existingUser);
    }



}
