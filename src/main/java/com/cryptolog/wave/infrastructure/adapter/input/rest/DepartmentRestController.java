package com.cryptolog.wave.infrastructure.adapter.input.rest;

import com.cryptolog.wave.domain.dto.DepartmentRequest;
import com.cryptolog.wave.domain.dto.DepartmentResponse;
import com.cryptolog.wave.domain.port.input.DepartmentUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/departments")
@Tag(name = "Departments", description = "Endpoints for department reference data management")
public class DepartmentRestController {

    private final DepartmentUseCase departmentUseCase;

    public DepartmentRestController(DepartmentUseCase departmentUseCase) {
        this.departmentUseCase = departmentUseCase;
    }

    @PostMapping
    @Operation(summary = "Create a new department", description = "Creates a new department and returns the created resource")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Department created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error in request body"),
            @ApiResponse(responseCode = "409", description = "Department with same name already exists")
    })
    public ResponseEntity<DepartmentResponse> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        DepartmentResponse created = departmentUseCase.createDepartment(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(summary = "Get all departments", description = "Retrieves the full list of departments sorted by name")
    @ApiResponse(responseCode = "200", description = "List of departments retrieved successfully")
    public ResponseEntity<List<DepartmentResponse>> getAllDepartments() {
        return ResponseEntity.ok(departmentUseCase.getAllDepartments());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get department by ID", description = "Retrieves a department by its unique identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<DepartmentResponse> getDepartmentById(
            @Parameter(description = "ID of the department to retrieve", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(departmentUseCase.getDepartmentById(id));
    }

    @GetMapping("/by-name/{name}")
    @Operation(summary = "Get department by name", description = "Retrieves a department by its name (case-insensitive)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<DepartmentResponse> getDepartmentByName(
            @Parameter(description = "Name of the department to retrieve", required = true)
            @PathVariable String name) {
        return ResponseEntity.ok(departmentUseCase.getDepartmentByName(name));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a department", description = "Updates an existing department's details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Department updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Department not found"),
            @ApiResponse(responseCode = "409", description = "Name already in use by another department")
    })
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(departmentUseCase.updateDepartment(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a department", description = "Deletes a department that has no employees assigned")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Department deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Department still has employees assigned"),
            @ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentUseCase.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}
