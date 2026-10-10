package com.cryptolog.wave.infrastructure.adapter.output.persistence.mapper;

import com.cryptolog.wave.domain.model.Employee;
import com.cryptolog.wave.infrastructure.adapter.output.persistence.entity.EmployeeJpaEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-10T09:49:49+0200",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12 (Oracle Corporation)"
)
@Component
public class EmployeePersistenceMapperImpl implements EmployeePersistenceMapper {

    @Override
    public EmployeeJpaEntity toJpaEntity(Employee employee) {
        if ( employee == null ) {
            return null;
        }

        EmployeeJpaEntity employeeJpaEntity = new EmployeeJpaEntity();

        employeeJpaEntity.setId( employee.id() );
        employeeJpaEntity.setFirstName( employee.firstName() );
        employeeJpaEntity.setLastName( employee.lastName() );
        employeeJpaEntity.setEmail( employee.email() );
        employeeJpaEntity.setDepartment( employee.department() );
        employeeJpaEntity.setPosition( employee.position() );
        employeeJpaEntity.setSalary( employee.salary() );
        employeeJpaEntity.setHireDate( employee.hireDate() );
        employeeJpaEntity.setCreatedAt( employee.createdAt() );
        employeeJpaEntity.setUpdatedAt( employee.updatedAt() );

        return employeeJpaEntity;
    }

    @Override
    public Employee toDomainEntity(EmployeeJpaEntity jpaEntity) {
        if ( jpaEntity == null ) {
            return null;
        }

        Long id = null;
        String firstName = null;
        String lastName = null;
        String email = null;
        String department = null;
        String position = null;
        BigDecimal salary = null;
        LocalDate hireDate = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        id = jpaEntity.getId();
        firstName = jpaEntity.getFirstName();
        lastName = jpaEntity.getLastName();
        email = jpaEntity.getEmail();
        department = jpaEntity.getDepartment();
        position = jpaEntity.getPosition();
        salary = jpaEntity.getSalary();
        hireDate = jpaEntity.getHireDate();
        createdAt = jpaEntity.getCreatedAt();
        updatedAt = jpaEntity.getUpdatedAt();

        Employee employee = new Employee( id, firstName, lastName, email, department, position, salary, hireDate, createdAt, updatedAt );

        return employee;
    }
}
