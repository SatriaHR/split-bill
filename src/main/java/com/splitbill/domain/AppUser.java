package com.splitbill.domain;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "app_users", uniqueConstraints = @UniqueConstraint(name = "uk_app_user_username", columnNames = "username"))
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100) private String username;
    @Column(nullable = false) private String passwordHash;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal balance = BigDecimal.ZERO;
    @Column(length = 100, unique = true) private String loginToken;
    private Instant loginTokenExpiration;

    protected AppUser() { }
    public AppUser(String username, String passwordHash) { this.username = username; this.passwordHash = passwordHash; }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public String getLoginToken() { return loginToken; }
    public void setLoginToken(String loginToken) { this.loginToken = loginToken; }
    public Instant getLoginTokenExpiration() { return loginTokenExpiration; }
    public void setLoginTokenExpiration(Instant loginTokenExpiration) { this.loginTokenExpiration = loginTokenExpiration; }
}
