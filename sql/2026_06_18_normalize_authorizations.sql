-- Normalisation des autorisations.
-- Format retenu : module:create, module:read, module:edit, module:delete.

START TRANSACTION;

-- Autorisations manquantes utilisees par le dashboard.
INSERT INTO authorizations (authorization_name, is_active)
SELECT 'hub:read', 1
WHERE NOT EXISTS (SELECT 1 FROM authorizations WHERE authorization_name = 'hub:read');

INSERT INTO authorizations (authorization_name, is_active)
SELECT 'department:read', 1
WHERE NOT EXISTS (SELECT 1 FROM authorizations WHERE authorization_name = 'department:read');

INSERT INTO authorizations (authorization_name, is_active)
SELECT 'evaluation:read', 1
WHERE NOT EXISTS (SELECT 1 FROM authorizations WHERE authorization_name = 'evaluation:read');

-- Normalisation des anciens noms non conformes.
INSERT INTO authorizations (authorization_name, is_active)
SELECT 'absence:create', 1
WHERE NOT EXISTS (SELECT 1 FROM authorizations WHERE authorization_name = 'absence:create');

INSERT INTO authorizations (authorization_name, is_active)
SELECT 'absence:edit', 1
WHERE NOT EXISTS (SELECT 1 FROM authorizations WHERE authorization_name = 'absence:edit');

INSERT INTO authorizations (authorization_name, is_active)
SELECT 'planning:edit', 1
WHERE NOT EXISTS (SELECT 1 FROM authorizations WHERE authorization_name = 'planning:edit');

INSERT INTO authorizations (authorization_name, is_active)
SELECT 'training:edit', 1
WHERE NOT EXISTS (SELECT 1 FROM authorizations WHERE authorization_name = 'training:edit');

INSERT INTO authorizations (authorization_name, is_active)
SELECT 'admin:read', 1
WHERE NOT EXISTS (SELECT 1 FROM authorizations WHERE authorization_name = 'admin:read');

-- Repointage des roles vers les autorisations normalisees.
UPDATE roles_authorization ra
JOIN authorizations old_auth ON old_auth.id = ra.authorization_id
JOIN authorizations new_auth ON new_auth.authorization_name =
    CASE old_auth.authorization_name
        WHEN 'hub:access' THEN 'hub:read'
        WHEN 'admin:access' THEN 'admin:read'
        WHEN 'employee:update' THEN 'employee:edit'
        WHEN 'employee:deactivate' THEN 'employee:delete'
        WHEN 'absence:request' THEN 'absence:create'
        WHEN 'absence:approve' THEN 'absence:edit'
        WHEN 'planning:manage' THEN 'planning:edit'
        WHEN 'training:manage' THEN 'training:edit'
        ELSE old_auth.authorization_name
    END
SET ra.authorization_id = new_auth.id,
    ra.role_authorization_name = CONCAT(
        (SELECT r.role_name FROM roles r WHERE r.id = ra.role_id),
        '_',
        REPLACE(new_auth.authorization_name, ':', '_')
    )
WHERE old_auth.authorization_name IN (
    'hub:access',
    'admin:access',
    'employee:update',
    'employee:deactivate',
    'absence:request',
    'absence:approve',
    'planning:manage',
    'training:manage'
);

-- Attribution des nouvelles autorisations utiles.
INSERT INTO roles_authorization (role_authorization_name, is_active, role_id, authorization_id)
SELECT CONCAT(r.role_name, '_', REPLACE(a.authorization_name, ':', '_')), 1, r.id, a.id
FROM roles r
JOIN authorizations a ON a.authorization_name IN (
    'hub:read',
    'department:read',
    'evaluation:read'
)
WHERE r.role_name IN ('ADMIN', 'HR')
AND NOT EXISTS (
    SELECT 1
    FROM roles_authorization ra
    WHERE ra.role_id = r.id
      AND ra.authorization_id = a.id
);

INSERT INTO roles_authorization (role_authorization_name, is_active, role_id, authorization_id)
SELECT CONCAT(r.role_name, '_', REPLACE(a.authorization_name, ':', '_')), 1, r.id, a.id
FROM roles r
JOIN authorizations a ON a.authorization_name IN ('hub:read')
WHERE r.role_name IN ('EMPLOYEE', 'SERVICE_MANAGER', 'SECRETARY', 'STAGIAIRE', 'STAGIAIRE 2')
AND NOT EXISTS (
    SELECT 1
    FROM roles_authorization ra
    WHERE ra.role_id = r.id
      AND ra.authorization_id = a.id
);

-- Les anciens noms restent dans la table mais sont desactives pour garder l'historique.
UPDATE authorizations
SET is_active = 0
WHERE authorization_name IN (
    'hub:access',
    'admin:access',
    'employee:update',
    'employee:deactivate',
    'absence:request',
    'absence:approve',
    'planning:manage',
    'training:manage'
);

COMMIT;
