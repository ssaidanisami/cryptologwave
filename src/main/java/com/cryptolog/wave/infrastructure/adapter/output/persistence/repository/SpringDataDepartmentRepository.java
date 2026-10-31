package com.cryptolog.wave.infrastructure.adapter.output.persistence.repository;

import com.cryptolog.wave.infrastructure.adapter.output.persistence.entity.DepartmentJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataDepartmentRepository extends JpaRepository<DepartmentJpaEntity, Long> {

    Optional<DepartmentJpaEntity> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
