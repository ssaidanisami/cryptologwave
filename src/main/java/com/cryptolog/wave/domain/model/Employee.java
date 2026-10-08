package com.cryptolog.wave.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record Employee(
        Long id,
        String firstName,
        String lastName,
        String email,
        String department,
        String position,
        BigDecimal salary,
        LocalDate hireDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
