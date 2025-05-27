package com.crypto_trading_sim.entity;

import lombok.AllArgsConstructor;

import java.math.BigDecimal;

public class User {

    private int id;

    private String username;

    private String email;

    private String password;

    private BigDecimal balance;

    public User() {
    }

    public User(int id, String password, String email, String username) {
        this.password = password;
        this.email = email;
        this.username = username;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
