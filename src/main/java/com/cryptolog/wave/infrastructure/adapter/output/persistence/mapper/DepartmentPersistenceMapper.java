package com.cryptolog.wave.infrastructure.adapter.output.persistence.mapper;

import com.cryptolog.wave.domain.model.Department;
import com.cryptolog.wave.infrastructure.adapter.output.persistence.entity.DepartmentJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DepartmentPersistenceMapper {

    DepartmentJpaEntity toJpaEntity(Department department);

    Department toDomainEntity(DepartmentJpaEntity jpaEntity);
}
