INSERT INTO companies (
    name,
    description,
    address,
    is_active,
    is_deleted,
    created_by,
    created_date
)
VALUES
    ('Acme Corp', 'Default demo company', '123 Main St', TRUE, FALSE, 1, NOW()),
    ('Globex', 'Reference company for testing', '456 Market Ave', TRUE, FALSE, 1, NOW()),
    ('Initech', 'Internal seed record', '789 Industrial Rd', TRUE, FALSE, 1, NOW())
ON CONFLICT (name) DO NOTHING;