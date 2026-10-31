package com.cryptolog.wave.infrastructure.adapter.output.persistence.mapper;

import com.cryptolog.wave.domain.model.Employee;
import com.cryptolog.wave.infrastructure.adapter.output.persistence.entity.EmployeeJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmployeePersistenceMapper {

    @Mapping(target = "department", ignore = true)
    EmployeeJpaEntity toJpaEntity(Employee employee);

    @Mapping(target = "department", source = "department.name")
    Employee toDomainEntity(EmployeeJpaEntity jpaEntity);
}
