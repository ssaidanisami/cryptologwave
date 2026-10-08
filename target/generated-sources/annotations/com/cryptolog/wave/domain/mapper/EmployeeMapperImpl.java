package com.cryptolog.wave.domain.mapper;

import com.cryptolog.wave.domain.dto.EmployeeRequest;
import com.cryptolog.wave.domain.dto.EmployeeResponse;
import com.cryptolog.wave.domain.model.Employee;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-06T15:27:49+0200",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12 (Oracle Corporation)"
)
@Component
public class EmployeeMapperImpl implements EmployeeMapper {

    @Override
    public Employee toDomain(EmployeeRequest request) {
        if ( request == null ) {
            return null;
        }

        String firstName = null;
        String lastName = null;
        String email = null;
        String department = null;
        String position = null;
        BigDecimal salary = null;
        LocalDate hireDate = null;

        firstName = request.firstName();
        lastName = request.lastName();
        email = request.email();
        department = request.department();
        position = request.position();
        salary = request.salary();
        hireDate = request.hireDate();

        Long id = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;

        Employee employee = new Employee( id, firstName, lastName, email, department, position, salary, hireDate, createdAt, updatedAt );

        return employee;
    }

    @Override
    public EmployeeResponse toResponse(Employee employee) {
        if ( employee == null ) {
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

        id = employee.id();
        firstName = employee.firstName();
        lastName = employee.lastName();
        email = employee.email();
        department = employee.department();
        position = employee.position();
        salary = employee.salary();
        hireDate = employee.hireDate();
        createdAt = employee.createdAt();
        updatedAt = employee.updatedAt();

        EmployeeResponse employeeResponse = new EmployeeResponse( id, firstName, lastName, email, department, position, salary, hireDate, createdAt, updatedAt );

        return employeeResponse;
    }

    @Override
    public Employee updateDomain(Employee existing, EmployeeRequest request) {
        if ( existing == null && request == null ) {
            return null;
        }

        Long id = null;
        LocalDateTime createdAt = null;
        LocalDateTime updatedAt = null;
        if ( existing != null ) {
            id = existing.id();
            createdAt = existing.createdAt();
            updatedAt = existing.updatedAt();
        }
        String firstName = null;
        String lastName = null;
        String email = null;
        String department = null;
        String position = null;
        BigDecimal salary = null;
        LocalDate hireDate = null;
        if ( request != null ) {
            firstName = request.firstName();
            lastName = request.lastName();
            email = request.email();
            department = request.department();
            position = request.position();
            salary = request.salary();
            hireDate = request.hireDate();
        }

        Employee employee = new Employee( id, firstName, lastName, email, department, position, salary, hireDate, createdAt, updatedAt );

        return employee;
    }
}
