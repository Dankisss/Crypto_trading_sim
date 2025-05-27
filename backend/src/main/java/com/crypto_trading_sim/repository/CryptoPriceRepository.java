package com.crypto_trading_sim.repository;

import com.crypto_trading_sim.entity.CryptoPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class CryptoPriceRepository {

    private static final Logger log = LoggerFactory.getLogger(CryptoPriceRepository.class);
    private final JdbcTemplate jdbcTemplate;

    public CryptoPriceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void deleteAll() {
        String deleteQuery = "DELETE FROM crypto_prices";

        int rowsAffected = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareCall(deleteQuery);

            return ps;
        });

        log.info("{} rows have been deleted from the crypto_users table", rowsAffected);
    }

    public void saveAll(List<CryptoPrice> cryptoPrices) {
        String insertSql = "INSERT INTO crypto_prices (symbol, price, updated_at) VALUES (?, ?, ?)";

        jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                CryptoPrice cryptoPrice = cryptoPrices.get(i);
                ps.setString(1, cryptoPrice.getSymbol());
                ps.setBigDecimal(2, cryptoPrice.getLastPrice());
                ps.setTimestamp(3, Timestamp.valueOf(cryptoPrice.getTimestamp()));
            }

            @Override
            public int getBatchSize() {
                return cryptoPrices.size();
            }
        });
    }
}
