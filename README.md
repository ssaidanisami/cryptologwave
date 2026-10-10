# CryptologWave

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

**CryptologWave** is an enterprise-grade RESTful Employee Management microservice built with **Java 21**, **Spring Boot 3.4**, and **Hexagonal Architecture (Ports and Adapters)**. It features OAuth2 / OpenID Connect resource server security, database versioning with Liquibase, MySQL persistence, and integration testing with WireMock.

---

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
  - [1. Clone Repository](#1-clone-repository)
  - [2. Start Infrastructure with Docker Compose](#2-start-infrastructure-with-docker-compose)
  - [3. Run Application Locally](#3-run-application-locally)
  - [4. Build & Run via Docker](#4-build--run-via-docker)
- [Configuration & Profiles](#configuration--profiles)
- [API Documentation & Endpoints](#api-documentation--endpoints)
  - [OpenAPI / Swagger UI](#openapi--swagger-ui)
  - [REST Endpoints](#rest-endpoints)
  - [Sample Payloads](#sample-payloads)
- [Security & Authentication](#security--authentication)
- [Database Migrations](#database-migrations)
- [Testing](#testing)
- [Project Structure](#project-structure)

---

## Features

- **Hexagonal Architecture (Ports & Adapters)**: Strict separation of domain logic, application use cases, and infrastructure adapters (REST & JPA).
- **CRUD & Search APIs**: Complete operations for employee management including pagination, sorting, department filtering, and name search.
- **OAuth2 / OIDC Security**: Spring Security Resource Server validating JWT tokens against an OpenID Connect identity provider (Keycloak).
- **Database Versioning**: Liquibase changelogs for automated, repeatable schema and seed data management.
- **Docker Compose Setup**: Preconfigured environment for MySQL 8.4, Apache Kafka (KRaft mode), Kafka UI, and Keycloak.
- **OpenAPI 3 / Swagger**: Interactive API documentation generated with `springdoc-openapi`.
- **Validation & Error Handling**: Comprehensive request validation (`jakarta.validation`) and RFC 7807 `ProblemDetail` error responses.
- **MapStruct**: Type-safe, high-performance DTO/Entity mappings.

---

## Architecture

The project adheres to **Hexagonal Architecture** principles:

```
src/main/java/com/cryptolog/wave/
├── application/           # Application Service Layer (orchestrates domain use cases)
│   └── service/
├── config/                # Configurations (Security, OpenAPI, etc.)
│   └── security/
├── domain/                # Core Domain (Business rules, models, DTOs, ports, exceptions)
│   ├── dto/
│   ├── exception/
│   ├── mapper/
│   ├── model/
│   └── port/
│       ├── input/        # Driver / Use Case interfaces
│       └── output/       # Driven / Repository interfaces
└── infrastructure/        # Adapters
    └── adapter/
        ├── input/rest/   # Inbound REST Controller & Exception Handler
        └── output/       # Outbound Persistence (Spring Data JPA, Entities, Mappers)
```

---

## Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.4.3
- **Data & Persistence**: Spring Data JPA, Hibernate, MySQL 8.4, H2 (test)
- **Database Migrations**: Liquibase
- **Security**: Spring Security 6, OAuth2 Resource Server (JWT)
- **Mapping**: MapStruct 1.6.3
- **API Specs**: SpringDoc OpenAPI 2.8.5 (Swagger UI)
- **Containerization**: Docker, Docker Compose
- **Testing**: JUnit 5, Mockito, Spring Security Test, WireMock 3.12.0

---

## Prerequisites

- **Java JDK 21+**
- **Maven 3.9+** (or use included `./mvnw` / `mvnw.cmd`)
- **Docker & Docker Compose**

---

## Getting Started

### 1. Clone Repository

```bash
git clone https://github.com/your-username/CryptologWave.git
cd CryptologWave
```

### 2. Start Infrastructure with Docker Compose

Launch MySQL, Keycloak, Kafka, and Kafka UI:

```bash
docker compose up -d mysql kafka kafka-ui keycloak
```

| Service | Port | Description |
|---|---|---|
| **MySQL** | `3307` (mapped from `3306`) | Database |
| **Keycloak** | `8081` | IAM / OIDC Provider (admin/admin) |
| **Kafka** | `9092`, `29092` | Event broker |
| **Kafka UI** | `8085` | Web dashboard for Kafka |

### 3. Run Application Locally

Run the Spring Boot application using Maven:

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

By default, the application runs on **port 8082** using the `dev` profile.

### 4. Build & Run via Docker

To run the entire stack including the application container:

```bash
docker compose up --build -d
```

---

## Configuration & Profiles

The application supports multiple profiles defined in `src/main/resources/`:

- `dev` (`application-dev.yml`): Default development profile with local MySQL and debug logs.
- `uat` (`application-uat.yml`): User acceptance testing profile.
- `prod` (`application-prod.yml`): Production configuration with production-grade settings and connection pools.

### Key Environment Variables

| Variable | Default (Dev) | Description |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` | Active Spring profile |
| `PORT` | `8082` | HTTP Server port |
| `DB_HOST` | `localhost` | MySQL host |
| `DB_PORT` | `3306` (or `3307` via compose) | MySQL port |
| `DB_NAME` | `cryptologwave_dev` | Database name |
| `DB_USER` | `root` | Database username |
| `DB_PASSWORD` | `root` | Database password |
| `OIDC_ISSUER_URI` | `http://localhost:8081/realms/cryptolog` | OIDC Issuer URL for JWT validation |

---

## API Documentation & Endpoints

### OpenAPI / Swagger UI

Interactive API documentation and schema inspection are available at:

- **Swagger UI**: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
- **OpenAPI JSON Spec**: [http://localhost:8082/api-docs](http://localhost:8082/api-docs)

### REST Endpoints

All endpoints are rooted under `/api/v1/employees` and require a valid Bearer token.

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/employees` | Create a new employee |
| `GET` | `/api/v1/employees/{id}` | Retrieve employee by ID |
| `GET` | `/api/v1/employees` | List employees (paginated & sorted) |
| `GET` | `/api/v1/employees/by-department` | Filter employees by department (paginated) |
| `GET` | `/api/v1/employees/search` | Search employees by name query (paginated) |
| `PUT` | `/api/v1/employees/{id}` | Update an existing employee |
| `DELETE` | `/api/v1/employees/{id}` | Delete an employee by ID |

#### Query Parameters for Pagination & Sorting

- `page` (default: `0`): Page index (0-based)
- `size` (default: `10`): Items per page
- `sortBy` (default: `id`): Field to sort by (e.g. `firstName`, `salary`, `hireDate`)
- `sortDir` (default: `asc`): Sort direction (`asc` or `desc`)

### Sample Payloads

#### Create / Update Employee Request (`POST` / `PUT`)

```json
{
  "firstName": "Alice",
  "lastName": "Smith",
  "email": "alice.smith@example.com",
  "department": "Engineering",
  "position": "Senior Software Engineer",
  "salary": 95000.00,
  "hireDate": "2023-03-15"
}
```

#### Employee Response (`200 OK` / `201 Created`)

```json
{
  "id": 1,
  "firstName": "Alice",
  "lastName": "Smith",
  "email": "alice.smith@example.com",
  "department": "Engineering",
  "position": "Senior Software Engineer",
  "salary": 95000.00,
  "hireDate": "2023-03-15",
  "createdAt": "2024-01-01T10:00:00Z",
  "updatedAt": "2024-01-01T10:00:00Z"
}
```

---

## Security & Authentication

- The API is secured as an **OAuth 2.0 Resource Server** using JWT tokens.
- Public endpoints (no token required):
  - Swagger UI & OpenAPI docs (`/swagger-ui/**`, `/api-docs/**`, `/v3/api-docs/**`)
  - Actuator health check & info (`/actuator/health`, `/actuator/info`)
- To invoke protected endpoints, provide a valid Bearer token in the `Authorization` header:

```http
Authorization: Bearer <your_jwt_token>
```

---

## Database Migrations

Database schema and seed data are automatically managed with **Liquibase**:

- `src/main/resources/db/changelog/db.changelog-master.yaml`: Master changelog.
- `001-create-employees-table.sql`: Schema creation for the `employees` table.
- `002-insert-sample-employees.sql`: Initial sample seed dataset.

---

## Testing

The project includes unit tests, integration tests, slice tests, and WireMock external API mocks.

Run the test suite with:

```bash
# Windows
.\mvnw.cmd test

# Linux / macOS
./mvnw test
```

### Test Scope
- **Domain & Application Services**: Unit tests for business logic (`EmployeeServiceTest`).
- **REST Controller**: Integration tests with mock MVC (`EmployeeRestControllerIntegrationTest`).
- **Security**: Security filter chain and JWT converter verification (`SecurityConfigTest`, `SecurityFilterChainIntegrationTest`, `JwtAuthoritiesConverterTest`).
- **Profiles**: Profile loading and configuration tests (`ProfilesConfigurationTest`).
- **WireMock**: Mocking external services (`ExternalApiWireMockTest`).

---

## Project Structure

```
CryptologWave/
├── docker-compose.yml
├── Dockerfile
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/cryptolog/wave/
│   │   │   ├── application/service/
│   │   │   ├── config/
│   │   │   ├── domain/
│   │   │   └── infrastructure/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-uat.yml
│   │       ├── application-prod.yml
│   │       └── db/changelog/
│   └── test/
│       ├── java/com/cryptolog/wave/
│       └── resources/
```

---

## License

This project is licensed under the Apache License 2.0 - see the LICENSE details.
