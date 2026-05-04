-- Find the parent 'Unassigned' category IDs
-- This handles cases where IDs might differ between environments
INSERT INTO finances.categories (parent_id, user_id, name, type, color, icon, is_system, active, created_at, updated_at)
SELECT id, NULL, 'Unassigned', type, color, icon, TRUE, TRUE, NOW(), NOW()
FROM finances.categories
WHERE name = 'Unassigned' AND parent_id IS NULL;
