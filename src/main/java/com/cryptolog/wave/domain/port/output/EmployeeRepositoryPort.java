package com.cryptolog.wave.domain.port.output;

import com.cryptolog.wave.domain.model.Employee;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeRepositoryPort {

    Employee save(Employee employee);

    Optional<Employee> findById(Long id);

    Page<Employee> findAll(Pageable pageable);

    Page<Employee> findByDepartmentIgnoreCase(String department, Pageable pageable);

    Page<Employee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName,
            String lastName,
            Pageable pageable
    );

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    void delete(Employee employee);
}
