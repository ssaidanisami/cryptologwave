package com.cryptolog.wave.infrastructure.adapter.output.persistence.repository;

import com.cryptolog.wave.infrastructure.adapter.output.persistence.entity.EmployeeJpaEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataEmployeeRepository
        extends JpaRepository<EmployeeJpaEntity, Long>, JpaSpecificationExecutor<EmployeeJpaEntity> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByDepartment_NameIgnoreCase(String department);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM EmployeeJpaEntity e WHERE e.department.id = :departmentId")
    boolean existsByDepartmentId(@Param("departmentId") Long departmentId);

    Optional<EmployeeJpaEntity> findByEmail(String email);

    Page<EmployeeJpaEntity> findByDepartment_NameIgnoreCase(String department, Pageable pageable);

    Page<EmployeeJpaEntity> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName,
            String lastName,
            Pageable pageable
    );
}
