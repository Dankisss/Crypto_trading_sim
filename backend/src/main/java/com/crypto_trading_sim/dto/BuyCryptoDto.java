package com.crypto_trading_sim.dto;

import java.math.BigDecimal;

public class BuyCryptoDto {
    String crypto;
    BigDecimal price;
    BigDecimal quantity;

    public BuyCryptoDto(String crypto, BigDecimal price, BigDecimal quantity) {
        this.crypto = crypto;
        this.price = price;
        this.quantity = quantity;
    }

    public String getCrypto() {
        return crypto;
    }

    public void setCrypto(String crypto) {
        this.crypto = crypto;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
}
