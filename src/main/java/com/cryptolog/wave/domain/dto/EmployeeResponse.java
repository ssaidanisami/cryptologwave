package com.cryptolog.wave.domain.dto;

import com.cryptolog.wave.domain.model.Employee;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Employee representation response")
public record EmployeeResponse(
        @Schema(example = "1", description = "Unique identifier of the employee")
        Long id,

        @Schema(example = "John", description = "First name")
        String firstName,

        @Schema(example = "Doe", description = "Last name")
        String lastName,

        @Schema(example = "john.doe@example.com", description = "Email address")
        String email,

        @Schema(example = "Engineering", description = "Department")
        String department,

        @Schema(example = "Software Engineer", description = "Job position")
        String position,

        @Schema(example = "75000.00", description = "Salary")
        BigDecimal salary,

        @Schema(example = "2024-01-15", description = "Date hired")
        LocalDate hireDate,

        @Schema(example = "2024-01-15T09:00:00", description = "Record creation timestamp")
        LocalDateTime createdAt,

        @Schema(example = "2024-01-15T09:00:00", description = "Record last update timestamp")
        LocalDateTime updatedAt
) {
    public static EmployeeResponse fromDomain(Employee employee) {
        if (employee == null) {
            return null;
        }
        return new EmployeeResponse(
                employee.id(),
                employee.firstName(),
                employee.lastName(),
                employee.email(),
                employee.department(),
                employee.position(),
                employee.salary(),
                employee.hireDate(),
                employee.createdAt(),
                employee.updatedAt()
        );
    }
}
