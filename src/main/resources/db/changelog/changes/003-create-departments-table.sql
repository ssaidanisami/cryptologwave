--liquibase formatted sql

--changeset cryptolog:003-create-departments-table
CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT pk_departments PRIMARY KEY (id),
    CONSTRAINT uk_departments_name UNIQUE (name)
);
--rollback DROP TABLE departments;

--changeset cryptolog:003-insert-sample-departments
INSERT INTO departments (name, description, created_at, updated_at)
VALUES ('Engineering', 'Builds and maintains the product', '2023-01-01 09:00:00', '2023-01-01 09:00:00');

INSERT INTO departments (name, description, created_at, updated_at)
VALUES ('Product', 'Defines the product roadmap and priorities', '2023-01-01 09:00:00', '2023-01-01 09:00:00');

INSERT INTO departments (name, description, created_at, updated_at)
VALUES ('Sales', 'Drives revenue and customer acquisition', '2023-01-01 09:00:00', '2023-01-01 09:00:00');

INSERT INTO departments (name, description, created_at, updated_at)
VALUES ('Human Resources', 'Manages people operations and recruiting', '2023-01-01 09:00:00', '2023-01-01 09:00:00');

INSERT INTO departments (name, description, created_at, updated_at)
VALUES ('Finance', 'Handles accounting, budgeting and payroll', '2023-01-01 09:00:00', '2023-01-01 09:00:00');
--rollback DELETE FROM departments WHERE name IN ('Engineering', 'Product', 'Sales', 'Human Resources', 'Finance');
