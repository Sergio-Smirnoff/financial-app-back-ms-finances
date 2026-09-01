ALTER TABLE finances.transactions
    ADD COLUMN payment_method VARCHAR(20),
    ADD COLUMN note VARCHAR(500);
