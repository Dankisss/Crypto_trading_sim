package com.crypto_trading_sim.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import javax.xml.crypto.Data;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);
    @Value("${app.db.driver}")
    private String DBDriver;

    @Value("${app.db.url}")
    private String DBUrl;

    @Value("${app.db.user}")
    private String DBUser;

    @Value("${app.db.password}")
    private String DBPassword;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        log.info(DBUrl);
        dataSource.setDriverClassName(DBDriver);
        dataSource.setUrl(DBUrl);
        dataSource.setUsername(DBUser);
        dataSource.setPassword(DBPassword);

        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
