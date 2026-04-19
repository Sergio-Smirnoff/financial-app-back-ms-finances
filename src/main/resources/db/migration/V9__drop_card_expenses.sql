-- Card expense domain has been moved to ms-banks (schema `banks`, tables `cards` and `card_installments`).
-- User confirmed clean-slate migration (no data carryover).
DROP TABLE IF EXISTS finances.card_expense_installments;
DROP TABLE IF EXISTS finances.card_expenses;
