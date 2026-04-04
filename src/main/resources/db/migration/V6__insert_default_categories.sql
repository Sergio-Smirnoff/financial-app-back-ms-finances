-- ===========================
-- Parent categories (system)
-- ===========================
INSERT INTO finances.categories (id, parent_id, user_id, name, type, color, icon, is_system, active, created_at, updated_at)
VALUES (1,  NULL, NULL, 'Alimentación',         'EXPENSE', '#FF6B6B', 'utensils',        TRUE, TRUE, NOW(), NOW()),
       (2,  NULL, NULL, 'Transporte',            'EXPENSE', '#4ECDC4', 'car',             TRUE, TRUE, NOW(), NOW()),
       (3,  NULL, NULL, 'Servicios',             'EXPENSE', '#45B7D1', 'zap',             TRUE, TRUE, NOW(), NOW()),
       (4,  NULL, NULL, 'Salud',                 'EXPENSE', '#96CEB4', 'heart-pulse',     TRUE, TRUE, NOW(), NOW()),
       (5,  NULL, NULL, 'Entretenimiento',       'EXPENSE', '#FFEAA7', 'gamepad-2',       TRUE, TRUE, NOW(), NOW()),
       (6,  NULL, NULL, 'Indumentaria',          'EXPENSE', '#DDA0DD', 'shirt',           TRUE, TRUE, NOW(), NOW()),
       (7,  NULL, NULL, 'Educación',             'EXPENSE', '#98D8C8', 'graduation-cap',  TRUE, TRUE, NOW(), NOW()),
       (8,  NULL, NULL, 'Hogar',                 'EXPENSE', '#F7DC6F', 'home',            TRUE, TRUE, NOW(), NOW()),
       (9,  NULL, NULL, 'Ingresos laborales',    'INCOME',  '#58D68D', 'briefcase',       TRUE, TRUE, NOW(), NOW()),
       (10, NULL, NULL, 'Inversiones y rentas',  'INCOME',  '#5DADE2', 'trending-up',     TRUE, TRUE, NOW(), NOW()),
       (11, NULL, NULL, 'Otros',                 'BOTH',    '#AEB6BF', 'circle-dot',      TRUE, TRUE, NOW(), NOW());

-- ===========================
-- Subcategories — Alimentación (1)
-- ===========================
INSERT INTO finances.categories (id, parent_id, user_id, name, type, color, icon, is_system, active, created_at, updated_at)
VALUES (101, 1, NULL, 'Supermercado',            'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (102, 1, NULL, 'Restaurantes y delivery', 'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (103, 1, NULL, 'Cafetería',               'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (104, 1, NULL, 'Almacén',                 'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW());

-- ===========================
-- Subcategories — Transporte (2)
-- ===========================
INSERT INTO finances.categories (id, parent_id, user_id, name, type, color, icon, is_system, active, created_at, updated_at)
VALUES (201, 2, NULL, 'Combustible',              'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (202, 2, NULL, 'Transporte público',        'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (203, 2, NULL, 'Peajes',                   'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (204, 2, NULL, 'Estacionamiento',           'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (205, 2, NULL, 'Mantenimiento vehículo',    'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW());

-- ===========================
-- Subcategories — Servicios (3)
-- ===========================
INSERT INTO finances.categories (id, parent_id, user_id, name, type, color, icon, is_system, active, created_at, updated_at)
VALUES (301, 3, NULL, 'Electricidad',             'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (302, 3, NULL, 'Agua',                     'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (303, 3, NULL, 'Gas',                      'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (304, 3, NULL, 'Internet y telefonía',      'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (305, 3, NULL, 'Streaming',                'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW());

-- ===========================
-- Subcategories — Salud (4)
-- ===========================
INSERT INTO finances.categories (id, parent_id, user_id, name, type, color, icon, is_system, active, created_at, updated_at)
VALUES (401, 4, NULL, 'Médico y consultas',        'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (402, 4, NULL, 'Medicamentos',              'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (403, 4, NULL, 'Obra social / prepaga',     'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (404, 4, NULL, 'Gimnasio',                  'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW());

-- ===========================
-- Subcategories — Entretenimiento (5)
-- ===========================
INSERT INTO finances.categories (id, parent_id, user_id, name, type, color, icon, is_system, active, created_at, updated_at)
VALUES (501, 5, NULL, 'Salidas y ocio',            'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (502, 5, NULL, 'Juegos y videojuegos',       'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (503, 5, NULL, 'Eventos y entradas',         'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW());

-- ===========================
-- Subcategories — Indumentaria (6)
-- ===========================
INSERT INTO finances.categories (id, parent_id, user_id, name, type, color, icon, is_system, active, created_at, updated_at)
VALUES (601, 6, NULL, 'Ropa',                      'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (602, 6, NULL, 'Calzado',                   'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (603, 6, NULL, 'Accesorios',                'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW());

-- ===========================
-- Subcategories — Educación (7)
-- ===========================
INSERT INTO finances.categories (id, parent_id, user_id, name, type, color, icon, is_system, active, created_at, updated_at)
VALUES (701, 7, NULL, 'Cursos y capacitación',      'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (702, 7, NULL, 'Material educativo',         'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (703, 7, NULL, 'Suscripciones educativas',   'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW());

-- ===========================
-- Subcategories — Hogar (8)
-- ===========================
INSERT INTO finances.categories (id, parent_id, user_id, name, type, color, icon, is_system, active, created_at, updated_at)
VALUES (801, 8, NULL, 'Alquiler o expensas',        'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (802, 8, NULL, 'Muebles y decoración',       'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (803, 8, NULL, 'Electrodomésticos',          'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (804, 8, NULL, 'Limpieza e higiene',         'EXPENSE', NULL, NULL, TRUE, TRUE, NOW(), NOW());

-- ===========================
-- Subcategories — Ingresos laborales (9)
-- ===========================
INSERT INTO finances.categories (id, parent_id, user_id, name, type, color, icon, is_system, active, created_at, updated_at)
VALUES (901, 9, NULL, 'Sueldo',                    'INCOME',  NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (902, 9, NULL, 'Freelance y honorarios',    'INCOME',  NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (903, 9, NULL, 'Horas extra',               'INCOME',  NULL, NULL, TRUE, TRUE, NOW(), NOW());

-- ===========================
-- Subcategories — Inversiones y rentas (10)
-- ===========================
INSERT INTO finances.categories (id, parent_id, user_id, name, type, color, icon, is_system, active, created_at, updated_at)
VALUES (1001, 10, NULL, 'Intereses y rendimientos', 'INCOME', NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (1002, 10, NULL, 'Alquileres cobrados',       'INCOME', NULL, NULL, TRUE, TRUE, NOW(), NOW()),
       (1003, 10, NULL, 'Dividendos',                'INCOME', NULL, NULL, TRUE, TRUE, NOW(), NOW());

-- ===========================
-- Subcategories — Otros (11)
-- ===========================
INSERT INTO finances.categories (id, parent_id, user_id, name, type, color, icon, is_system, active, created_at, updated_at)
VALUES (1101, 11, NULL, 'Varios', 'BOTH', NULL, NULL, TRUE, TRUE, NOW(), NOW());

-- Reset sequence to avoid collision with future inserts
SELECT setval('finances.categories_id_seq', (SELECT MAX(id) FROM finances.categories));
