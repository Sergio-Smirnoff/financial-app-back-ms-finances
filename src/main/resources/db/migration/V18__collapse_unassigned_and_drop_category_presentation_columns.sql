-- Slice 5: the account-to-account model derives expense/income from ownership, not a category
-- attribute, so the two directional system 'Unassigned' categories collapse into one, and the
-- presentation columns (type/color/icon) are dropped — the minimal Category aggregate is
-- name + status + subcategories. color/icon are client-side concerns now.

-- 1. Repoint any transaction referencing a non-kept 'Unassigned' row to the kept one
--    (kept = lowest-id top-level system 'Unassigned').
UPDATE finances.transactions
SET category_id = (SELECT MIN(id) FROM finances.categories
                   WHERE name = 'Unassigned' AND is_system = TRUE AND parent_id IS NULL)
WHERE category_id IN (
    SELECT id FROM finances.categories
    WHERE name = 'Unassigned' AND is_system = TRUE
      AND id <> (SELECT MIN(id) FROM finances.categories
                 WHERE name = 'Unassigned' AND is_system = TRUE AND parent_id IS NULL)
);

-- 2. Delete the duplicate 'Unassigned' rows (children first for the self-referential FK).
DELETE FROM finances.categories
WHERE name = 'Unassigned' AND is_system = TRUE AND parent_id IS NOT NULL;

DELETE FROM finances.categories
WHERE name = 'Unassigned' AND is_system = TRUE AND parent_id IS NULL
  AND id <> (SELECT MIN(id) FROM finances.categories
             WHERE name = 'Unassigned' AND is_system = TRUE AND parent_id IS NULL);

-- 3. Drop the now-unused presentation columns.
ALTER TABLE finances.categories DROP COLUMN type;
ALTER TABLE finances.categories DROP COLUMN color;
ALTER TABLE finances.categories DROP COLUMN icon;
