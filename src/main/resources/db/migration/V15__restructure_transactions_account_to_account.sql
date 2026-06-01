-- ═══════════════════════════════════════════════════════════════════
-- V15: Account-to-account transaction model.
-- transactions is empty (V12 wiped it), so this is a data-free restructure:
-- add from_cbu/to_cbu (both required), drop the legacy type/account_id/transfer_group_id.
-- amount stays a positive magnitude (NUMERIC(15,2)); direction is derived from ownership.
-- ═══════════════════════════════════════════════════════════════════

ALTER TABLE finances.transactions DROP COLUMN IF EXISTS transfer_group_id;
ALTER TABLE finances.transactions DROP COLUMN IF EXISTS account_id;
ALTER TABLE finances.transactions DROP COLUMN IF EXISTS type;

ALTER TABLE finances.transactions ADD COLUMN from_cbu VARCHAR(22) NOT NULL;
ALTER TABLE finances.transactions ADD COLUMN to_cbu   VARCHAR(22) NOT NULL;

CREATE INDEX idx_transactions_from_cbu ON finances.transactions (from_cbu);
CREATE INDEX idx_transactions_to_cbu   ON finances.transactions (to_cbu);
