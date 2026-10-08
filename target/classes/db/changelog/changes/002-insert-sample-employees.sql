--liquibase formatted sql

--changeset cryptolog:002-insert-sample-employees
INSERT INTO employees (first_name, last_name, email, department, position, salary, hire_date, created_at, updated_at)
VALUES ('Alice', 'Smith', 'alice.smith@example.com', 'Engineering', 'Senior Software Engineer', 85000.00, '2023-01-15', '2023-01-15 09:00:00', '2023-01-15 09:00:00');

INSERT INTO employees (first_name, last_name, email, department, position, salary, hire_date, created_at, updated_at)
VALUES ('Bob', 'Johnson', 'bob.johnson@example.com', 'Product', 'Product Manager', 78000.00, '2023-03-01', '2023-03-01 09:00:00', '2023-03-01 09:00:00');

INSERT INTO employees (first_name, last_name, email, department, position, salary, hire_date, created_at, updated_at)
VALUES ('Charlie', 'Brown', 'charlie.brown@example.com', 'Engineering', 'DevOps Engineer', 82000.00, '2023-06-10', '2023-06-10 09:00:00', '2023-06-10 09:00:00');

--rollback DELETE FROM employees WHERE email IN ('alice.smith@example.com', 'bob.johnson@example.com', 'charlie.brown@example.com');
