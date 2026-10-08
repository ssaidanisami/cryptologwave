package com.cryptolog.wave.infrastructure.adapter.output.persistence.mapper;

import com.cryptolog.wave.domain.model.Employee;
import com.cryptolog.wave.infrastructure.adapter.output.persistence.entity.EmployeeJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmployeePersistenceMapper {

    EmployeeJpaEntity toJpaEntity(Employee employee);

    Employee toDomainEntity(EmployeeJpaEntity jpaEntity);
}
