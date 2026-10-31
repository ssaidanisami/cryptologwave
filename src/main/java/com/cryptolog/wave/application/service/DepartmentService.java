package com.cryptolog.wave.application.service;

import com.cryptolog.wave.domain.dto.DepartmentRequest;
import com.cryptolog.wave.domain.dto.DepartmentResponse;
import com.cryptolog.wave.domain.exception.DuplicateResourceException;
import com.cryptolog.wave.domain.exception.ResourceNotFoundException;
import com.cryptolog.wave.domain.mapper.DepartmentMapper;
import com.cryptolog.wave.domain.model.Department;
import com.cryptolog.wave.domain.port.input.DepartmentUseCase;
import com.cryptolog.wave.domain.port.output.DepartmentRepositoryPort;
import com.cryptolog.wave.domain.port.output.EmployeeRepositoryPort;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DepartmentService implements DepartmentUseCase {

    private final DepartmentRepositoryPort departmentRepositoryPort;
    private final EmployeeRepositoryPort employeeRepositoryPort;
    private final DepartmentMapper departmentMapper;

    public DepartmentService(DepartmentRepositoryPort departmentRepositoryPort,
                             EmployeeRepositoryPort employeeRepositoryPort,
                             DepartmentMapper departmentMapper) {
        this.departmentRepositoryPort = departmentRepositoryPort;
        this.employeeRepositoryPort = employeeRepositoryPort;
        this.departmentMapper = departmentMapper;
    }

    @Override
    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepositoryPort.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("Department", "name", request.name());
        }

        var department = departmentMapper.toDomain(request);
        var saved = departmentRepositoryPort.save(department);
        return departmentMapper.toResponse(saved);
    }

    @Override
    public DepartmentResponse getDepartmentById(Long id) {
        return departmentMapper.toResponse(findDepartmentOrThrow(id));
    }

    @Override
    public DepartmentResponse getDepartmentByName(String name) {
        var department = departmentRepositoryPort.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "name", name));
        return departmentMapper.toResponse(department);
    }

    @Override
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepositoryPort.findAll().stream()
                .map(departmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        var existing = findDepartmentOrThrow(id);

        if (departmentRepositoryPort.existsByNameIgnoreCaseAndIdNot(request.name(), id)) {
            throw new DuplicateResourceException("Department", "name", request.name());
        }

        var updated = departmentMapper.updateDomain(existing, request);
        var saved = departmentRepositoryPort.save(updated);
        return departmentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        var department = findDepartmentOrThrow(id);
        if (employeeRepositoryPort.existsByDepartmentId(id)) {
            throw new IllegalArgumentException(
                    "Department '" + department.name() + "' cannot be deleted because employees are still assigned to it");
        }
        departmentRepositoryPort.delete(department);
    }

    private Department findDepartmentOrThrow(Long id) {
        return departmentRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }
}
