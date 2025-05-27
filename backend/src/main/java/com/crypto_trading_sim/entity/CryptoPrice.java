package com.crypto_trading_sim.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CryptoPrice {

    private String symbol;
    private BigDecimal lastPrice;
    private LocalDateTime timestamp;

    public CryptoPrice(String symbol, BigDecimal lastPrice, LocalDateTime timestamp) {
        this.symbol = symbol;
        this.lastPrice = lastPrice;
        this.timestamp = timestamp;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(BigDecimal lastPrice) {
        this.lastPrice = lastPrice;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

}
