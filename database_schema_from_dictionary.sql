CREATE DATABASE IF NOT EXISTS `erp_projet_integration`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `erp_projet_integration`;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `candidates`;
DROP TABLE IF EXISTS `job_offers_candidates`;
DROP TABLE IF EXISTS `job_offers`;
DROP TABLE IF EXISTS `evaluations_objectives`;
DROP TABLE IF EXISTS `objectives`;
DROP TABLE IF EXISTS `criteria_function`;
DROP TABLE IF EXISTS `categories`;
DROP TABLE IF EXISTS `criteria`;
DROP TABLE IF EXISTS `evaluation_criteria`;
DROP TABLE IF EXISTS `evaluations`;
DROP TABLE IF EXISTS `companies`;
DROP TABLE IF EXISTS `training_centers`;
DROP TABLE IF EXISTS `employee_trainings`;
DROP TABLE IF EXISTS `training_functions`;
DROP TABLE IF EXISTS `trainings`;
DROP TABLE IF EXISTS `functions`;
DROP TABLE IF EXISTS `employee_functions`;
DROP TABLE IF EXISTS `plannings_employees`;
DROP TABLE IF EXISTS `plannings_departments`;
DROP TABLE IF EXISTS `plannings`;
DROP TABLE IF EXISTS `absences`;
DROP TABLE IF EXISTS `contracts`;
DROP TABLE IF EXISTS `superiors`;
DROP TABLE IF EXISTS `department_heads`;
DROP TABLE IF EXISTS `departments`;
DROP TABLE IF EXISTS `cities`;
DROP TABLE IF EXISTS `authorizations`;
DROP TABLE IF EXISTS `roles_authorization`;
DROP TABLE IF EXISTS `roles`;
DROP TABLE IF EXISTS `addresses`;
DROP TABLE IF EXISTS `employees`;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `employees` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `last_name` VARCHAR(100) NOT NULL,
    `first_name` VARCHAR(100) NOT NULL,
    `birth_date` DATE,
    `place_of_birth` VARCHAR(150),
    `phone` VARCHAR(20),
    `email` VARCHAR(150) NOT NULL,
    UNIQUE KEY `uq_employees_email` (`email`),
    `password` VARCHAR(255) NOT NULL,
    `civilite` VARCHAR(10),
    `gender` VARCHAR(10),
    `employment_status` VARCHAR(50),
    `employee_number` VARCHAR(50) NOT NULL,
    UNIQUE KEY `uq_employees_employee_number` (`employee_number`),
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `address_id` INT,
    `role_id` INT
) ENGINE=InnoDB;

CREATE TABLE `addresses` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `street_name` VARCHAR(50) NOT NULL,
    `street_number` VARCHAR(11),
    `box_number` VARCHAR(5),
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `city_id` INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE `roles` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `role_name` VARCHAR(100) NOT NULL,
    UNIQUE KEY `uq_roles_role_name` (`role_name`),
    `is_active` TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE `roles_authorization` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `role_authorization_name` VARCHAR(150),
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `role_id` INT NOT NULL,
    `authorization_id` INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE `authorizations` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `authorization_name` VARCHAR(150) NOT NULL,
    UNIQUE KEY `uq_authorizations_authorization_name` (`authorization_name`),
    `is_active` TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE `cities` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `city_name` VARCHAR(100) NOT NULL,
    `zip_code` INT NOT NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE `departments` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `department_name` VARCHAR(150) NOT NULL,
    UNIQUE KEY `uq_departments_department_name` (`department_name`),
    `description` TEXT,
    `phone` VARCHAR(20),
    `email` VARCHAR(150),
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `department_head_id` INT
) ENGINE=InnoDB;

