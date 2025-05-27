package com.crypto_trading_sim.service;

import com.crypto_trading_sim.dto.BuyCryptoDto;
import com.crypto_trading_sim.dto.UserDto;
import com.crypto_trading_sim.entity.User;
import com.crypto_trading_sim.exception.InsufficientFundsException;
import com.crypto_trading_sim.exception.UserCreationException;
import com.crypto_trading_sim.repository.UserRepository;
import org.postgresql.util.PSQLException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class UserService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(UserDto userDto) {
        try {
            UserDto toBeCreated = new UserDto(
                    userDto.getUsername(),
                    userDto.getEmail(),
                    passwordEncoder.encode(userDto.getPassword())
            );

            return userRepository.createUser(toBeCreated);
        } catch (DataIntegrityViolationException e) {
            Throwable rootCause = e.getRootCause();

            if (rootCause instanceof PSQLException psqlException) {

                if ("23505".equals(psqlException.getSQLState())) {
                    String serverErrorMessage = psqlException.getServerErrorMessage() != null ?
                            psqlException.getServerErrorMessage().getMessage() :
                            "Server error while insertion";

                    if (serverErrorMessage.contains("users_email_key")) {
                        throw new UserCreationException("Error: The email address '" + userDto.getEmail() + "' is already in use.");

                    } else if (serverErrorMessage.contains("users_username_key")) {
                        throw new UserCreationException("Error: The username '" + userDto.getUsername() + "' is already taken.");
                    } else {
                        throw new UserCreationException("Error: A unique field value (e.g., email or username) already exists.");
                    }
                }
            }

            throw new UserCreationException("Error creating user due to a data conflict. Please check your input.", e);
        }
    }

    public User getUserById(int id) {
        return userRepository.getUserById(id);
    }

    public User resetBalance(int id) {
        return userRepository.resetBalance(id);
    }

    public User updateBalance(int userId, BigDecimal amount) {
        return userRepository.updateBalance(userId, amount);
    }

    @Transactional
    public void buyCrypto(int userId, BuyCryptoDto buyCryptoDto) {
        String crypto = buyCryptoDto.getCrypto();
        BigDecimal quantity = buyCryptoDto.getQuantity();
        BigDecimal price = buyCryptoDto.getPrice();

        BigDecimal totalCost = quantity.multiply(price);

//        //TODO: get the fiat money from the users table and check if the user can buy the crypto
//        BigDecimal usdAvailable = getAvailableBalance(userId);
//        if (usdAvailable.compareTo(totalCost) < 0) {
//            throw new InsufficientFundsException("Not enough balance");
//        }
//
//        userRepository.updateBalance(userId, totalCost.multiply(BigDecimal.valueOf(-1)));
//
//        userRepository.updateBalance(userId, crypto, quantity);
//
//        insertTransaction(userId, "BUY", crypto, quantity, price, totalCost, null);
    }
}
