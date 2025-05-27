package com.crypto_trading_sim.controller;

import com.crypto_trading_sim.dto.UserDto;
import com.crypto_trading_sim.entity.User;
import com.crypto_trading_sim.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/app/v1/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserDto userDto) {
        return ResponseEntity.ok(userService.createUser(userDto));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable int userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("/balance/update/{userId}")
    public ResponseEntity<User> updateBalance(@PathVariable int userId, @RequestBody BigDecimal amount) {
        return ResponseEntity.ok(userService.updateBalance(userId, amount));
    }

    @PutMapping("/balance/reset/{userId}")
    public ResponseEntity<User> resetBalance(@PathVariable int userId) {
        log.info("Logger test");
        return ResponseEntity.ok(userService.resetBalance(userId));
    }


    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody UserDto userDto) {
        return null;
    }
}
