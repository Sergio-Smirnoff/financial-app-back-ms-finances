ALTER TABLE finances.transactions
  ADD COLUMN mep_rate NUMERIC(12,4),
  ADD COLUMN ccl_rate NUMERIC(12,4),
  ADD COLUMN oficial_rate NUMERIC(12,4),
  ADD COLUMN rate_date DATE;
