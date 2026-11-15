package com.cryptolog.wave.domain.port.output;

import com.cryptolog.wave.domain.model.Employee;
import java.util.List;

public interface EmployeeReportPort {

    byte[] generateEmployeePdfReport(List<Employee> employees);
}
