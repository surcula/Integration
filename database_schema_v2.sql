CREATE DATABASE IF NOT EXISTS erp_projet_integration
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE erp_projet_integration;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS job_offers_candidates;
DROP TABLE IF EXISTS job_offers;
DROP TABLE IF EXISTS evaluation_objectives;
DROP TABLE IF EXISTS evaluation_criteria;
DROP TABLE IF EXISTS evaluations;
DROP TABLE IF EXISTS criteria_functions;
DROP TABLE IF EXISTS criteria;
DROP TABLE IF EXISTS objectives;
DROP TABLE IF EXISTS employees_certificates;
DROP TABLE IF EXISTS training_functions;
DROP TABLE IF EXISTS trainings;
DROP TABLE IF EXISTS training_centers;
DROP TABLE IF EXISTS planning_employees;
DROP TABLE IF EXISTS plannings;
DROP TABLE IF EXISTS absences;
DROP TABLE IF EXISTS contracts;
DROP TABLE IF EXISTS employee_superiors;
DROP TABLE IF EXISTS employee_departments;
DROP TABLE IF EXISTS department_heads;
DROP TABLE IF EXISTS departments;
DROP TABLE IF EXISTS employees_functions;
DROP TABLE IF EXISTS roles_authorization;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS companies;
DROP TABLE IF EXISTS addresses;
DROP TABLE IF EXISTS cities;
DROP TABLE IF EXISTS functions;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS authorizations;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS public_holidays;
DROP TABLE IF EXISTS candidates;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE cities (
    city_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    zip_code VARCHAR(20),
    is_active TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE addresses (
    address_id INT AUTO_INCREMENT PRIMARY KEY,
    street_name VARCHAR(150) NOT NULL,
    street_number VARCHAR(20),
    box_number VARCHAR(20),
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    city_id INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    is_active TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE authorizations (
    authorization_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    is_active TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE roles_authorization (
    role_authorization_id INT AUTO_INCREMENT PRIMARY KEY,
    role_authorization_name VARCHAR(150),
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    role_id INT NOT NULL,
    authorization_id INT NOT NULL,
    UNIQUE KEY uq_roles_authorization (role_id, authorization_id)
) ENGINE=InnoDB;

CREATE TABLE functions (
    function_id INT AUTO_INCREMENT PRIMARY KEY,
    function_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    is_active TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE employees (
    employee_id INT AUTO_INCREMENT PRIMARY KEY,
    last_name VARCHAR(100) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    birth_date DATE,
    place_of_birth VARCHAR(100),
    phone VARCHAR(30),
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255),
    civility VARCHAR(30),
    gender VARCHAR(30),
    employment_status VARCHAR(100),
    employee_number VARCHAR(50) UNIQUE,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    address_id INT,
    role_id INT
) ENGINE=InnoDB;

CREATE TABLE departments (
    department_id INT AUTO_INCREMENT PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    phone VARCHAR(30),
    email VARCHAR(150),
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    department_head_id INT
) ENGINE=InnoDB;

CREATE TABLE department_heads (
    department_head_id INT AUTO_INCREMENT PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date DATE,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    department_id INT NOT NULL,
    superior_id INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE employee_departments (
    employee_department_id INT AUTO_INCREMENT PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date DATE,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    department_id INT NOT NULL,
    employee_id INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE employee_superiors (
    employee_superior_id INT AUTO_INCREMENT PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date DATE,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    superior_id INT NOT NULL,
    employee_id INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE employees_functions (
    employee_function_id INT AUTO_INCREMENT PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date DATE,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    function_id INT NOT NULL,
    employee_id INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE contracts (
    contract_id INT AUTO_INCREMENT PRIMARY KEY,
    contract_type VARCHAR(100) NOT NULL,
    contract_name VARCHAR(150),
    salary DECIMAL(10,2),
    work_schedule VARCHAR(100),
    start_date DATE NOT NULL,
    end_date DATE,
    status VARCHAR(50),
    description VARCHAR(255),
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    employee_id INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE plannings (
    planning_id INT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    period VARCHAR(100),
    status VARCHAR(50),
    description VARCHAR(255),
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    employee_id INT,
    department_id INT
) ENGINE=InnoDB;

CREATE TABLE planning_employees (
    planning_employee_id INT AUTO_INCREMENT PRIMARY KEY,
    date DATE NOT NULL,
    start_hour TIME NOT NULL,
    end_hour TIME NOT NULL,
    note VARCHAR(255),
    status VARCHAR(50),
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    employee_id INT NOT NULL,
    planning_id INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE public_holidays (
    holiday_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    date DATE NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    UNIQUE KEY uq_public_holidays_date (date)
) ENGINE=InnoDB;

CREATE TABLE absences (
    absence_id INT AUTO_INCREMENT PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    number_of_days DECIMAL(5,2),
    type VARCHAR(100) NOT NULL,
    request_date DATE,
    approved_by VARCHAR(150),
    comment VARCHAR(255),
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    status VARCHAR(50),
    employee_id INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE training_centers (
    training_center_id INT AUTO_INCREMENT PRIMARY KEY,
    training_centers_name VARCHAR(150) NOT NULL,
    phone VARCHAR(30),
    email VARCHAR(150),
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    address_id INT
) ENGINE=InnoDB;

CREATE TABLE trainings (
    training_id INT AUTO_INCREMENT PRIMARY KEY,
    training_certificate_name VARCHAR(150) NOT NULL,
    code VARCHAR(50),
    validity_months INT,
    description VARCHAR(255),
    is_active TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE training_functions (
    training_function_id INT AUTO_INCREMENT PRIMARY KEY,
    mandatory TINYINT(1) NOT NULL DEFAULT 0,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    training_id INT NOT NULL,
    function_id INT NOT NULL,
    UNIQUE KEY uq_training_functions (training_id, function_id)
) ENGINE=InnoDB;

CREATE TABLE employees_certificates (
    employee_training_id INT AUTO_INCREMENT PRIMARY KEY,
    obtained_date DATE,
    expiry_date DATE,
    status VARCHAR(50),
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    training_id INT NOT NULL,
    employee_id INT NOT NULL,
    training_center_id INT
) ENGINE=InnoDB;

CREATE TABLE companies (
    company_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    phone VARCHAR(30),
    bank VARCHAR(100),
    info VARCHAR(255),
    logo VARCHAR(255),
    email VARCHAR(150),
    facebook VARCHAR(255),
    linkedin VARCHAR(255),
    youtube VARCHAR(255),
    address_id INT
) ENGINE=InnoDB;

CREATE TABLE categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE criteria (
    criteria_id INT AUTO_INCREMENT PRIMARY KEY,
    criteria_name VARCHAR(150) NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    description VARCHAR(255),
    category_id INT
) ENGINE=InnoDB;

CREATE TABLE criteria_functions (
    criteria_function_id INT AUTO_INCREMENT PRIMARY KEY,
    function_id INT NOT NULL,
    criteria_id INT NOT NULL,
    UNIQUE KEY uq_criteria_functions (function_id, criteria_id)
) ENGINE=InnoDB;

CREATE TABLE objectives (
    objective_id INT AUTO_INCREMENT PRIMARY KEY,
    objective_name VARCHAR(150) NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    description VARCHAR(255)
) ENGINE=InnoDB;

CREATE TABLE evaluations (
    evaluation_id INT AUTO_INCREMENT PRIMARY KEY,
    evaluation_name VARCHAR(150) NOT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    evaluation_date DATE,
    period_start DATE,
    period_end DATE,
    global_score DECIMAL(5,2),
    comment VARCHAR(255),
    global_score_set TINYINT(1) NOT NULL DEFAULT 0,
    evaluator_id INT,
    employee_id INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE evaluation_criteria (
    evaluation_criteria_id INT AUTO_INCREMENT PRIMARY KEY,
    note DECIMAL(5,2),
    comment VARCHAR(255),
    self_evaluation DECIMAL(5,2),
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    evaluation_id INT NOT NULL,
    criteria_id INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE evaluation_objectives (
    evaluation_objective_id INT AUTO_INCREMENT PRIMARY KEY,
    evaluation_objective_name VARCHAR(150),
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    objective_id INT NOT NULL,
    evaluation_id INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE candidates (
    candidate_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    birth_date DATE,
    email VARCHAR(150),
    phone VARCHAR(30),
    status VARCHAR(50),
    is_active TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE job_offers (
    job_offer_id INT AUTO_INCREMENT PRIMARY KEY,
    job_offer_name VARCHAR(150) NOT NULL,
    email VARCHAR(150),
    contact VARCHAR(150),
    create_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    duration VARCHAR(100),
    publish_start_date DATE,
    publish_end_date DATE,
    status VARCHAR(50),
    number_of_open_positions INT,
    profil VARCHAR(255),
    job_description TEXT,
    comments TEXT,
    requirements TEXT,
    function_id INT,
    city_id INT
) ENGINE=InnoDB;

CREATE TABLE job_offers_candidates (
    job_offer_candidate_id INT AUTO_INCREMENT PRIMARY KEY,
    job_offer_name VARCHAR(150),
    application_date DATE,
    motivation TEXT,
    job_offer_id INT NOT NULL,
    candidate_id INT NOT NULL
) ENGINE=InnoDB;

ALTER TABLE addresses
    ADD CONSTRAINT fk_addresses_city FOREIGN KEY (city_id) REFERENCES cities(city_id);

ALTER TABLE roles_authorization
    ADD CONSTRAINT fk_roles_authorization_role FOREIGN KEY (role_id) REFERENCES roles(role_id),
    ADD CONSTRAINT fk_roles_authorization_authorization FOREIGN KEY (authorization_id) REFERENCES authorizations(authorization_id);

ALTER TABLE employees
    ADD CONSTRAINT fk_employees_address FOREIGN KEY (address_id) REFERENCES addresses(address_id),
    ADD CONSTRAINT fk_employees_role FOREIGN KEY (role_id) REFERENCES roles(role_id);

ALTER TABLE departments
    ADD CONSTRAINT fk_departments_head_employee FOREIGN KEY (department_head_id) REFERENCES employees(employee_id);

ALTER TABLE department_heads
    ADD CONSTRAINT fk_department_heads_department FOREIGN KEY (department_id) REFERENCES departments(department_id),
    ADD CONSTRAINT fk_department_heads_superior FOREIGN KEY (superior_id) REFERENCES employees(employee_id);

ALTER TABLE employee_departments
    ADD CONSTRAINT fk_employee_departments_department FOREIGN KEY (department_id) REFERENCES departments(department_id),
    ADD CONSTRAINT fk_employee_departments_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id);

ALTER TABLE employee_superiors
    ADD CONSTRAINT fk_employee_superiors_superior FOREIGN KEY (superior_id) REFERENCES employees(employee_id),
    ADD CONSTRAINT fk_employee_superiors_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id);

ALTER TABLE employees_functions
    ADD CONSTRAINT fk_employees_functions_function FOREIGN KEY (function_id) REFERENCES functions(function_id),
    ADD CONSTRAINT fk_employees_functions_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id);

ALTER TABLE contracts
    ADD CONSTRAINT fk_contracts_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id);

ALTER TABLE plannings
    ADD CONSTRAINT fk_plannings_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    ADD CONSTRAINT fk_plannings_department FOREIGN KEY (department_id) REFERENCES departments(department_id);

ALTER TABLE planning_employees
    ADD CONSTRAINT fk_planning_employees_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    ADD CONSTRAINT fk_planning_employees_planning FOREIGN KEY (planning_id) REFERENCES plannings(planning_id);

ALTER TABLE absences
    ADD CONSTRAINT fk_absences_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id);

ALTER TABLE training_centers
    ADD CONSTRAINT fk_training_centers_address FOREIGN KEY (address_id) REFERENCES addresses(address_id);

ALTER TABLE training_functions
    ADD CONSTRAINT fk_training_functions_training FOREIGN KEY (training_id) REFERENCES trainings(training_id),
    ADD CONSTRAINT fk_training_functions_function FOREIGN KEY (function_id) REFERENCES functions(function_id);

ALTER TABLE employees_certificates
    ADD CONSTRAINT fk_employees_certificates_training FOREIGN KEY (training_id) REFERENCES trainings(training_id),
    ADD CONSTRAINT fk_employees_certificates_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    ADD CONSTRAINT fk_employees_certificates_training_center FOREIGN KEY (training_center_id) REFERENCES training_centers(training_center_id);

ALTER TABLE companies
    ADD CONSTRAINT fk_companies_address FOREIGN KEY (address_id) REFERENCES addresses(address_id);

ALTER TABLE criteria
    ADD CONSTRAINT fk_criteria_category FOREIGN KEY (category_id) REFERENCES categories(category_id);

ALTER TABLE criteria_functions
    ADD CONSTRAINT fk_criteria_functions_function FOREIGN KEY (function_id) REFERENCES functions(function_id),
    ADD CONSTRAINT fk_criteria_functions_criteria FOREIGN KEY (criteria_id) REFERENCES criteria(criteria_id);

ALTER TABLE evaluations
    ADD CONSTRAINT fk_evaluations_evaluator FOREIGN KEY (evaluator_id) REFERENCES employees(employee_id),
    ADD CONSTRAINT fk_evaluations_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id);

ALTER TABLE evaluation_criteria
    ADD CONSTRAINT fk_evaluation_criteria_evaluation FOREIGN KEY (evaluation_id) REFERENCES evaluations(evaluation_id),
    ADD CONSTRAINT fk_evaluation_criteria_criteria FOREIGN KEY (criteria_id) REFERENCES criteria(criteria_id);

ALTER TABLE evaluation_objectives
    ADD CONSTRAINT fk_evaluation_objectives_objective FOREIGN KEY (objective_id) REFERENCES objectives(objective_id),
    ADD CONSTRAINT fk_evaluation_objectives_evaluation FOREIGN KEY (evaluation_id) REFERENCES evaluations(evaluation_id);

ALTER TABLE job_offers
    ADD CONSTRAINT fk_job_offers_function FOREIGN KEY (function_id) REFERENCES functions(function_id),
    ADD CONSTRAINT fk_job_offers_city FOREIGN KEY (city_id) REFERENCES cities(city_id);

ALTER TABLE job_offers_candidates
    ADD CONSTRAINT fk_job_offers_candidates_job_offer FOREIGN KEY (job_offer_id) REFERENCES job_offers(job_offer_id),
    ADD CONSTRAINT fk_job_offers_candidates_candidate FOREIGN KEY (candidate_id) REFERENCES candidates(candidate_id);

CREATE INDEX idx_employees_name ON employees(last_name, first_name);
CREATE INDEX idx_absences_dates ON absences(start_date, end_date);
CREATE INDEX idx_planning_employees_date ON planning_employees(date);
CREATE INDEX idx_contracts_dates ON contracts(start_date, end_date);
CREATE INDEX idx_job_offers_status ON job_offers(status);

INSERT INTO roles (name, is_active) VALUES
('ADMIN', 1),
('RH', 1),
('MANAGER', 1),
('EMPLOYEE', 1);

INSERT INTO authorizations (name, is_active) VALUES
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

INSERT INTO functions (function_name, description, is_active) VALUES
('Infirmier', 'Fonction medicale', 1),
('Medecin', 'Fonction medicale', 1),
('Responsable RH', 'Gestion des ressources humaines', 1),
('Manager', 'Responsable d equipe', 1);

INSERT INTO categories (category_name) VALUES
('Competences techniques'),
('Competences relationnelles'),
('Organisation'),
('Objectifs');
