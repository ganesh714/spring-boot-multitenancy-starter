# Spring Boot Multitenancy Starter Project

This project demonstrates a custom Spring Boot Starter that provides auto-configured, header-based multi-tenant data source routing.

## Modules

* `multitenancy-spring-boot-starter`: The reusable starter library containing the auto-configuration logic, dynamic data source, and interceptors.
* `demo-application`: A standard Spring Boot web application that consumes the starter to handle multiple tenants.

## How to Build

To build the project locally (both the starter and the demo application):

```bash
mvn clean install
```

## How to Run

The easiest way to run the application and its required PostgreSQL database is using Docker Compose.

```bash
docker-compose up -d --build
```

This command will:
1. Start a PostgreSQL container (`db`) and initialize three databases: `tenant1_db`, `tenant2_db`, and `tenant3_db`.
2. Build and start the Spring Boot application container (`app`).

## Verifying Functionality

Once the containers are up and running, you can test the multi-tenancy features.

**1. Create a user in Tenant 1**
```bash
curl -X POST -H "X-Tenant-ID: tenant1" -H "Content-Type: application/json" -d '{"name": "Alice", "email": "alice@example.com"}' http://localhost:8080/api/users
```

**2. Retrieve users from Tenant 1**
```bash
curl -H "X-Tenant-ID: tenant1" http://localhost:8080/api/users
```
*(Should return a list containing Alice)*

**3. Retrieve users from Tenant 2 (Data Isolation)**
```bash
curl -H "X-Tenant-ID: tenant2" http://localhost:8080/api/users
```
*(Should return an empty list)*

**4. Check missing tenant header**
```bash
curl http://localhost:8080/api/users
```
*(Should return 400 Bad Request)*

**5. Check invalid tenant ID**
```bash
curl -H "X-Tenant-ID: unknown" http://localhost:8080/api/users
```
*(Should return 404 Not Found)*

**6. View DataSources Health Check**
```bash
curl http://localhost:8080/actuator/health/datasources
```
*(Should show UP status for all 3 configured tenants)*
