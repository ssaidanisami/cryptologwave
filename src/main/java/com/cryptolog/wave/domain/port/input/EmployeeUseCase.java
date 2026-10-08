package com.cryptolog.wave.domain.port.input;

import com.cryptolog.wave.domain.dto.EmployeeRequest;
import com.cryptolog.wave.domain.dto.EmployeeResponse;
import com.cryptolog.wave.domain.dto.PageResponse;

public interface EmployeeUseCase {

    EmployeeResponse createEmployee(EmployeeRequest request);

    EmployeeResponse getEmployeeById(Long id);

    PageResponse<EmployeeResponse> getAllEmployees(int page, int size, String sortBy, String sortDir);

    PageResponse<EmployeeResponse> getEmployeesByDepartment(String department, int page, int size, String sortBy, String sortDir);

    PageResponse<EmployeeResponse> searchEmployeesByName(String query, int page, int size, String sortBy, String sortDir);

    EmployeeResponse updateEmployee(Long id, EmployeeRequest request);

    void deleteEmployee(Long id);
}
