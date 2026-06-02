-- A global system category used by ms-investments to record holding buy/sell
-- transactions (purchase debits the funding account, sale credits the destination).
INSERT INTO finances.categories (id, parent_id, user_id, name, is_system, active, created_at, updated_at)
VALUES (12, NULL, NULL, 'Inversiones', TRUE, TRUE, NOW(), NOW());
