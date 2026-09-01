CREATE TABLE finances.budgets (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT       NOT NULL,
    category_id         BIGINT       NOT NULL REFERENCES finances.categories (id),
    year                INT          NOT NULL,
    month               INT          NOT NULL,
    amount              NUMERIC(15,2) NOT NULL,
    currency            CHAR(3)      NOT NULL,
    alert_threshold_pct NUMERIC(5,2),
    last_alerted_year   INT,
    last_alerted_month  INT,
    created_at          TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT now(),
    UNIQUE (user_id, category_id, year, month)
);
