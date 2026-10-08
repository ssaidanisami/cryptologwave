package com.cryptolog.wave.application.service;

import com.cryptolog.wave.domain.dto.EmployeeRequest;
import com.cryptolog.wave.domain.dto.EmployeeResponse;
import com.cryptolog.wave.domain.dto.PageResponse;
import com.cryptolog.wave.domain.exception.DuplicateResourceException;
import com.cryptolog.wave.domain.exception.ResourceNotFoundException;
import com.cryptolog.wave.domain.mapper.EmployeeMapper;
import com.cryptolog.wave.domain.model.Employee;
import com.cryptolog.wave.domain.port.output.EmployeeRepositoryPort;
import java.math.BigDecimal;
import java.time.LocalDate;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepositoryPort employeeRepositoryPort;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee sampleEmployee;
    private EmployeeRequest sampleRequest;
    private EmployeeResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleEmployee = new Employee(
                1L,
                "John",
                "Doe",
                "john.doe@example.com",
                "Engineering",
                "Software Engineer",
                new BigDecimal("80000.00"),
                LocalDate.of(2023, 1, 10),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        sampleRequest = new EmployeeRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "Engineering",
                "Software Engineer",
                new BigDecimal("80000.00"),
                LocalDate.of(2023, 1, 10)
        );

        sampleResponse = new EmployeeResponse(
                1L,
                "John",
                "Doe",
                "john.doe@example.com",
                "Engineering",
                "Software Engineer",
                new BigDecimal("80000.00"),
                LocalDate.of(2023, 1, 10),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Should create employee successfully when email is unique")
    void shouldCreateEmployeeSuccessfully() {
        when(employeeRepositoryPort.existsByEmail(sampleRequest.email())).thenReturn(false);
        when(employeeMapper.toDomain(sampleRequest)).thenReturn(sampleEmployee);
        when(employeeRepositoryPort.save(sampleEmployee)).thenReturn(sampleEmployee);
        when(employeeMapper.toResponse(sampleEmployee)).thenReturn(sampleResponse);

        EmployeeResponse response = employeeService.createEmployee(sampleRequest);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("john.doe@example.com");
        verify(employeeRepositoryPort).save(sampleEmployee);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email already exists during creation")
    void shouldThrowDuplicateResourceExceptionWhenEmailExists() {
        when(employeeRepositoryPort.existsByEmail(sampleRequest.email())).thenReturn(true);

        assertThatThrownBy(() -> employeeService.createEmployee(sampleRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("john.doe@example.com");
    }

    @Test
    @DisplayName("Should return employee by ID when found")
    void shouldReturnEmployeeById() {
        when(employeeRepositoryPort.findById(1L)).thenReturn(Optional.of(sampleEmployee));
        when(employeeMapper.toResponse(sampleEmployee)).thenReturn(sampleResponse);

        EmployeeResponse response = employeeService.getEmployeeById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.firstName()).isEqualTo("John");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when employee ID does not exist")
    void shouldThrowResourceNotFoundExceptionWhenIdNotFound() {
        when(employeeRepositoryPort.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should return paginated list of employees")
    void shouldReturnPaginatedEmployees() {
        Page<Employee> page = new PageImpl<>(List.of(sampleEmployee));
        when(employeeRepositoryPort.findAll(any(Pageable.class))).thenReturn(page);
        when(employeeMapper.toResponse(sampleEmployee)).thenReturn(sampleResponse);

        PageResponse<EmployeeResponse> response = employeeService.getAllEmployees(0, 10, "id", "asc");

        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
        assertThat(response.totalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should update employee successfully")
    void shouldUpdateEmployeeSuccessfully() {
        Employee updatedEmployee = new Employee(
                1L,
                "John",
                "Doe",
                "john.doe@example.com",
                "Engineering",
                "Software Engineer",
                new BigDecimal("80000.00"),
                LocalDate.of(2023, 1, 10),
                sampleEmployee.createdAt(),
                sampleEmployee.updatedAt()
        );

        when(employeeRepositoryPort.findById(1L)).thenReturn(Optional.of(sampleEmployee));
        when(employeeRepositoryPort.existsByEmailAndIdNot(sampleRequest.email(), 1L)).thenReturn(false);
        when(employeeMapper.updateDomain(sampleEmployee, sampleRequest)).thenReturn(updatedEmployee);
        when(employeeRepositoryPort.save(updatedEmployee)).thenReturn(updatedEmployee);
        when(employeeMapper.toResponse(updatedEmployee)).thenReturn(sampleResponse);

        EmployeeResponse response = employeeService.updateEmployee(1L, sampleRequest);

        assertThat(response).isNotNull();
        verify(employeeMapper).updateDomain(sampleEmployee, sampleRequest);
        verify(employeeRepositoryPort).save(updatedEmployee);
    }

    @Test
    @DisplayName("Should delete employee when ID exists")
    void shouldDeleteEmployeeSuccessfully() {
        when(employeeRepositoryPort.findById(1L)).thenReturn(Optional.of(sampleEmployee));

        employeeService.deleteEmployee(1L);

        verify(employeeRepositoryPort).delete(sampleEmployee);
    }
}
