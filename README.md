# MoneyMissint API

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.6-brightgreen?style=flat-square&logo=springboot)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue?style=flat-square&logo=docker)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14-336791?style=flat-square&logo=postgresql)

MoneyMissint es una API RESTful escalable diseñada para la gestión de finanzas personales. Implementa una arquitectura limpia para rastrear ingresos, gastos y categorías, asegurada mediante autenticación basada en tokens (JWT) y desplegable vía Docker.

## Características Principales

* **Arquitectura:** Diseño en capas (Controller, Service, Repository) siguiendo principios SOLID.
* **Seguridad:** Autenticación Stateless con JWT y Spring Security.
* **Lógica de Negocio:** Validación de transacciones y cálculo optimizado de estadísticas mensuales.
* **Infraestructura:** Configuración completa para despliegue en contenedores con Docker Compose.
* **Calidad de Código:** Cobertura de pruebas de integración utilizando JUnit 5, MockMvc y H2.
* **Documentación:** API documentada automáticamente con OpenAPI / Swagger.

## Stack Tecnológico

* **Lenguaje:** Java 21 (OpenJDK)
* **Framework:** Spring Boot 3
* **Base de Datos:** PostgreSQL 14 (Producción), H2 (Testing)
* **Construcción:** Maven
* **Seguridad:** JJWT, Spring Security

## Requisitos

* Docker Desktop (Recomendado)
* Git
* Java JDK 21 (Solo para ejecución manual sin Docker)

## Instalación y Despliegue

### Opción A: Docker Compose (Recomendada)

Esta opción levanta la aplicación y la base de datos PostgreSQL en una red virtual aislada.

1.  **Clonar el repositorio:**
    ```bash
    git clone [https://github.com/emiliorevv/MoneyMissint.git](https://github.com/emiliorevv/MoneyMissint.git)
    cd MoneyMissint
    ```

2.  **Construir y ejecutar:**
    ```bash
    docker compose up --build
    ```

3.  **Verificar despliegue:**
    * Documentación API: http://localhost:8080/swagger-ui.html
    * Health Check: http://localhost:8080/actuator/health

### Opción B: Ejecución Manual

1.  Asegúrese de tener una instancia de PostgreSQL ejecutándose en el puerto `5432`.
2.  Configure las variables de entorno en su IDE o sistema:
    * `SPRING_DATASOURCE_URL`
    * `SPRING_DATASOURCE_USERNAME`
    * `SPRING_DATASOURCE_PASSWORD`
    * `JWT_SECRET`
3.  Ejecute la aplicación:
    ```bash
    ./mvnw spring-boot:run
    ```

## Ejecución de Pruebas

El proyecto incluye una suite de tests de integración para validar el flujo completo de la aplicación.

```bash
./mvnw test
```

## Endpoints Principales

La API expone los siguientes recursos. Los endpoints marcados como 'Privado' requieren un token JWT válido en el encabezado `Authorization: Bearer <token>`.

| Verbo  | Endpoint                                | Descripción                                      | Acceso  |
| :----- | :-------------------------------------- | :----------------------------------------------- | :------ |
| POST   | `/api/v1/auth/register`                 | Registra un nuevo usuario en el sistema          | Público |
| POST   | `/api/v1/auth/login`                    | Autentica credenciales y devuelve token JWT      | Público |
| GET    | `/api/v1/transactions`                  | Lista transacciones con paginación               | Privado |
| POST   | `/api/v1/transactions`                  | Crea un nuevo ingreso o gasto                    | Privado |
| GET    | `/api/v1/transactions/{id}`             | Obtiene el detalle de una transacción única      | Privado |
| PUT    | `/api/v1/transactions/{id}`             | Modifica los datos de una transacción existente  | Privado |
| DELETE | `/api/v1/transactions/{id}`             | Elimina (soft-delete) una transacción            | Privado |
| GET    | `/api/v1/transactions/monthly-stats`    | Devuelve el resumen financiero del mes actual    | Privado |
| GET    | `/api/v1/categories`                    | Lista las categorías disponibles para gastos     | Privado |

## Estructura del Proyecto

El código sigue una arquitectura en capas para asegurar la separación de responsabilidades:

```text
src/main/java/com/example/moneymissint
├── config          # Configuraciones globales (Security, OpenAPI, CORS)
├── controller      # Capa REST: Maneja las peticiones HTTP y respuestas
├── dto             # Objetos de Transferencia de Datos (Request/Response)
├── exceptions      # Manejo global de errores y excepciones personalizadas
├── model           # Entidades JPA que representan las tablas de la BD
├── repository      # Interfaces que extienden JpaRepository para acceso a datos
├── security        # Lógica de autenticación, filtros JWT y UserDetails
├── service         # Lógica de negocio, validaciones y cálculos
└── utils           # Clases auxiliares y componentes estáticos
