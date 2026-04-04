CREATE TABLE finances.transactions
(
    id          BIGSERIAL     PRIMARY KEY,
    user_id     BIGINT        NOT NULL,
    type        VARCHAR(10)   NOT NULL,
    amount      NUMERIC(15,2) NOT NULL,
    currency    VARCHAR(3)    NOT NULL,
    category_id BIGINT        NOT NULL REFERENCES finances.categories (id),
    description VARCHAR(500),
    date        DATE          NOT NULL,
    created_at  TIMESTAMP     NOT NULL,
    updated_at  TIMESTAMP     NOT NULL
);

CREATE INDEX idx_transactions_user_id ON finances.transactions (user_id);
CREATE INDEX idx_transactions_category_id ON finances.transactions (category_id);
CREATE INDEX idx_transactions_date ON finances.transactions (date);
CREATE INDEX idx_transactions_currency ON finances.transactions (currency);
