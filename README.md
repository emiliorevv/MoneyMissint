# MoneyMissint API

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.6-brightgreen?style=flat-square&logo=springboot)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue?style=flat-square&logo=docker)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14-336791?style=flat-square&logo=postgresql)

MoneyMissint is a scalable RESTful API designed for personal finance management. It implements a clean architecture to track income, expenses, and categories, secured by token-based authentication (JWT) and deployable via Docker.

## Key Features

* **Architecture:** Layered design (Controller, Service, Repository) following SOLID principles.
* **Security:** Stateless authentication using JWT and Spring Security.
* **Business Logic:** Transaction validation and optimized calculation of monthly statistics.
* **Infrastructure:** Full configuration for containerized deployment using Docker Compose.
* **Code Quality:** Integration test coverage using JUnit 5, MockMvc, and H2.
* **Documentation:** API automatically documented with OpenAPI / Swagger.

## Tech Stack

* **Language:** Java 21 (OpenJDK)
* **Framework:** Spring Boot 3
* **Database:** PostgreSQL 14 (Production), H2 (Testing)
* **Build Tool:** Maven
* **Security:** JJWT, Spring Security

## Prerequisites

* Docker Desktop (Recommended)
* Git
* Java JDK 21 (Only for manual execution without Docker)

## Installation and Deployment

### Option A: Docker Compose (Recommended)

This option spins up both the application and the PostgreSQL database within an isolated virtual network.

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/emiliorevv/MoneyMissint.git](https://github.com/emiliorevv/MoneyMissint.git)
    cd MoneyMissint
    ```

2.  **Build and run:**
    ```bash
    docker compose up --build
    ```

3.  **Verify deployment:**
    * API Documentation: http://localhost:8080/swagger-ui.html
    * Health Check: http://localhost:8080/actuator/health

### Option B: Manual Execution

1.  Ensure you have a PostgreSQL instance running on port `5432`.
2.  Configure the environment variables in your IDE or system:
    * `SPRING_DATASOURCE_URL`
    * `SPRING_DATASOURCE_USERNAME`
    * `SPRING_DATASOURCE_PASSWORD`
    * `JWT_SECRET`
3.  Run the application:
    ```bash
    ./mvnw spring-boot:run
    ```

## Running Tests

The project includes a suite of integration tests to validate the full application flow.

```bash
./mvnw test

```
## Main endpoints

The API exposes the following resources. Endpoints marked as 'Private' require a valid JWT token in the Authorization: Bearer <token> header.

| Verb   | Endpoint                                | Description                                      | Access  |
| :----- | :-------------------------------------- | :----------------------------------------------- | :------ |
| POST   | `/api/v1/auth/register`                 | Registers a new user in the system               | Public  |
| POST   | `/api/v1/auth/login`                    | Authenticates credentials and returns JWT token  | Public  |
| GET    | `/api/v1/transactions`                  | Lists transactions with pagination               | Private |
| POST   | `/api/v1/transactions`                  | Creates a new income or expense                  | Private |
| GET    | `/api/v1/transactions/{id}`             | Retrieves details of a specific transaction      | Private |
| PUT    | `/api/v1/transactions/{id}`             | Modifies data of an existing transaction         | Private |
| DELETE | `/api/v1/transactions/{id}`             | Removes (soft-delete) a transaction              | Private |
| GET    | `/api/v1/transactions/monthly-stats`    | Returns the monthly stats of the current month   | Private |
| GET    | `/api/v1/categories`                    | Lists available categories for operations        | Private |

## Project Structure

The code follows a layered architecture to ensure separation of concerns:

```text
src/main/java/com/example/moneymissint
├── config          # Global configurations (Security, OpenAPI, CORS)
├── controller      # REST Layer: Handles HTTP requests and responses
├── dto             # Data Transfer Objects (Request/Response)
├── exceptions      # Global error handling and custom exceptions
├── model           # JPA Entities mapping DB tables
├── repository      # Interfaces extending JpaRepository for data access
├── security        # Authentication logic, JWT filters, and UserDetails
├── service         # Business logic, validations, and calculations
└── utils           # Utility classes and helper components
