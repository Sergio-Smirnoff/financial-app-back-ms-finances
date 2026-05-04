INSERT INTO finances.categories (parent_id, user_id, name, type, color, icon, is_system, active, created_at, updated_at)
VALUES
  (NULL, NULL, 'Unassigned', 'EXPENSE', '#9E9E9E', 'help-circle', TRUE, TRUE, NOW(), NOW()),
  (NULL, NULL, 'Unassigned', 'INCOME',  '#9E9E9E', 'help-circle', TRUE, TRUE, NOW(), NOW());
