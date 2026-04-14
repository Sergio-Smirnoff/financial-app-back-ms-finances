-- Scheduler queries: loans filtered by active + payment date
CREATE INDEX IF NOT EXISTS idx_loans_active_next_payment
    ON finances.loans (active, next_payment_date)
    WHERE active = true;

-- Scheduler queries: card_expenses filtered by active + due date
CREATE INDEX IF NOT EXISTS idx_card_expenses_active_next_due
    ON finances.card_expenses (active, next_due_date)
    WHERE active = true;

-- User-scoped queries
CREATE INDEX IF NOT EXISTS idx_transactions_user_id
    ON finances.transactions (user_id);

CREATE INDEX IF NOT EXISTS idx_transactions_user_date
    ON finances.transactions (user_id, date DESC);

CREATE INDEX IF NOT EXISTS idx_loan_installments_due_paid
    ON finances.loan_installments (due_date, paid)
    WHERE paid = false;

CREATE INDEX IF NOT EXISTS idx_loan_installments_loan_id
    ON finances.loan_installments (loan_id);

CREATE INDEX IF NOT EXISTS idx_card_expenses_user_id
    ON finances.card_expenses (user_id);
