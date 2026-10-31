# CryptologWave - Claude Skills & Architectural Guidelines (`skills.md`)

This document defines the architectural skills, operational patterns, and engineering standards for Claude when working on the **CryptologWave** codebase.

---

## 1. System Overview & Architecture Paradigm

**CryptologWave** is an enterprise-grade RESTful microservice built with **Java 21**, **Spring Boot 3.4.3**, and **Hexagonal Architecture (Ports and Adapters / Clean Architecture)**.

```
                  ┌────────────────────────────────────────────────────────┐
                  │                 INFRASTRUCTURE (ADAPTERS)              │
                  │                                                        │
                  │   Inbound (Driver)              Outbound (Driven)      │
                  │   ┌────────────────────┐       ┌────────────────────┐  │
                  │   │ EmployeeController │       │ PersistenceAdapter │  │
                  │   └─────────┬──────────┘       └─────────▲──────────┘  │
                  │             │                            │             │
                  │             ▼                            │             │
                  │   ┌─────────────────────────────────────────────────┐  │
                  │   │            APPLICATION (USE CASES)              │  │
                  │   │   ┌─────────────────────────────────────────┐   │  │
                  │   │   │             EmployeeService             │   │  │
                  │   │   └─────┬─────────────────────────────▲─────┘   │  │
                  │   │         │                             │         │  │
                  │   │         ▼                             │         │  │
                  │   │   ┌───────────────┐         ┌─────────────────┐ │  │
                  │   │   │  Input Port   │         │   Output Port   │ │  │
                  │   │   │(EmployeeUseCase)│       │ (RepositoryPort)│ │  │
                  │   │   └───────────────┘         └─────────────────┘ │  │
                  │   │                                                 │  │
                  │   │   ┌─────────────────────────────────────────┐   │  │
                  │   │   │              DOMAIN CORE                │   │  │
                  │   │   │ • Models (Employee, Department)         │   │  │
                  │   │   │ • Exceptions (NotFound, Duplicate)      │   │  │
                  │   │   │ • Mappers (Domain DTO Mappers)          │   │  │
                  │   │   └─────────────────────────────────────────┘   │  │
                  │   └─────────────────────────────────────────────────┘  │
                  └────────────────────────────────────────────────────────┘
```

### Architectural Dependency Rule
- **Domain Core (`domain`)**: Pure Java. Zero dependency on Spring Framework, JPA/Hibernate, or Infrastructure packages.
- **Application (`application`)**: Depends only on `domain` (models, ports, exceptions, DTOs). Contains business workflows and use case implementations.
- **Infrastructure (`infrastructure`)**: Implements ports or drives input. Depends on `domain` and `application`.
- **Config (`config`)**: Security, OpenAPI, and cross-cutting Spring configurations.

---

## 2. Core Claude Skills Matrix

```
┌─────────────────────────────────┬────────────────────────────────────────────────────────────────┐
│ Skill ID                        │ Focus Area & Responsibilities                                  │
├─────────────────────────────────┼────────────────────────────────────────────────────────────────┤
│ skill-hexagonal-domain          │ Domain models, ports (input/output), business exceptions       │
│ skill-application-orchestration │ Use cases, transactional services, domain orchestration        │
│ skill-inbound-rest-adapter      │ REST controllers, OpenAPI 3 docs, RFC 7807 ProblemDetail       │
│ skill-outbound-persistence      │ Spring Data JPA entities, repositories, persistence mappers    │
│ skill-database-liquibase        │ Schema evolutions, versioned changelogs, rollback safety       │
│ skill-security-oauth2-jwt       │ Spring Security 6, JWT resource server, role mapping           │
│ skill-mapstruct-transformation  │ DTO ↔ Domain ↔ JPA Entity mapping with MapStruct 1.6           │
│ skill-testing-verification      │ Unit tests (Mockito), Slice tests, WireMock, MockMvc           │
└─────────────────────────────────┴────────────────────────────────────────────────────────────────┘
```

---

## 3. Detailed Skill Specifications

### Skill 1: `skill-hexagonal-domain`
**Purpose**: Maintain pure business logic and contracts isolated from frameworks.
- **Location**: `com.cryptolog.wave.domain.*`
- **Rules & Conventions**:
  - Domain models (`Employee`, `Department`) must be pure POJOs / Records.
  - Define driver interfaces in `domain.port.input` (e.g., `EmployeeUseCase`, `DepartmentUseCase`).
  - Define driven interfaces in `domain.port.output` (e.g., `EmployeeRepositoryPort`, `DepartmentRepositoryPort`).
  - Domain exceptions must extend `RuntimeException` and reside in `domain.exception` (e.g., `ResourceNotFoundException`, `DuplicateResourceException`).
  - No `@Entity`, `@Table`, `@RestController`, or `@Service` annotations inside domain models.

---

### Skill 2: `skill-application-orchestration`
**Purpose**: Implement business workflows coordinating domain models and ports.
- **Location**: `com.cryptolog.wave.application.service.*`
- **Rules & Conventions**:
  - Services implement input port interfaces (`implements EmployeeUseCase`).
  - Annotate with `@Service` and `@Transactional(readOnly = true)` by default; mark mutating methods with `@Transactional`.
  - Inject driven output ports (`EmployeeRepositoryPort`, `DepartmentRepositoryPort`) via constructor injection (Lombok or explicit constructors).
  - Perform domain business validation and throw specific domain exceptions when invariants fail.

---

