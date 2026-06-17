INSERT INTO authorizations (authorization_name, is_active)
SELECT 'address:read', 1
WHERE NOT EXISTS (SELECT 1 FROM authorizations WHERE authorization_name = 'address:read');

INSERT INTO authorizations (authorization_name, is_active)
SELECT 'address:create', 1
WHERE NOT EXISTS (SELECT 1 FROM authorizations WHERE authorization_name = 'address:create');

INSERT INTO authorizations (authorization_name, is_active)
SELECT 'address:edit', 1
WHERE NOT EXISTS (SELECT 1 FROM authorizations WHERE authorization_name = 'address:edit');

INSERT INTO authorizations (authorization_name, is_active)
SELECT 'address:delete', 1
WHERE NOT EXISTS (SELECT 1 FROM authorizations WHERE authorization_name = 'address:delete');

INSERT INTO authorizations (authorization_name, is_active)
SELECT 'city:read', 1
WHERE NOT EXISTS (SELECT 1 FROM authorizations WHERE authorization_name = 'city:read');

INSERT INTO authorizations (authorization_name, is_active)
SELECT 'city:create', 1
WHERE NOT EXISTS (SELECT 1 FROM authorizations WHERE authorization_name = 'city:create');

INSERT INTO authorizations (authorization_name, is_active)
SELECT 'city:edit', 1
WHERE NOT EXISTS (SELECT 1 FROM authorizations WHERE authorization_name = 'city:edit');

INSERT INTO authorizations (authorization_name, is_active)
SELECT 'city:delete', 1
WHERE NOT EXISTS (SELECT 1 FROM authorizations WHERE authorization_name = 'city:delete');

INSERT INTO roles_authorization (role_authorization_name, is_active, role_id, authorization_id)
SELECT CONCAT(r.role_name, '_', REPLACE(a.authorization_name, ':', '_')), 1, r.id, a.id
FROM roles r
JOIN authorizations a ON a.authorization_name IN (
    'address:read',
    'address:create',
    'address:edit',
    'address:delete',
    'city:read',
    'city:create',
    'city:edit',
    'city:delete'
)
WHERE r.role_name = 'ADMIN'
AND NOT EXISTS (
    SELECT 1
    FROM roles_authorization ra
    WHERE ra.role_id = r.id
    AND ra.authorization_id = a.id
);
