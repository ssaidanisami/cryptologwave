package com.cryptolog.wave.domain.port.output;

import com.cryptolog.wave.domain.dto.EmployeeResponse;

public interface EmployeeEventPublisherPort {

    void publishEmployeeCreated(EmployeeResponse employeeResponse);

    void publishEmployeeUpdated(EmployeeResponse employeeResponse);
}
