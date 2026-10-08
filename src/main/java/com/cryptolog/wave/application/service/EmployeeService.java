package com.cryptolog.wave.application.service;

import com.cryptolog.wave.domain.dto.EmployeeRequest;
import com.cryptolog.wave.domain.dto.EmployeeResponse;
import com.cryptolog.wave.domain.dto.PageResponse;
import com.cryptolog.wave.domain.exception.DuplicateResourceException;
import com.cryptolog.wave.domain.exception.ResourceNotFoundException;
import com.cryptolog.wave.domain.mapper.EmployeeMapper;
import com.cryptolog.wave.domain.model.Employee;
import com.cryptolog.wave.domain.port.input.EmployeeUseCase;
import com.cryptolog.wave.domain.port.output.EmployeeRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EmployeeService implements EmployeeUseCase {

    private final EmployeeRepositoryPort employeeRepositoryPort;
    private final EmployeeMapper employeeMapper;

    public EmployeeService(EmployeeRepositoryPort employeeRepositoryPort, EmployeeMapper employeeMapper) {
        this.employeeRepositoryPort = employeeRepositoryPort;
        this.employeeMapper = employeeMapper;
    }

    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        if (employeeRepositoryPort.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Employee", "email", request.email());
        }

        var employee = employeeMapper.toDomain(request);
        var saved = employeeRepositoryPort.save(employee);
        return employeeMapper.toResponse(saved);
    }

    @Override
    public EmployeeResponse getEmployeeById(Long id) {
        var employee = findEmployeeOrThrow(id);
        return employeeMapper.toResponse(employee);
    }

    @Override
    public PageResponse<EmployeeResponse> getAllEmployees(int page, int size, String sortBy, String sortDir) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        Page<EmployeeResponse> responsePage = employeeRepositoryPort.findAll(pageable)
                .map(employeeMapper::toResponse);
        return PageResponse.fromPage(responsePage);
    }

    @Override
    public PageResponse<EmployeeResponse> getEmployeesByDepartment(String department, int page, int size, String sortBy, String sortDir) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        Page<EmployeeResponse> responsePage = employeeRepositoryPort.findByDepartmentIgnoreCase(department, pageable)
                .map(employeeMapper::toResponse);
        return PageResponse.fromPage(responsePage);
    }

    @Override
    public PageResponse<EmployeeResponse> searchEmployeesByName(String query, int page, int size, String sortBy, String sortDir) {
        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        Page<EmployeeResponse> responsePage = employeeRepositoryPort
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(query, query, pageable)
                .map(employeeMapper::toResponse);
        return PageResponse.fromPage(responsePage);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        var existing = findEmployeeOrThrow(id);

        if (employeeRepositoryPort.existsByEmailAndIdNot(request.email(), id)) {
            throw new DuplicateResourceException("Employee", "email", request.email());
        }

        var updatedEmployee = employeeMapper.updateDomain(existing, request);
        var saved = employeeRepositoryPort.save(updatedEmployee);
        return employeeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        var employee = findEmployeeOrThrow(id);
        employeeRepositoryPort.delete(employee);
    }

    private Employee findEmployeeOrThrow(Long id) {
        return employeeRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }
}
