CREATE TABLE finances.card_expense_installments
(
    id                 BIGSERIAL     PRIMARY KEY,
    card_expense_id    BIGINT        NOT NULL REFERENCES finances.card_expenses (id),
    installment_number INT           NOT NULL,
    amount             NUMERIC(15,2) NOT NULL,
    due_date           DATE          NOT NULL,
    paid               BOOLEAN       NOT NULL DEFAULT FALSE,
    paid_date          DATE,
    created_at         TIMESTAMP     NOT NULL,
    updated_at         TIMESTAMP     NOT NULL
);

CREATE INDEX idx_card_expense_installments_card_expense_id ON finances.card_expense_installments (card_expense_id);
CREATE INDEX idx_card_expense_installments_due_date ON finances.card_expense_installments (due_date);
CREATE INDEX idx_card_expense_installments_paid ON finances.card_expense_installments (paid);
