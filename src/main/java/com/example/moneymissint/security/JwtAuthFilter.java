package com.example.moneymissint.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final UserDetailsService userDetailsService;




    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException{

        final String authorizationHeader = request.getHeader("Authorization");
        final String token;
        String userEmail = null;

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        token = authorizationHeader.substring(7);

        try{
            userEmail = jwtService.extractUsername(token);
        } catch (ExpiredJwtException | MalformedJwtException e){
            System.out.println("Token invalid: " + e.getMessage());
        }



        if (userEmail != null  && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(token, userDetails)){

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails,null, userDetails.getAuthorities());

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
