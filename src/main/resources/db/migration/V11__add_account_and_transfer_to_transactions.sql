-- ═══════════════════════════════════════════════════════════════════
-- V11: Add account_id and transfer_group_id to transactions
-- ═══════════════════════════════════════════════════════════════════

ALTER TABLE finances.transactions
ADD COLUMN account_id BIGINT NULL,
ADD COLUMN transfer_group_id UUID NULL;

CREATE INDEX idx_transactions_account_id ON finances.transactions (account_id);
CREATE INDEX idx_transactions_transfer_group_id ON finances.transactions (transfer_group_id);
