CREATE TABLE finances.loan_installments
(
    id                 BIGSERIAL     PRIMARY KEY,
    loan_id            BIGINT        NOT NULL REFERENCES finances.loans (id),
    installment_number INT           NOT NULL,
    amount             NUMERIC(15,2) NOT NULL,
    due_date           DATE          NOT NULL,
    paid               BOOLEAN       NOT NULL DEFAULT FALSE,
    paid_date          DATE,
    created_at         TIMESTAMP     NOT NULL,
    updated_at         TIMESTAMP     NOT NULL
);

CREATE INDEX idx_loan_installments_loan_id ON finances.loan_installments (loan_id);
CREATE INDEX idx_loan_installments_due_date ON finances.loan_installments (due_date);
CREATE INDEX idx_loan_installments_paid ON finances.loan_installments (paid);
