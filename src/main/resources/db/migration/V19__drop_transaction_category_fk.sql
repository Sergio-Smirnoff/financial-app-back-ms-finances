-- Transaction and Category are separate aggregates: reference by id only, no cross-aggregate DB FK.
-- Drop the constraint; keep the category_id column and its index (idx_transactions_category_id from V2).
-- Referential integrity is enforced in the domain (the system 'Unassigned' fallback guarantees a
-- valid id on the write path).
ALTER TABLE finances.transactions
    DROP CONSTRAINT IF EXISTS transactions_category_id_fkey;