CREATE TABLE `department_heads` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `start_date` DATE,
    `end_date` DATE,
    `superior_id` INT,
    `employee_id` INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE `superiors` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `superior_name` VARCHAR(200) NOT NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `start_date` DATE,
    `employee_id` INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE `contracts` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `contract_code` VARCHAR(50) NOT NULL,
    UNIQUE KEY `uq_contracts_contract_code` (`contract_code`),
    `start_date` DATE NOT NULL,
    `end_date` DATE,
    `status` VARCHAR(50),
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `employee_id` INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE `absences` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(200),
    `start_date` DATE NOT NULL,
    `end_date` DATE,
    `reason` VARCHAR(100),
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `employee_id` INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE `plannings` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `date` DATE NOT NULL,
    `start_hour` TIME,
    `end_hour` TIME,
    `note` VARCHAR(255),
    `type` VARCHAR(50),
    `description` TEXT,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `department_id` INT
) ENGINE=InnoDB;

CREATE TABLE `plannings_departments` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `planning_id` INT NOT NULL,
    `department_id` INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE `plannings_employees` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `employee_id` INT NOT NULL,
    `planning_id` INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE `employee_functions` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `start_date` DATE,
    `end_date` DATE,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `function_id` INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE `functions` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `function_name` VARCHAR(150) NOT NULL,
    UNIQUE KEY `uq_functions_function_name` (`function_name`),
    `mandatory` TINYINT(1),
    `description` TEXT,
    `number_of_open_positions` INT,
    `job_description` TEXT,
    `comments` TEXT,
    `status` VARCHAR(50),
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `function_id` INT,
    `city_id` INT
) ENGINE=InnoDB;

CREATE TABLE `trainings` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `training_certificate_name` VARCHAR(200),
    `code` VARCHAR(50),
    UNIQUE KEY `uq_trainings_code` (`code`),
    `validity_months` INT,
    `description` TEXT,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE `training_functions` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `mandatory` TINYINT(1),
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `training_id` INT NOT NULL,
    `function_id` INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE `employee_trainings` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `obtained_date` DATE,
    `expiry_date` DATE,
    `status` VARCHAR(50),
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `training_id` INT NOT NULL,
    `employee_id` INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE `training_centers` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `training_career_name` VARCHAR(200),
    `phone` VARCHAR(20),
    `address_id` INT
) ENGINE=InnoDB;

CREATE TABLE `companies` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(200) NOT NULL,
    `phone` VARCHAR(20),
    `bank` VARCHAR(100),
    `iban` VARCHAR(34),
    UNIQUE KEY `uq_companies_iban` (`iban`),
    `logo` VARCHAR(500),
    `email` VARCHAR(150),
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `website` VARCHAR(255),
    `address_id` INT
) ENGINE=InnoDB;

CREATE TABLE `evaluations` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `evaluation_name` VARCHAR(200) NOT NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `evaluation_date` DATE,
    `period_start` DATE,
    `period_end` DATE,
    `global_score` DECIMAL(10,2),
    `comments` TEXT,
    `self_global_score` DECIMAL(10,2),
    `evaluator_id` INT NOT NULL,
    `employee_id` INT NOT NULL,
    `status` VARCHAR(50) NOT NULL DEFAULT 'CREATED'
) ENGINE=InnoDB;

CREATE TABLE `evaluation_criteria` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `note` DECIMAL(10,2),
    `comment` TEXT,
    `self_evaluation` DECIMAL(10,2),
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `evaluation_id` INT NOT NULL,
    `criterion_id` INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE `criteria` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `criterion_name` VARCHAR(200) NOT NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `description` TEXT,
    `category_id` INT,
    `function_criterion_id` INT
) ENGINE=InnoDB;

CREATE TABLE `categories` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `category_name` VARCHAR(150) NOT NULL,
    UNIQUE KEY `uq_categories_category_name` (`category_name`)
) ENGINE=InnoDB;

CREATE TABLE `criteria_function` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `function_id` INT NOT NULL,
    `criterion_id` INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE `objectives` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `objectives_name` VARCHAR(200) NOT NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `description` TEXT
) ENGINE=InnoDB;

CREATE TABLE `evaluations_objectives` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `evaluation_objectives_name` VARCHAR(200),
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `objective_id` INT NOT NULL,
    `evaluation_id` INT NOT NULL,
    `status` VARCHAR(50) NOT NULL DEFAULT 'TO_DO'
) ENGINE=InnoDB;

