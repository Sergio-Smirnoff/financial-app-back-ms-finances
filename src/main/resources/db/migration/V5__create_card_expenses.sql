CREATE TABLE finances.card_expenses
(
    id                     BIGSERIAL     PRIMARY KEY,
    user_id                BIGINT        NOT NULL,
    card_id                BIGINT        NOT NULL,
    description            VARCHAR(255)  NOT NULL,
    total_amount           NUMERIC(15,2) NOT NULL,
    currency               VARCHAR(3)    NOT NULL,
    total_installments     INT           NOT NULL,
    remaining_installments INT           NOT NULL,
    installment_amount     NUMERIC(15,2) NOT NULL,
    next_due_date          DATE          NOT NULL,
    active                 BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at             TIMESTAMP     NOT NULL,
    updated_at             TIMESTAMP     NOT NULL
);

CREATE INDEX idx_card_expenses_user_id ON finances.card_expenses (user_id);
CREATE INDEX idx_card_expenses_card_id ON finances.card_expenses (card_id);
CREATE INDEX idx_card_expenses_active ON finances.card_expenses (active);
CREATE INDEX idx_card_expenses_next_due_date ON finances.card_expenses (next_due_date);
