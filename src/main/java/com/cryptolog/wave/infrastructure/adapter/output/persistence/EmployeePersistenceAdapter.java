package com.cryptolog.wave.infrastructure.adapter.output.persistence;

import com.cryptolog.wave.domain.model.Employee;
import com.cryptolog.wave.domain.port.output.EmployeeRepositoryPort;
import com.cryptolog.wave.infrastructure.adapter.output.persistence.entity.DepartmentJpaEntity;
import com.cryptolog.wave.infrastructure.adapter.output.persistence.entity.EmployeeJpaEntity;
import com.cryptolog.wave.infrastructure.adapter.output.persistence.mapper.EmployeePersistenceMapper;
import com.cryptolog.wave.infrastructure.adapter.output.persistence.repository.SpringDataDepartmentRepository;
import com.cryptolog.wave.infrastructure.adapter.output.persistence.repository.SpringDataEmployeeRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class EmployeePersistenceAdapter implements EmployeeRepositoryPort {

    private final SpringDataEmployeeRepository springDataEmployeeRepository;
    private final SpringDataDepartmentRepository springDataDepartmentRepository;
    private final EmployeePersistenceMapper employeePersistenceMapper;

    public EmployeePersistenceAdapter(
            SpringDataEmployeeRepository springDataEmployeeRepository,
            SpringDataDepartmentRepository springDataDepartmentRepository,
            EmployeePersistenceMapper employeePersistenceMapper
    ) {
        this.springDataEmployeeRepository = springDataEmployeeRepository;
        this.springDataDepartmentRepository = springDataDepartmentRepository;
        this.employeePersistenceMapper = employeePersistenceMapper;
    }

    @Override
    public Employee save(Employee employee) {
        EmployeeJpaEntity jpaEntity = employeePersistenceMapper.toJpaEntity(employee);
        if (employee.department() != null) {
            DepartmentJpaEntity departmentEntity = springDataDepartmentRepository
                    .findByNameIgnoreCase(employee.department().trim())
                    .orElseGet(() -> {
                        DepartmentJpaEntity newDept = new DepartmentJpaEntity();
                        newDept.setName(employee.department().trim());
                        return springDataDepartmentRepository.save(newDept);
                    });
            jpaEntity.setDepartment(departmentEntity);
        }
        EmployeeJpaEntity savedEntity = springDataEmployeeRepository.save(jpaEntity);
        return employeePersistenceMapper.toDomainEntity(savedEntity);
    }

    @Override
    public Optional<Employee> findById(Long id) {
        return springDataEmployeeRepository.findById(id)
                .map(employeePersistenceMapper::toDomainEntity);
    }

    @Override
    public Page<Employee> findAll(Pageable pageable) {
        return springDataEmployeeRepository.findAll(pageable)
                .map(employeePersistenceMapper::toDomainEntity);
    }

    @Override
    public Page<Employee> findByDepartmentIgnoreCase(String department, Pageable pageable) {
        return springDataEmployeeRepository.findByDepartment_NameIgnoreCase(department, pageable)
                .map(employeePersistenceMapper::toDomainEntity);
    }

    @Override
    public Page<Employee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName,
            String lastName,
            Pageable pageable
    ) {
        return springDataEmployeeRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(firstName, lastName, pageable)
                .map(employeePersistenceMapper::toDomainEntity);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataEmployeeRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, Long id) {
        return springDataEmployeeRepository.existsByEmailAndIdNot(email, id);
    }

    @Override
    public boolean existsByDepartmentId(Long departmentId) {
        return springDataEmployeeRepository.existsByDepartmentId(departmentId);
    }

    @Override
    public void delete(Employee employee) {
        if (employee.id() != null) {
            springDataEmployeeRepository.deleteById(employee.id());
        }
    }
}
