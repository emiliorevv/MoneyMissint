package com.example.moneymissint.utils;

import com.example.moneymissint.model.User;
import com.example.moneymissint.roles.Status;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.mockito.Mockito.when;

@Component
public class SecurityUtils {

    @Value("${application.security.jwt.secret_key}")
    private String secretKey;

    private String buildToken(String email, long expirationTime){
        return Jwts.builder().subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+expirationTime))
                .signWith(getSignInKey()).compact();
    }

    private SecretKey getSignInKey () {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateValidToken(String email){
        return buildToken(email, 1000 * 60 * 60);
    }

    public String generateExpiredToken(String email){
        return buildToken(email, -1000 * 60 * 60);
    }


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
