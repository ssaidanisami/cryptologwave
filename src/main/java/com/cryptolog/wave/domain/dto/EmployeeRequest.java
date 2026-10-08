package com.cryptolog.wave.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Payload for creating or updating an employee")
public record EmployeeRequest(
        @NotBlank(message = "First name is mandatory")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        @Schema(example = "John", description = "First name of the employee")
        String firstName,

        @NotBlank(message = "Last name is mandatory")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        @Schema(example = "Doe", description = "Last name of the employee")
        String lastName,

        @NotBlank(message = "Email is mandatory")
        @Email(message = "Email must be valid")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        @Schema(example = "john.doe@example.com", description = "Unique email address")
        String email,

        @NotBlank(message = "Department is mandatory")
        @Size(max = 100, message = "Department must not exceed 100 characters")
        @Schema(example = "Engineering", description = "Department the employee belongs to")
        String department,

        @NotBlank(message = "Position is mandatory")
        @Size(max = 100, message = "Position must not exceed 100 characters")
        @Schema(example = "Software Engineer", description = "Job title or position")
        String position,

        @NotNull(message = "Salary is mandatory")
        @Positive(message = "Salary must be strictly positive")
        @Schema(example = "75000.00", description = "Annual base salary in USD")
        BigDecimal salary,

        @NotNull(message = "Hire date is mandatory")
        @PastOrPresent(message = "Hire date cannot be in the future")
        @Schema(example = "2024-01-15", description = "Date of hiring (YYYY-MM-DD)")
        LocalDate hireDate
) {
}