CREATE TABLE `job_offers` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `job_offer_name` VARCHAR(200) NOT NULL,
    `description` TEXT,
    `email` VARCHAR(150),
    `contact` VARCHAR(150),
    `duration` VARCHAR(100),
    `number_of_open_positions` INT,
    `profil` TEXT,
    `job_description` TEXT,
    `requirements` TEXT,
    `comments` TEXT,
    `create_at` DATETIME NOT NULL,
    `publish_start_date` DATE,
    `publish_end_date` DATE,
    `status` VARCHAR(50),
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `function_id` INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE `job_offers_candidates` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `job_offer_name` VARCHAR(200),
    `application_date` DATE NOT NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `application_status` VARCHAR(50),
    `job_offers_id` INT NOT NULL,
    `candidate_id` INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE `candidates` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `first_name` VARCHAR(100) NOT NULL,
    `last_name` VARCHAR(100) NOT NULL,
    `birth_date` DATE,
    `email` VARCHAR(150) NOT NULL,
    UNIQUE KEY `uq_candidates_email` (`email`),
    `phone` VARCHAR(20),
    `status` VARCHAR(50),
    `is_active` TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

ALTER TABLE `employees`
    ADD CONSTRAINT `fk_employees_address_id` FOREIGN KEY (`address_id`) REFERENCES `addresses` (`id`);

ALTER TABLE `employees`
    ADD CONSTRAINT `fk_employees_role_id` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`);

ALTER TABLE `addresses`
    ADD CONSTRAINT `fk_addresses_city_id` FOREIGN KEY (`city_id`) REFERENCES `cities` (`id`);

ALTER TABLE `roles_authorization`
    ADD CONSTRAINT `fk_roles_authorization_role_id` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`);

ALTER TABLE `roles_authorization`
    ADD CONSTRAINT `fk_roles_authorization_authorization_id` FOREIGN KEY (`authorization_id`) REFERENCES `authorizations` (`id`);

ALTER TABLE `departments`
    ADD CONSTRAINT `fk_departments_department_head_id` FOREIGN KEY (`department_head_id`) REFERENCES `department_heads` (`id`);

ALTER TABLE `department_heads`
    ADD CONSTRAINT `fk_department_heads_superior_id` FOREIGN KEY (`superior_id`) REFERENCES `superiors` (`id`);

ALTER TABLE `department_heads`
    ADD CONSTRAINT `fk_department_heads_employee_id` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`);

ALTER TABLE `superiors`
    ADD CONSTRAINT `fk_superiors_employee_id` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`);

ALTER TABLE `contracts`
    ADD CONSTRAINT `fk_contracts_employee_id` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`);

ALTER TABLE `absences`
    ADD CONSTRAINT `fk_absences_employee_id` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`);

ALTER TABLE `plannings`
    ADD CONSTRAINT `fk_plannings_department_id` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`);

ALTER TABLE `plannings_departments`
    ADD CONSTRAINT `fk_plannings_departments_planning_id` FOREIGN KEY (`planning_id`) REFERENCES `plannings` (`id`);

ALTER TABLE `plannings_departments`
    ADD CONSTRAINT `fk_plannings_departments_department_id` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`);

ALTER TABLE `plannings_employees`
    ADD CONSTRAINT `fk_plannings_employees_employee_id` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`);

ALTER TABLE `plannings_employees`
    ADD CONSTRAINT `fk_plannings_employees_planning_id` FOREIGN KEY (`planning_id`) REFERENCES `plannings` (`id`);

ALTER TABLE `employee_functions`
    ADD CONSTRAINT `fk_employee_functions_function_id` FOREIGN KEY (`function_id`) REFERENCES `functions` (`id`);

ALTER TABLE `functions`
    ADD CONSTRAINT `fk_functions_function_id` FOREIGN KEY (`function_id`) REFERENCES `functions` (`id`);

ALTER TABLE `functions`
    ADD CONSTRAINT `fk_functions_city_id` FOREIGN KEY (`city_id`) REFERENCES `cities` (`id`);

ALTER TABLE `training_functions`
    ADD CONSTRAINT `fk_training_functions_training_id` FOREIGN KEY (`training_id`) REFERENCES `trainings` (`id`);

ALTER TABLE `training_functions`
    ADD CONSTRAINT `fk_training_functions_function_id` FOREIGN KEY (`function_id`) REFERENCES `functions` (`id`);

ALTER TABLE `employee_trainings`
    ADD CONSTRAINT `fk_employee_trainings_training_id` FOREIGN KEY (`training_id`) REFERENCES `trainings` (`id`);

ALTER TABLE `employee_trainings`
    ADD CONSTRAINT `fk_employee_trainings_employee_id` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`);

