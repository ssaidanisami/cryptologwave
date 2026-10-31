package com.cryptolog.wave.application.service;

import com.cryptolog.wave.domain.dto.DepartmentRequest;
import com.cryptolog.wave.domain.dto.DepartmentResponse;
import com.cryptolog.wave.domain.exception.DuplicateResourceException;
import com.cryptolog.wave.domain.exception.ResourceNotFoundException;
import com.cryptolog.wave.domain.mapper.DepartmentMapper;
import com.cryptolog.wave.domain.model.Department;
import com.cryptolog.wave.domain.port.output.DepartmentRepositoryPort;
import com.cryptolog.wave.domain.port.output.EmployeeRepositoryPort;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepositoryPort departmentRepositoryPort;

    @Mock
    private EmployeeRepositoryPort employeeRepositoryPort;

    @Mock
    private DepartmentMapper departmentMapper;

    @InjectMocks
    private DepartmentService departmentService;

    private Department sampleDepartment;
    private DepartmentRequest sampleRequest;
    private DepartmentResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleDepartment = new Department(
                1L,
                "Engineering",
                "Engineering Department",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        sampleRequest = new DepartmentRequest(
                "Engineering",
                "Engineering Department"
        );

        sampleResponse = new DepartmentResponse(
                1L,
                "Engineering",
                "Engineering Department",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Should create department successfully when name is unique")
    void shouldCreateDepartmentSuccessfully() {
        when(departmentRepositoryPort.existsByNameIgnoreCase(sampleRequest.name())).thenReturn(false);
        when(departmentMapper.toDomain(sampleRequest)).thenReturn(sampleDepartment);
        when(departmentRepositoryPort.save(sampleDepartment)).thenReturn(sampleDepartment);
        when(departmentMapper.toResponse(sampleDepartment)).thenReturn(sampleResponse);

        DepartmentResponse response = departmentService.createDepartment(sampleRequest);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Engineering");
        verify(departmentRepositoryPort).save(sampleDepartment);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when department name already exists")
    void shouldThrowDuplicateResourceExceptionWhenNameExists() {
        when(departmentRepositoryPort.existsByNameIgnoreCase(sampleRequest.name())).thenReturn(true);

        assertThatThrownBy(() -> departmentService.createDepartment(sampleRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Engineering");
    }

    @Test
    @DisplayName("Should return department by ID when found")
    void shouldReturnDepartmentById() {
        when(departmentRepositoryPort.findById(1L)).thenReturn(Optional.of(sampleDepartment));
        when(departmentMapper.toResponse(sampleDepartment)).thenReturn(sampleResponse);

        DepartmentResponse response = departmentService.getDepartmentById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should return department by name when found")
    void shouldReturnDepartmentByName() {
        when(departmentRepositoryPort.findByNameIgnoreCase("Engineering")).thenReturn(Optional.of(sampleDepartment));
        when(departmentMapper.toResponse(sampleDepartment)).thenReturn(sampleResponse);

        DepartmentResponse response = departmentService.getDepartmentByName("Engineering");

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Engineering");
    }

    @Test
    @DisplayName("Should return all departments")
    void shouldReturnAllDepartments() {
        when(departmentRepositoryPort.findAll()).thenReturn(List.of(sampleDepartment));
        when(departmentMapper.toResponse(sampleDepartment)).thenReturn(sampleResponse);

        List<DepartmentResponse> responses = departmentService.getAllDepartments();

        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("Should update department successfully")
    void shouldUpdateDepartmentSuccessfully() {
        Department updatedDepartment = new Department(
                1L,
                "Engineering",
                "Updated Description",
                sampleDepartment.createdAt(),
                sampleDepartment.updatedAt()
        );

        when(departmentRepositoryPort.findById(1L)).thenReturn(Optional.of(sampleDepartment));
        when(departmentRepositoryPort.existsByNameIgnoreCaseAndIdNot(sampleRequest.name(), 1L)).thenReturn(false);
        when(departmentMapper.updateDomain(sampleDepartment, sampleRequest)).thenReturn(updatedDepartment);
        when(departmentRepositoryPort.save(updatedDepartment)).thenReturn(updatedDepartment);
        when(departmentMapper.toResponse(updatedDepartment)).thenReturn(sampleResponse);

        DepartmentResponse response = departmentService.updateDepartment(1L, sampleRequest);

        assertThat(response).isNotNull();
        verify(departmentRepositoryPort).save(updatedDepartment);
    }

    @Test
    @DisplayName("Should delete department when ID exists and no employees are assigned")
    void shouldDeleteDepartmentWhenNoEmployeesAssigned() {
        when(departmentRepositoryPort.findById(1L)).thenReturn(Optional.of(sampleDepartment));
        when(employeeRepositoryPort.existsByDepartmentId(1L)).thenReturn(false);

        departmentService.deleteDepartment(1L);

        verify(departmentRepositoryPort).delete(sampleDepartment);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when deleting department with assigned employees")
    void shouldThrowExceptionWhenDeletingDepartmentWithEmployees() {
        when(departmentRepositoryPort.findById(1L)).thenReturn(Optional.of(sampleDepartment));
        when(employeeRepositoryPort.existsByDepartmentId(1L)).thenReturn(true);

        assertThatThrownBy(() -> departmentService.deleteDepartment(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Department 'Engineering' cannot be deleted because employees are still assigned to it");

        verify(departmentRepositoryPort, never()).delete(sampleDepartment);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when department ID does not exist on delete")
    void shouldThrowResourceNotFoundExceptionOnDeleteWhenNotFound() {
        when(departmentRepositoryPort.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.deleteDepartment(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(departmentRepositoryPort, never()).delete(sampleDepartment);
    }
}
