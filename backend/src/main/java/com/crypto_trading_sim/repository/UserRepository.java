package com.crypto_trading_sim.repository;

import com.crypto_trading_sim.dto.UserDto;
import com.crypto_trading_sim.entity.User;
import com.crypto_trading_sim.exception.UserCreationException;
import com.crypto_trading_sim.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.PreparedStatement;

@Repository
public class UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);
    private final JdbcTemplate jdbcTemplate;

    private static final BigDecimal RESET_BALANCE_AMOUNT = new BigDecimal("10000");

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /***
     *
     * @param userDto
     * @return the created user
     */
    public User createUser(UserDto userDto) {
        String createUserQuery = "INSERT INTO users(username, email, password_hash) VALUES (?, ?, ?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(createUserQuery, PreparedStatement.RETURN_GENERATED_KEYS);

            ps.setString(1, userDto.getUsername());
            ps.setString(2, userDto.getEmail());
            ps.setString(3, userDto.getPassword());

            return ps;
        }, keyHolder);

        int id;

        if (keyHolder.getKeys() != null && keyHolder.getKeys().containsKey("id")) {
            id = (Integer) keyHolder.getKeys().get("id");
        } else {
            throw new UserCreationException("Failed to retrieve generated ID.");
        }

        return new User(id, userDto.getUsername(), userDto.getEmail(), userDto.getPassword());

    }

    /**
     * Resets a user's balance to 10000 using the PreparedStatementCreator pattern
     * and returns the updated User object.
     *
     * @param userId The ID of the user to update.
     * @return The updated User object, or null if the user was not found.
     */
    @Transactional
    public User resetBalance(int userId) {
        String updateQuery = "UPDATE users SET balance = ? WHERE id = ?";

        User updatedUser = null;

        int rowsAffected = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(updateQuery);
            ps.setBigDecimal(1, RESET_BALANCE_AMOUNT);
            ps.setInt(2, userId);
            return ps;
        });

        if (rowsAffected > 0) {
                updatedUser = getUserById(userId);
        } else {
            UserRepository.log.warn("User with ID " + userId + " not found. No update performed.");
        }

        return updatedUser;
    }


    public User getUserById(int userId) {
        String selectQuery = "SELECT id, username, email, balance FROM users WHERE id = ?";

        User user;

        try {
            user = jdbcTemplate.queryForObject(selectQuery, (rs, rowNum) -> {
                User tempUser = new User();
                tempUser.setId(rs.getInt("id"));
                tempUser.setUsername(rs.getString("username"));
                tempUser.setEmail("email");
                tempUser.setBalance(rs.getBigDecimal("balance"));

                return tempUser;
            }, userId);

        } catch (EmptyResultDataAccessException e) {
            log.error("CRITICAL: User updated but could not be selected (ID: " + userId + "). Rolling back.");
            throw new IllegalStateException("Data inconsistency: User " + userId + " not found after update.", e);
        } catch (Exception e) {
            log.error("Error selecting user after update (ID: " + userId + "). Rolling back.");
            throw new RuntimeException("Error selecting user " + userId + " after update.", e); // Force rollback
        }

        return user;
    }

    public User updateBalance(int userId, BigDecimal amount) {
        String updateQuery = "UPDATE users SET balance = balance + ? WHERE id = ?";
        User updatedUser;
        int rowsAffected  = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(updateQuery);

            ps.setBigDecimal(1, amount);
            ps.setInt(2, userId);
            return ps;
        });

        if (rowsAffected > 0) {
            updatedUser = getUserById(userId);
        } else {
            throw new UserNotFoundException("User with id not found: " + userId);
        }

        return updatedUser;
    }

    public void updateBalance(int userId, String currency, BigDecimal amount) {
        String updateQuery = "UPDATE user_balances SET available = available + ? WHERE user_id = ? AND currency = ?";

        try {
            int updated = jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(updateQuery);
                ps.setBigDecimal(1, amount);
                ps.setInt(2, userId);
                ps.setString(3, currency);

                return ps;
            });

            if (updated == 0) {
                String insertQuery = "INSERT INTO user_balances (user_id, currency, available) VALUES (?, ?, ?)";

                jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(insertQuery);

                    ps.setInt(1, userId);
                    ps.setString(2, currency);
                    ps.setBigDecimal(3, amount);

                    return ps;
                });

            }
        } catch (DataAccessException ex) {
            log.error("Failed to access the required data: {}", ex.getMessage());
            throw new RuntimeException("Database error while executing transaction", ex);
        }

    }

    public void insertTransaction(int userId, String transactionType, String cryptoCurrency,
                                  BigDecimal amount, BigDecimal price) {
        try {
            String insertQuery = "INSERT INTO transaction_history (user_id, crypto_currency, amount, price, transaction_type) VALUES (?, ?, ?, ?, ?)";

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(insertQuery);

                ps.setInt(1, userId);
                ps.setString(2, cryptoCurrency);
                ps.setBigDecimal(3, amount);
                ps.setBigDecimal(4, price);
                ps.setString(5, transactionType);

                return ps;
            });
        } catch (DataAccessException ex) {

            log.error("Failed to insert transaction for userId {}: {}", userId, ex.getMessage());
            throw new RuntimeException("Database error while inserting transaction", ex);
        }
    }

}
