-- ═══════════════════════════════════════════════════════════════════
-- V10: Drop legacy loans (moved to ms-banks)
-- ═══════════════════════════════════════════════════════════════════

DROP TABLE IF EXISTS finances.loan_installments;
DROP TABLE IF EXISTS finances.loans;
