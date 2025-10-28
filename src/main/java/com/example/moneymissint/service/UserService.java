
/**
 * Crear usuario
 * actualizar usuario
 * desactivar el usuario
 * Activar el usuario
 * Cambiar la currency
 */

package com.example.moneymissint.service;

import com.example.moneymissint.model.User;
import com.example.moneymissint.repository.UserRepository;
import com.example.moneymissint.roles.Status;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    public User createUser(User user) {
        if (StringUtils.hasText(user.getEmail()) && StringUtils.hasText(user.getPassword()) &&
                StringUtils.hasText(user.getName())) {
            return userRepository.save(user);
        } else {
            return null;
        }
    }

    public User updateUser(User user) {
        Optional<User> optionalUser = userRepository.findById(user.getId());
        if (optionalUser.isPresent() && optionalUser.equals(user)) {
            user.setEmail(user.getEmail().trim().toLowerCase());
            user.setPassword(user.getPassword());
            user.setName(user.getName());
            return userRepository.save(user);
        } else {
            return null;
        }
    }

    public User deactivateUser(User user) {
        Optional<User> optionalUser = userRepository.findById(user.getId());
        if(optionalUser.isPresent() && optionalUser.equals(user)&& optionalUser.get().getStatus() == Status.ACTIVE) {
            user.setStatus(Status.INACTIVE);
            return userRepository.save(user);
        }
        return null;
    }

    public User activateUser(User user) {
        Optional<User> optionalUser = userRepository.findById(user.getId());
        if(optionalUser.isPresent() && optionalUser.equals(user) && optionalUser.get().getStatus() == Status.INACTIVE) {
            user.setStatus(Status.ACTIVE);
            return userRepository.save(user);
        }
        return null;
    }

    public User changeCurrency(User user) {
        Optional<User> optionalUser = userRepository.findById(user.getId());
        if (optionalUser.isPresent() &&  optionalUser.equals(user) && optionalUser.get().getStatus() ==Status.ACTIVE){
            user.setCurrency(user.getCurrency());
            return userRepository.save(user);
        }
        return null;
    }



}
