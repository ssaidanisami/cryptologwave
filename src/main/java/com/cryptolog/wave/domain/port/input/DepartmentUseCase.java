package com.cryptolog.wave.domain.port.input;

import com.cryptolog.wave.domain.dto.DepartmentRequest;
import com.cryptolog.wave.domain.dto.DepartmentResponse;
import java.util.List;

public interface DepartmentUseCase {

    DepartmentResponse createDepartment(DepartmentRequest request);

    DepartmentResponse getDepartmentById(Long id);

    DepartmentResponse getDepartmentByName(String name);

    List<DepartmentResponse> getAllDepartments();

    DepartmentResponse updateDepartment(Long id, DepartmentRequest request);

    void deleteDepartment(Long id);
}
