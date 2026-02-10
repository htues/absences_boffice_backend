CREATE TABLE IF NOT EXISTS companies (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200) NOT NULL,
    address VARCHAR(100) NOT NULL,

    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,

    created_by BIGINT NOT NULL DEFAULT 1,
    last_modified_by BIGINT NULL,

    created_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_modified_date TIMESTAMPTZ NULL
);

CREATE INDEX IF NOT EXISTS ix_companies_is_active ON companies (is_active);
CREATE INDEX IF NOT EXISTS ix_companies_is_deleted ON companies (is_deleted);