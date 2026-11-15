package com.cryptolog.wave.domain.port.input;

public interface EmployeeReportUseCase {

    byte[] generateEmployeesPdfReport();

    byte[] generateEmployeesPdfReport(int page, int size, String sortBy, String sortDir);
}
