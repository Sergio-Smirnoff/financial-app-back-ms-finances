CREATE TABLE finances.loans
(
    id                 BIGSERIAL     PRIMARY KEY,
    user_id            BIGINT        NOT NULL,
    description        VARCHAR(255)  NOT NULL,
    entity             VARCHAR(100),
    total_amount       NUMERIC(15,2) NOT NULL,
    currency           VARCHAR(3)    NOT NULL,
    total_installments INT           NOT NULL,
    paid_installments  INT           NOT NULL DEFAULT 0,
    next_payment_date  DATE,
    installment_amount NUMERIC(15,2) NOT NULL,
    active             BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP     NOT NULL,
    updated_at         TIMESTAMP     NOT NULL
);

CREATE INDEX idx_loans_user_id ON finances.loans (user_id);
CREATE INDEX idx_loans_active ON finances.loans (active);
CREATE INDEX idx_loans_currency ON finances.loans (currency);
