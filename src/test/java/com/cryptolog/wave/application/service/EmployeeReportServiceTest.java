package com.cryptolog.wave.application.service;

import com.cryptolog.wave.domain.model.Employee;
import com.cryptolog.wave.domain.port.output.EmployeeReportPort;
import com.cryptolog.wave.domain.port.output.EmployeeRepositoryPort;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeReportServiceTest {

    @Mock
    private EmployeeRepositoryPort employeeRepositoryPort;

    @Mock
    private EmployeeReportPort employeeReportPort;

    @InjectMocks
    private EmployeeReportService employeeReportService;

    private Employee sampleEmployee;

    @BeforeEach
    void setUp() {
        sampleEmployee = new Employee(
                1L,
                "John",
                "Doe",
                "john.doe@example.com",
                "Engineering",
                "Software Engineer",
                new BigDecimal("75000.00"),
                LocalDate.of(2023, 1, 15),
                LocalDateTime.of(2023, 1, 15, 9, 0),
                LocalDateTime.of(2023, 1, 15, 9, 0)
        );
    }

    @Test
    @DisplayName("generateEmployeesPdfReport should fetch all employees and call report port to generate PDF")
    void shouldGenerateEmployeesPdfReport() {
        byte[] expectedPdf = new byte[]{1, 2, 3, 4, 5};
        List<Employee> employees = List.of(sampleEmployee);

        when(employeeRepositoryPort.findAll()).thenReturn(employees);
        when(employeeReportPort.generateEmployeePdfReport(employees)).thenReturn(expectedPdf);

        byte[] result = employeeReportService.generateEmployeesPdfReport();

        assertThat(result).isNotNull().isEqualTo(expectedPdf);
        verify(employeeRepositoryPort).findAll();
        verify(employeeReportPort).generateEmployeePdfReport(employees);
    }

    @Test
    @DisplayName("generateEmployeesPdfReport with pagination should fetch paginated employees and call report port to generate PDF")
    void shouldGenerateEmployeesPdfReportWithPagination() {
        byte[] expectedPdf = new byte[]{1, 2, 3, 4, 5};
        List<Employee> employees = List.of(sampleEmployee);
        Page<Employee> page = new PageImpl<>(employees, PageRequest.of(0, 5, Sort.by("id").ascending()), 1);

        Pageable expectedPageable = PageRequest.of(0, 5, Sort.by("id").ascending());
        when(employeeRepositoryPort.findAll(expectedPageable)).thenReturn(page);
        when(employeeReportPort.generateEmployeePdfReport(employees)).thenReturn(expectedPdf);

        byte[] result = employeeReportService.generateEmployeesPdfReport(0, 5, "id", "asc");

        assertThat(result).isNotNull().isEqualTo(expectedPdf);
        verify(employeeRepositoryPort).findAll(expectedPageable);
        verify(employeeReportPort).generateEmployeePdfReport(employees);
    }
}
