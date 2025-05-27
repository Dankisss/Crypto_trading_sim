CREATE TABLE user_balances (
user_id     INT,
currency    VARCHAR(10),
available   DECIMAL(38,18),
PRIMARY KEY (user_id, currency),
CONSTRAINT user_balances FOREIGN KEY (user_id) REFERENCES users(id)
);