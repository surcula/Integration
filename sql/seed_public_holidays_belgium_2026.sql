INSERT INTO public_holidays (name, holiday_date, description, is_active) VALUES
('Jour de l''An', '2026-01-01', 'Jour ferie legal belge.', 1),
('Lundi de Paques', '2026-04-06', 'Jour ferie legal belge.', 1),
('Fete du Travail', '2026-05-01', 'Jour ferie legal belge.', 1),
('Ascension', '2026-05-14', 'Jour ferie legal belge.', 1),
('Lundi de Pentecote', '2026-05-25', 'Jour ferie legal belge.', 1),
('Fete nationale', '2026-07-21', 'Jour ferie legal belge.', 1),
('Assomption', '2026-08-15', 'Jour ferie legal belge.', 1),
('Toussaint', '2026-11-01', 'Jour ferie legal belge.', 1),
('Armistice', '2026-11-11', 'Jour ferie legal belge.', 1),
('Noel', '2026-12-25', 'Jour ferie legal belge.', 1)
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    is_active = VALUES(is_active);
