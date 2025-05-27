CREATE TABLE transaction_history (
    id SERIAL PRIMARY KEY,
    user_id INTEGER,
    crypto_id INTEGER,
    transaction_type VARCHAR(4) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT transaction_user FOREIGN KEY (user_id) REFERENCES USERS(id),
    CONSTRAINT transaction_crypto FOREIGN KEY (crypto_id) REFERENCES CRYPTO_PRICES(id),
    CONSTRAINT chk_transaction_type CHECK (transaction_type IN ('BUY', 'SELL'))
);