# Risk Assessment API

Risk Assessment API is an enterprise-style backend service that simulates customer risk evaluation workflows commonly found in financial institutions. The project demonstrates modern Java development practices, clean architecture, RESTful API design, automated testing, containerization, and API documentation.

---

## Overview

Financial institutions continuously evaluate customer risk before granting loans, extending credit, or offering financial products.

This project models a simplified risk assessment engine capable of:

- Managing customer information
- Calculating a customer risk score
- Assigning a risk classification
- Persisting assessment history
- Exposing REST endpoints for integration

Although simplified, the architecture mirrors common enterprise backend patterns used in banking environments.

---

## Technology Stack

- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Hibernate
- Jakarta Validation
- Maven
- PostgreSQL
- JUnit 5
- Mockito

---

## Architecture

The project follows a layered architecture.

```
Controller
      │
      ▼
Service Layer
      │
      ▼
Business Rules (Risk Engine)
      │
      ▼
Repository (JPA)
      │
      ▼
Database
```

The business logic is isolated from the REST layer, allowing the scoring engine to evolve independently from the API.

---

## Current Features

### Customer Management

- Create customer
- Retrieve customer
- List customers
- Update customer
- Delete customer

---

### Risk Assessment

Create a risk assessment based on customer information.

Current scoring factors include:

- Credit Score
- Annual Income
- Existing Debt
- Employment Status

The engine calculates:

- Numerical Risk Score
- Risk Category

Example:

| Score | Classification |
|--------|----------------|
| 90 | LOW |
| 65 | MEDIUM |
| 35 | HIGH |

(The scoring algorithm is intentionally simplified for demonstration.)

---

### Validation

The API validates incoming requests using Jakarta Validation.

Examples include:

- Required fields
- Positive numeric values
- Invalid payload detection

---

### Exception Handling

A centralized exception handler returns consistent error responses.

Example:

```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Validation Failed",
  "message": "...",
  "path": "/api/customers"
}
```

---

## Project Structure

```
src
 ├── controller
 ├── dto
 ├── entity
 ├── exception
 ├── repository
 ├── service
 └── config
```

This separation keeps responsibilities isolated and improves maintainability.

---

## Example Workflow

### Create Customer

```
POST /api/customers
```

↓

Customer stored in database.

↓

### Run Assessment

```
POST /api/risk-assessments
```

↓

Business rules calculate the score.

↓

Risk classification returned and persisted.

---

## Testing

The project includes unit tests covering:

- Risk scoring logic
- Service layer
- Validation scenarios

The objective is to ensure business rules remain reliable as the scoring model evolves.

---

## Design Principles

- SOLID principles
- Separation of concerns
- Layered architecture
- DTO pattern
- Dependency Injection
- Constructor injection
- Single Responsibility Principle

---

## Local Container Run

1. Copy `.env.example` to `.env`
2. Set strong values for `POSTGRES_PASSWORD`, `JWT_SECRET`, and (if enabled) admin credentials
3. Start the stack:

```bash
docker compose up --build
```

API runs on `http://localhost:8080`.

---

## Temporary VPS Access (by IP)

If you deploy without a domain, users can access the API directly through your VPS public IP:

- Base URL: `http://<YOUR_VPS_IP>:8080`
- Health: `http://<YOUR_VPS_IP>:8080/actuator/health`
- Swagger UI: `http://<YOUR_VPS_IP>:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://<YOUR_VPS_IP>:8080/v3/api-docs`

Important:

- Ensure port `8080` is open in your VPS firewall/security rules.
- This is HTTP (no TLS) and should be treated as temporary until a domain + HTTPS is configured.

---

## Future Improvements

Planned enhancements include:

- Audit logging
- Configurable scoring rules
- Integration tests using Testcontainers

---

## Why this Project?

This project was built to demonstrate backend engineering practices commonly found in enterprise financial systems.

It emphasizes:

- Clean architecture
- Business rule implementation
- RESTful API design
- Validation and error handling
- Maintainable code
- Testability
- Extensibility

These principles are especially relevant for software supporting operational risk management, financial services, and other mission-critical applications.

---

## Author

**Adriano Reckziegel**

Software Developer

Special interests:

- Financial systems
- Risk analysis
- Enterprise Java
- Backend architecture
- Database layer (SQL and NoSQL)
