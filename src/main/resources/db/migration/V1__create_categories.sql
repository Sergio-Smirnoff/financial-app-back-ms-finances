CREATE TABLE finances.categories
(
    id        BIGSERIAL PRIMARY KEY,
    parent_id BIGINT REFERENCES finances.categories (id),
    user_id   BIGINT,
    name      VARCHAR(100) NOT NULL,
    type      VARCHAR(10)  NOT NULL,
    color     VARCHAR(7),
    icon      VARCHAR(50),
    is_system BOOLEAN      NOT NULL DEFAULT FALSE,
    active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP   NOT NULL,
    updated_at TIMESTAMP   NOT NULL
);

CREATE INDEX idx_categories_parent_id ON finances.categories (parent_id);
CREATE INDEX idx_categories_user_id ON finances.categories (user_id);
CREATE INDEX idx_categories_type ON finances.categories (type);
