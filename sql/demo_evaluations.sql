-- Script de demonstration pour le module Evaluations.
-- A executer uniquement apres validation.
-- Il cree jusqu'a 15 evaluations a partir des relations actives employee_superiors.
-- Les evaluateurs respectent la regle metier :
-- employee_superiors.employee_id = employe evalue
-- employee_superiors.superior_id = evaluateur

-- Si d'anciennes donnees de demonstration utilisaient Admin Default comme evaluateur,
-- elles sont desactivees pour garder un jeu de donnees coherent.
UPDATE evaluations evaluation
JOIN employees evaluator ON evaluator.id = evaluation.evaluator_id
SET evaluation.is_active = 0
WHERE evaluation.is_active = 1
  AND evaluator.first_name = 'Admin'
  AND evaluator.last_name = 'Default';

INSERT INTO evaluations (
    evaluation_name,
    is_active,
    evaluation_date,
    period_start,
    period_end,
    global_score,
    comments,
    self_global_score,
    evaluator_id,
    employee_id,
    status
)
SELECT
    CONCAT('Evaluation demo ', demo.row_number, ' - ', employee.first_name, ' ', employee.last_name),
    1,
    demo.evaluation_date,
    demo.period_start,
    demo.period_end,
    demo.global_score,
    demo.comments,
    demo.self_global_score,
    relation.superior_id,
    relation.employee_id,
    demo.status
FROM (
    SELECT repeated_relations.employee_id,
           repeated_relations.superior_id,
           @row_number := @row_number + 1 AS row_number
    FROM (
        SELECT active_superiors.employee_id, active_superiors.superior_id
        FROM employee_superiors active_superiors
        JOIN employees employee ON employee.id = active_superiors.employee_id
        JOIN employees superior ON superior.id = active_superiors.superior_id
        WHERE active_superiors.is_active = 1
          AND employee.is_active = 1
          AND superior.is_active = 1
          AND NOT (employee.first_name = 'Admin' AND employee.last_name = 'Default')
          AND NOT (superior.first_name = 'Admin' AND superior.last_name = 'Default')
        UNION ALL
        SELECT active_superiors.employee_id, active_superiors.superior_id
        FROM employee_superiors active_superiors
        JOIN employees employee ON employee.id = active_superiors.employee_id
        JOIN employees superior ON superior.id = active_superiors.superior_id
        WHERE active_superiors.is_active = 1
          AND employee.is_active = 1
          AND superior.is_active = 1
          AND NOT (employee.first_name = 'Admin' AND employee.last_name = 'Default')
          AND NOT (superior.first_name = 'Admin' AND superior.last_name = 'Default')
        UNION ALL
        SELECT active_superiors.employee_id, active_superiors.superior_id
        FROM employee_superiors active_superiors
        JOIN employees employee ON employee.id = active_superiors.employee_id
        JOIN employees superior ON superior.id = active_superiors.superior_id
        WHERE active_superiors.is_active = 1
          AND employee.is_active = 1
          AND superior.is_active = 1
          AND NOT (employee.first_name = 'Admin' AND employee.last_name = 'Default')
          AND NOT (superior.first_name = 'Admin' AND superior.last_name = 'Default')
    ) repeated_relations
    CROSS JOIN (SELECT @row_number := 0) counter
    LIMIT 15
) relation
JOIN employees employee ON employee.id = relation.employee_id
JOIN (
    SELECT 1 AS row_number, 'CREATED' AS status, '2026-06-03' AS evaluation_date,
           '2026-01-01' AS period_start, '2026-03-31' AS period_end,
           NULL AS global_score, 'Evaluation creee pour preparation de l entretien.' AS comments,
           NULL AS self_global_score
    UNION ALL SELECT 2, 'IN_PROGRESS', '2026-06-04', '2026-01-01', '2026-03-31',
           NULL, 'Evaluation en cours de remplissage.', NULL
    UNION ALL SELECT 3, 'COMPLETED', '2026-06-05', '2026-01-01', '2026-03-31',
           7.50, 'Evaluation completee par le manager.', 7.25
    UNION ALL SELECT 4, 'SCORE_CALCULATED', '2026-06-06', '2026-01-01', '2026-03-31',
           8.20, 'Score final calcule.', 8.00
    UNION ALL SELECT 5, 'VALIDATED', '2026-06-07', '2026-01-01', '2026-03-31',
           8.75, 'Evaluation validee par le service RH.', 8.50
    UNION ALL SELECT 6, 'CREATED', '2026-06-10', '2026-04-01', '2026-06-30',
           NULL, 'Nouvelle evaluation trimestrielle creee.', NULL
    UNION ALL SELECT 7, 'IN_PROGRESS', '2026-06-11', '2026-04-01', '2026-06-30',
           NULL, 'Entretien planifie avec le manager.', NULL
    UNION ALL SELECT 8, 'COMPLETED', '2026-06-12', '2026-04-01', '2026-06-30',
           6.90, 'Evaluation completee avec axes d amelioration.', 6.75
    UNION ALL SELECT 9, 'SCORE_CALCULATED', '2026-06-13', '2026-04-01', '2026-06-30',
           7.85, 'Calcul du score termine.', 7.60
    UNION ALL SELECT 10, 'VALIDATED', '2026-06-14', '2026-04-01', '2026-06-30',
           9.10, 'Evaluation validee apres relecture RH.', 8.90
    UNION ALL SELECT 11, 'CREATED', '2026-06-17', '2026-07-01', '2026-09-30',
           NULL, 'Evaluation creee pour la prochaine periode.', NULL
    UNION ALL SELECT 12, 'IN_PROGRESS', '2026-06-18', '2026-07-01', '2026-09-30',
           NULL, 'Objectifs en cours de verification.', NULL
    UNION ALL SELECT 13, 'COMPLETED', '2026-06-19', '2026-07-01', '2026-09-30',
           8.00, 'Evaluation completee et prete pour calcul.', 7.80
    UNION ALL SELECT 14, 'CREATED', '2026-06-20', '2026-07-01', '2026-09-30',
           NULL, 'Evaluation creee apres controle RH.', NULL
    UNION ALL SELECT 15, 'VALIDATED', '2026-06-21', '2026-07-01', '2026-09-30',
           9.25, 'Evaluation finalisee et validee.', 9.00
) demo ON demo.row_number = relation.row_number;
