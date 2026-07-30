CREATE INDEX idx_transactions_user_date_id ON finances.transactions (user_id, date DESC, id DESC);