### Skill 3: `skill-inbound-rest-adapter`
**Purpose**: Expose RESTful endpoints adhering to OpenAPI specifications and standard error handling.
- **Location**: `com.cryptolog.wave.infrastructure.adapter.input.rest.*`
- **Rules & Conventions**:
  - Controllers implement input REST contracts, annotated with `@RestController`, `@RequestMapping("/api/v1/...")`, and `@Validated`.
  - Use `@Valid` on request body payloads (`EmployeeRequest`, `DepartmentRequest`).
  - Document all endpoints using `@Tag`, `@Operation`, and `@ApiResponse` from `io.swagger.v3.oas.annotations`.
  - Handle errors uniformly in `GlobalExceptionHandler` returning `org.springframework.http.ProblemDetail` (RFC 7807 standard).
  - Return appropriate HTTP status codes: `200 OK`, `201 Created`, `204 No Content`, `400 Bad Request`, `404 Not Found`, `409 Conflict`.

---

### Skill 4: `skill-outbound-persistence`
**Purpose**: Bridge domain output ports with Spring Data JPA and relational database storage.
- **Location**: `com.cryptolog.wave.infrastructure.adapter.output.persistence.*`
- **Rules & Conventions**:
  - Keep JPA entities (`EmployeeJpaEntity`, `DepartmentJpaEntity`) in `entity/` package.
  - Use Spring Data interfaces (`SpringDataEmployeeRepository`, `SpringDataDepartmentRepository`) extending `JpaRepository` and `JpaSpecificationExecutor`.
  - Persistence adapters (`EmployeePersistenceAdapter`) implement output ports (`EmployeeRepositoryPort`) and convert between JPA entities and domain models using `EmployeePersistenceMapper`.

---

### Skill 5: `skill-database-liquibase`
**Purpose**: Safe, automated, and repeatable relational schema evolutions.
- **Location**: `src/main/resources/db/changelog/`
- **Rules & Conventions**:
  - Master file: `db.changelog-master.yaml` includes individual migration files sequentially.
  - Migrations are stored under `changes/` with strict numeric prefixes: `001-create-employees-table.sql`, `002-insert-sample-employees.sql`, etc.
  - Formatted SQL migrations must contain `--liquibase formatted sql` and `--changeset <author>:<id>`.
  - Never alter existing already-executed migrations; always write new forward migrations.

---

### Skill 6: `skill-security-oauth2-jwt`
**Purpose**: Secure endpoints with OAuth2 / OpenID Connect resource server authentication and fine-grained authorization.
- **Location**: `com.cryptolog.wave.config.security.*`
- **Rules & Conventions**:
  - Spring Security 6 Resource Server validates JWT tokens issued by Keycloak / OIDC identity provider.
  - `JwtAuthoritiesConverter` maps Keycloak realm access roles (`realm_access.roles`) and resource access roles to Spring Security `ROLE_` granted authorities.
  - Public endpoints (Swagger UI `/swagger-ui/**`, OpenAPI docs `/v3/api-docs/**`, Actuator `/actuator/health`) are explicitly permitted.
  - Protected API endpoints (`/api/v1/**`) require valid JWT authentication and role claims where configured.
  - Custom `ProblemDetailAuthenticationEntryPoint` and `ProblemDetailAccessDeniedHandler` format 401/403 errors into RFC 7807 `ProblemDetail`.

---

### Skill 7: `skill-mapstruct-transformation`
**Purpose**: Maintain type-safe, compile-time bidirectional data mapping across layers.
- **Location**: `domain.mapper.*`, `infrastructure.adapter.output.persistence.mapper.*`
- **Rules & Conventions**:
  - Annotate mappers with `@Mapper(componentModel = "spring")`.
  - Separate Domain Mappers (`EmployeeMapper`: DTO ↔ Domain Model) from Persistence Mappers (`EmployeePersistenceMapper`: Domain Model ↔ JPA Entity).
  - Explicitly define `@Mapping(target = "...", source = "...")` for field discrepancies and `@Mapping(target = "...", ignore = true)` for unmapped fields.

---

### Skill 8: `skill-testing-verification`
**Purpose**: Guarantee code correctness through multi-layered testing.
- **Location**: `src/test/java/com/cryptolog/wave/`
- **Test Categories**:
  - **Unit Tests**: Test application services in isolation with Mockito (`@ExtendWith(MockitoExtension.class)`).
  - **Controller / Integration Tests**: Slice tests using `@WebMvcTest` or `@SpringBootTest` with `@AutoConfigureMockMvc`.
  - **Security Tests**: Verify endpoint access, unauthorized rejections (401), and forbidden access (403) using `@WithMockUser` or JWT test helpers.
  - **External API Tests**: Simulate downstream/OIDC endpoints with WireMock (`@WireMockTest`).
  - **Profiles**: Use `application.yml` under `src/test/resources/` with in-memory H2 database for test execution.

---

## 4. Claude Decision & Action Protocols

When responding to tasks or modifying code in CryptologWave, follow these steps:

1. **Classify Intent**:
   - Feature Addition / Modification -> Identify affected Port, Domain Model, Service, REST Controller, and Persistence Adapter.
   - Database Change -> Create new Liquibase changelog script and update master changelog YAML.
   - Security Adjustment -> Update `SecurityConfig` or `JwtAuthoritiesConverter`.
2. **Preserve Architectural Boundaries**:
   - Never inject Spring Data repositories directly into REST controllers.
   - Never import Spring/JPA annotations into `com.cryptolog.wave.domain.*`.
3. **Validate Code**:
   - Ensure clean compilation with Maven: `./mvnw clean compile`.
   - Run unit and integration tests: `./mvnw test`.
4. **Adhere to Code Quality**:
   - Follow Java 21 features (pattern matching, records, var when readable).
   - Ensure proper validation annotations on incoming DTOs.
   - Preserve RFC 7807 `ProblemDetail` responses for error conditions.