ALTER TABLE `training_centers`
    ADD CONSTRAINT `fk_training_centers_address_id` FOREIGN KEY (`address_id`) REFERENCES `addresses` (`id`);

ALTER TABLE `companies`
    ADD CONSTRAINT `fk_companies_address_id` FOREIGN KEY (`address_id`) REFERENCES `addresses` (`id`);

ALTER TABLE `evaluations`
    ADD CONSTRAINT `fk_evaluations_evaluator_id` FOREIGN KEY (`evaluator_id`) REFERENCES `employees` (`id`);

ALTER TABLE `evaluations`
    ADD CONSTRAINT `fk_evaluations_employee_id` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`id`);

ALTER TABLE `evaluation_criteria`
    ADD CONSTRAINT `fk_evaluation_criteria_evaluation_id` FOREIGN KEY (`evaluation_id`) REFERENCES `evaluations` (`id`);

ALTER TABLE `evaluation_criteria`
    ADD CONSTRAINT `fk_evaluation_criteria_criterion_id` FOREIGN KEY (`criterion_id`) REFERENCES `criteria` (`id`);

ALTER TABLE `criteria`
    ADD CONSTRAINT `fk_criteria_category_id` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`);

ALTER TABLE `criteria`
    ADD CONSTRAINT `fk_criteria_function_criterion_id` FOREIGN KEY (`function_criterion_id`) REFERENCES `criteria_function` (`id`);

ALTER TABLE `criteria_function`
    ADD CONSTRAINT `fk_criteria_function_function_id` FOREIGN KEY (`function_id`) REFERENCES `functions` (`id`);

ALTER TABLE `criteria_function`
    ADD CONSTRAINT `fk_criteria_function_criterion_id_2` FOREIGN KEY (`criterion_id`) REFERENCES `criteria` (`id`);

ALTER TABLE `evaluations_objectives`
    ADD CONSTRAINT `fk_evaluations_objectives_objective_id` FOREIGN KEY (`objective_id`) REFERENCES `objectives` (`id`);

ALTER TABLE `evaluations_objectives`
    ADD CONSTRAINT `fk_evaluations_objectives_evaluation_id` FOREIGN KEY (`evaluation_id`) REFERENCES `evaluations` (`id`);

ALTER TABLE `job_offers`
    ADD CONSTRAINT `fk_job_offers_function_id` FOREIGN KEY (`function_id`) REFERENCES `functions` (`id`);

ALTER TABLE `job_offers_candidates`
    ADD CONSTRAINT `fk_job_offers_candidates_job_offers_id` FOREIGN KEY (`job_offers_id`) REFERENCES `job_offers` (`id`);

ALTER TABLE `job_offers_candidates`
    ADD CONSTRAINT `fk_job_offers_candidates_candidate_id` FOREIGN KEY (`candidate_id`) REFERENCES `candidates` (`id`);

INSERT INTO `roles` (`role_name`, `is_active`) VALUES
('ADMIN', 1),
('RH', 1),
('MANAGER', 1),
('EMPLOYEE', 1);

INSERT INTO `authorizations` (`authorization_name`, `is_active`) VALUES
('EMPLOYEE_READ', 1),
('EMPLOYEE_WRITE', 1),
('PLANNING_READ', 1),
('PLANNING_WRITE', 1),
('ABSENCE_READ', 1),
('ABSENCE_WRITE', 1),
('EVALUATION_READ', 1),
('EVALUATION_WRITE', 1),
('TRAINING_READ', 1),
('TRAINING_WRITE', 1);
