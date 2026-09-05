package com.splitbill.security;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.splitbill.domain.AppUser;
import com.splitbill.repository.AppUserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TokenFilter extends OncePerRequestFilter {
    private final AppUserRepository users;
    public TokenFilter(AppUserRepository users) { this.users = users; }
    
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            users.findByLoginTokenForUpdate(token).filter(u -> u.getLoginTokenExpiration() != null && u.getLoginTokenExpiration().isAfter(Instant.now())).ifPresent(this::authenticate);
        }
        chain.doFilter(request, response);
    }
    private void authenticate(AppUser user) { 
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of())); 
    }
}
