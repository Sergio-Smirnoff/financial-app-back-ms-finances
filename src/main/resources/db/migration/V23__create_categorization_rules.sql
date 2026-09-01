CREATE TABLE finances.categorization_rules (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    match_type  VARCHAR(20) NOT NULL,
    pattern     VARCHAR(200) NOT NULL,
    category_id BIGINT      NOT NULL REFERENCES finances.categories (id),
    match_count INT         NOT NULL DEFAULT 0,
    created_at  TIMESTAMP   NOT NULL DEFAULT now()
);
CREATE INDEX idx_categorization_rules_user ON finances.categorization_rules (user_id);
