package com.cryptolog.wave.infrastructure.adapter.output.report;

import com.cryptolog.wave.domain.model.Employee;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JasperEmployeeReportAdapterTest {

    private JasperEmployeeReportAdapter reportAdapter;

    @BeforeEach
    void setUp() {
        reportAdapter = new JasperEmployeeReportAdapter();
    }

    @Test
    @DisplayName("generateEmployeePdfReport with empty list should generate valid PDF document")
    void shouldGeneratePdfWithEmptyList() {
        byte[] pdfBytes = reportAdapter.generateEmployeePdfReport(List.of());

        assertThat(pdfBytes).isNotNull().isNotEmpty();
        // PDF files always start with %PDF- header (ASCII: 0x25, 0x50, 0x44, 0x46, 0x2D)
        String header = new String(pdfBytes, 0, Math.min(pdfBytes.length, 5));
        assertThat(header).isEqualTo("%PDF-");
    }

    @Test
    @DisplayName("generateEmployeePdfReport with employees should generate valid PDF document")
    void shouldGeneratePdfWithEmployees() {
        List<Employee> employees = List.of(
                new Employee(
                        1L,
                        "John",
                        "Doe",
                        "john.doe@example.com",
                        "Engineering",
                        "Senior Software Engineer",
                        new BigDecimal("95000.00"),
                        LocalDate.of(2022, 5, 10),
                        LocalDateTime.now(),
                        LocalDateTime.now()
                ),
                new Employee(
                        2L,
                        "Jane",
                        "Smith",
                        "jane.smith@example.com",
                        "Marketing",
                        "Marketing Manager",
                        new BigDecimal("82000.00"),
                        LocalDate.of(2023, 3, 1),
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )
        );

        byte[] pdfBytes = reportAdapter.generateEmployeePdfReport(employees);

        assertThat(pdfBytes).isNotNull().isNotEmpty();
        String header = new String(pdfBytes, 0, Math.min(pdfBytes.length, 5));
        assertThat(header).isEqualTo("%PDF-");
    }
}
