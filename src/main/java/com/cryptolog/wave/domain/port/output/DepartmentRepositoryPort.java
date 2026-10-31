package com.cryptolog.wave.domain.port.output;

import com.cryptolog.wave.domain.model.Department;
import java.util.List;
import java.util.Optional;

public interface DepartmentRepositoryPort {

    Department save(Department department);

    Optional<Department> findById(Long id);

    Optional<Department> findByNameIgnoreCase(String name);

    List<Department> findAll();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    void delete(Department department);
}
