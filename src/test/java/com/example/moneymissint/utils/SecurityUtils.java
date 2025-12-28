package com.example.moneymissint.utils;

import com.example.moneymissint.model.User;
import com.example.moneymissint.roles.Status;
import org.junit.jupiter.api.AfterEach;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.when;

public class SecurityUtils {

    public static User mockedLoginUser(Status status, SecurityContext securityContext, Authentication authentication){
        User user = new User();
        user.setId(1L);
        user.setStatus(status);
        Mockito.lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.lenient().when(authentication.getPrincipal()).thenReturn(user);
        SecurityContextHolder.setContext(securityContext);

        return user;
    }
}
