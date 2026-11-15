package com.cryptolog.wave.application.service;

import com.cryptolog.wave.domain.model.Employee;
import com.cryptolog.wave.domain.port.input.EmployeeReportUseCase;
import com.cryptolog.wave.domain.port.output.EmployeeReportPort;
import com.cryptolog.wave.domain.port.output.EmployeeRepositoryPort;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EmployeeReportService implements EmployeeReportUseCase {

    private final EmployeeRepositoryPort employeeRepositoryPort;
    private final EmployeeReportPort employeeReportPort;

    public EmployeeReportService(
            EmployeeRepositoryPort employeeRepositoryPort,
            EmployeeReportPort employeeReportPort
    ) {
        this.employeeRepositoryPort = employeeRepositoryPort;
        this.employeeReportPort = employeeReportPort;
    }

    @Override
    public byte[] generateEmployeesPdfReport() {
        List<Employee> employees = employeeRepositoryPort.findAll();
        return employeeReportPort.generateEmployeePdfReport(employees);
    }

    @Override
    public byte[] generateEmployeesPdfReport(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        List<Employee> employees = employeeRepositoryPort.findAll(pageable).getContent();
        return employeeReportPort.generateEmployeePdfReport(employees);
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        String effectiveSortBy = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy;
        Sort sort = "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(effectiveSortBy).descending()
                : Sort.by(effectiveSortBy).ascending();
        return PageRequest.of(Math.max(0, page), Math.max(1, size), sort);
    }
}
