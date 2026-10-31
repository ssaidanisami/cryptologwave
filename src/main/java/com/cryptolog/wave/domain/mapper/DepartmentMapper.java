package com.cryptolog.wave.domain.mapper;

import com.cryptolog.wave.domain.dto.DepartmentRequest;
import com.cryptolog.wave.domain.dto.DepartmentResponse;
import com.cryptolog.wave.domain.model.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Department toDomain(DepartmentRequest request);

    DepartmentResponse toResponse(Department department);

    @Mapping(target = "id", source = "existing.id")
    @Mapping(target = "name", source = "request.name")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "createdAt", source = "existing.createdAt")
    @Mapping(target = "updatedAt", source = "existing.updatedAt")
    Department updateDomain(Department existing, DepartmentRequest request);
}
