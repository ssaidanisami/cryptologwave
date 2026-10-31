package com.cryptolog.wave.infrastructure.adapter.output.persistence;

import com.cryptolog.wave.domain.model.Department;
import com.cryptolog.wave.domain.port.output.DepartmentRepositoryPort;
import com.cryptolog.wave.infrastructure.adapter.output.persistence.entity.DepartmentJpaEntity;
import com.cryptolog.wave.infrastructure.adapter.output.persistence.mapper.DepartmentPersistenceMapper;
import com.cryptolog.wave.infrastructure.adapter.output.persistence.repository.SpringDataDepartmentRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class DepartmentPersistenceAdapter implements DepartmentRepositoryPort {

    private final SpringDataDepartmentRepository springDataDepartmentRepository;
    private final DepartmentPersistenceMapper departmentPersistenceMapper;

    public DepartmentPersistenceAdapter(
            SpringDataDepartmentRepository springDataDepartmentRepository,
            DepartmentPersistenceMapper departmentPersistenceMapper
    ) {
        this.springDataDepartmentRepository = springDataDepartmentRepository;
        this.departmentPersistenceMapper = departmentPersistenceMapper;
    }

    @Override
    public Department save(Department department) {
        DepartmentJpaEntity jpaEntity = departmentPersistenceMapper.toJpaEntity(department);
        DepartmentJpaEntity savedEntity = springDataDepartmentRepository.save(jpaEntity);
        return departmentPersistenceMapper.toDomainEntity(savedEntity);
    }

    @Override
    public Optional<Department> findById(Long id) {
        return springDataDepartmentRepository.findById(id)
                .map(departmentPersistenceMapper::toDomainEntity);
    }

    @Override
    public Optional<Department> findByNameIgnoreCase(String name) {
        return springDataDepartmentRepository.findByNameIgnoreCase(name)
                .map(departmentPersistenceMapper::toDomainEntity);
    }

    @Override
    public List<Department> findAll() {
        return springDataDepartmentRepository.findAll(Sort.by("name").ascending()).stream()
                .map(departmentPersistenceMapper::toDomainEntity)
                .toList();
    }

    @Override
    public boolean existsByNameIgnoreCase(String name) {
        return springDataDepartmentRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public boolean existsByNameIgnoreCaseAndIdNot(String name, Long id) {
        return springDataDepartmentRepository.existsByNameIgnoreCaseAndIdNot(name, id);
    }

    @Override
    public void delete(Department department) {
        DepartmentJpaEntity jpaEntity = departmentPersistenceMapper.toJpaEntity(department);
        springDataDepartmentRepository.delete(jpaEntity);
    }
}
