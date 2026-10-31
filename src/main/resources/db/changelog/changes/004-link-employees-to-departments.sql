--liquibase formatted sql

--changeset cryptolog:004-add-department-id-to-employees-mysql dbms:mysql,mariadb
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'employees' AND COLUMN_NAME = 'department_id'
ALTER TABLE employees ADD COLUMN department_id BIGINT NULL;
--rollback ALTER TABLE employees DROP COLUMN department_id;

--changeset cryptolog:004-add-department-id-to-employees-h2 dbms:h2
ALTER TABLE employees ADD COLUMN IF NOT EXISTS department_id BIGINT NULL;
--rollback ALTER TABLE employees DROP COLUMN IF EXISTS department_id;

--changeset cryptolog:004-backfill-departments-from-employees
--validCheckSum: 9:f46a8a39428dd77c0939a25739e6388f
--validCheckSum: 9:1691e57fb4436df782dec0af683797ac
--validCheckSum: ANY
INSERT INTO departments (name, description, created_at, updated_at)
SELECT DISTINCT e.department, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM employees e
WHERE e.department IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM departments d WHERE d.name = e.department);

UPDATE employees
SET department_id = (SELECT d.id FROM departments d WHERE d.name = employees.department)
WHERE department_id IS NULL;
--rollback UPDATE employees SET department_id = NULL;

--changeset cryptolog:004-enforce-department-fk-mysql dbms:mysql,mariadb
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA = DATABASE() AND TABLE_NAME = 'employees' AND CONSTRAINT_NAME = 'fk_employees_department' AND CONSTRAINT_TYPE = 'FOREIGN KEY'
ALTER TABLE employees MODIFY COLUMN department_id BIGINT NOT NULL;
ALTER TABLE employees ADD CONSTRAINT fk_employees_department FOREIGN KEY (department_id) REFERENCES departments (id);
--rollback ALTER TABLE employees DROP FOREIGN KEY fk_employees_department;
--rollback ALTER TABLE employees MODIFY COLUMN department_id BIGINT NULL;

--changeset cryptolog:004-enforce-department-fk-h2 dbms:h2
ALTER TABLE employees ALTER COLUMN department_id BIGINT NOT NULL;
ALTER TABLE employees ADD CONSTRAINT fk_employees_department_h2 FOREIGN KEY (department_id) REFERENCES departments (id);
CREATE INDEX IF NOT EXISTS idx_employees_department_id ON employees (department_id);
--rollback ALTER TABLE employees DROP CONSTRAINT fk_employees_department_h2;
--rollback ALTER TABLE employees ALTER COLUMN department_id BIGINT NULL;

--changeset cryptolog:004-drop-legacy-department-index-mysql dbms:mysql,mariadb
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'employees' AND INDEX_NAME = 'idx_employees_department'
ALTER TABLE employees DROP INDEX idx_employees_department;
--rollback CREATE INDEX idx_employees_department ON employees (department);

--changeset cryptolog:004-drop-legacy-department-index-h2 dbms:h2
DROP INDEX IF EXISTS idx_employees_department;
--rollback CREATE INDEX idx_employees_department ON employees (department);

--changeset cryptolog:004-drop-legacy-department-column-mysql dbms:mysql,mariadb
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'employees' AND COLUMN_NAME = 'department'
ALTER TABLE employees DROP COLUMN department;
--rollback ALTER TABLE employees ADD COLUMN department VARCHAR(100) NULL;

--changeset cryptolog:004-drop-legacy-department-column-h2 dbms:h2
ALTER TABLE employees DROP COLUMN IF EXISTS department;
--rollback ALTER TABLE employees ADD COLUMN department VARCHAR(100) NULL;
