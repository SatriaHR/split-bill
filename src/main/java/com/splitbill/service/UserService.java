package com.splitbill.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.splitbill.domain.AppUser;
import com.splitbill.repository.AppUserRepository;
import com.splitbill.web.ApiException;
import com.splitbill.web.dto.Requests;

@Service
public class UserService {
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    public UserService(AppUserRepository users, PasswordEncoder passwordEncoder) { this.users = users; this.passwordEncoder = passwordEncoder; }

    @Transactional
    public AppUser signUp(Requests.SignUp request) {
        if (users.findByUsername(request.username()).isPresent()) throw new ApiException(HttpStatus.CONFLICT, "Username already exists");
        return users.save(new AppUser(request.username(), passwordEncoder.encode(request.password())));
    }
    
    @Transactional
    public AppUser signIn(Requests.SignIn request) {
        AppUser user = users.findByUsername(request.username()).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        user.setLoginToken(UUID.randomUUID().toString());
        user.setLoginTokenExpiration(Instant.now().plus(Duration.ofHours(24)));
        return user;
    }
    
    @Transactional(readOnly = true)
    public AppUser get(Long id) { return users.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found")); }
    
    @Transactional(readOnly = true)
    public Iterable<AppUser> all() { return users.findAll(); }
    
    @Transactional
    public AppUser topUp(Long id, BigDecimal amount) { AppUser user = locked(id); user.setBalance(user.getBalance().add(amount)); return user; }
    
    @Transactional
    public AppUser withdraw(Long id, BigDecimal amount) { AppUser user = locked(id); if (user.getBalance().compareTo(amount) < 0) throw new ApiException(HttpStatus.CONFLICT, "Insufficient balance"); user.setBalance(user.getBalance().subtract(amount)); return user; }
    
    @Transactional
    public void delete(Long id) { users.delete(get(id)); }
    
    public AppUser locked(Long id) { return users.findByIdForUpdate(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found")); }
}
