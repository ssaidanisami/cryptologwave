package com.cryptolog.wave.infrastructure.adapter.input.rest;

import com.cryptolog.wave.domain.port.input.EmployeeReportUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Employee Reports", description = "Endpoints for generating employee reports and documents")
public class EmployeeReportRestController {

    private final EmployeeReportUseCase employeeReportUseCase;

    public EmployeeReportRestController(EmployeeReportUseCase employeeReportUseCase) {
        this.employeeReportUseCase = employeeReportUseCase;
    }

    @GetMapping(
            value = {"/api/reports/employees", "/api/v1/reports/employees"},
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    @Operation(
            summary = "Generate Employee PDF Report",
            description = "Generates and downloads a JasperReports PDF containing employees in tabular format with optional pagination and sorting"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee PDF report generated successfully"),
            @ApiResponse(responseCode = "500", description = "Failed to generate report")
    })
    public ResponseEntity<byte[]> getEmployeesPdfReport(
            @Parameter(description = "Page index (zero-based)")
            @RequestParam(required = false) Integer page,
            @Parameter(description = "Number of items per page")
            @RequestParam(required = false) Integer size,
            @Parameter(description = "Field name to sort by")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        byte[] pdfReport;
        if (page != null || size != null) {
            int pageIndex = (page != null) ? page : 0;
            int pageSize = (size != null) ? size : 10;
            pdfReport = employeeReportUseCase.generateEmployeesPdfReport(pageIndex, pageSize, sortBy, sortDir);
        } else {
            pdfReport = employeeReportUseCase.generateEmployeesPdfReport();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename("employees-report.pdf")
                        .build()
        );
        headers.setContentLength(pdfReport.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfReport);
    }
}
