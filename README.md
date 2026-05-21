# Session Billing API

Prototype stage: Spring Boot CLI billing registry with partial REST API surface.

Session Billing API is an early backend prototype for tracking billable customer
sessions, service rates, and billing records. The current codebase is not a
finished invoicing platform. It is an initial CLI prototype that is being
gradually migrated toward a REST-first Spring Boot API.

## Current Stage

This repository currently represents the initial prototype stage.

The implemented system is centered on a legacy command-line registry workflow
for:

- customers
- services and hourly rates
- billing records

A partial REST API exists alongside the CLI so the project can evolve
incrementally without removing the working prototype flow too early.

## What Works Today

- Spring Boot application using Java 21.
- JPA repositories for customer, service, and billing data.
- MySQL-backed persistence configuration.
- Optional CLI workflow for registry operations.
- REST endpoints for listing customers, services, and billing records.
- REST create, replace, patch, and delete operations for customers and services.
- REST create, replace, patch, and delete operations for billing records.
- REST create and version operations for service offerings.
- Customer, service, and billing REST workflows now route through service layers.
- Controller tests covering the current REST surface.

## Current REST Surface

The REST API is intentionally partial at this stage.

Available endpoints include:

- `GET /customers`
- `GET /customers/{customerIdentifier}`
- `POST /customers`
- `PUT /customers/{customerIdentifier}`
- `PATCH /customers/{customerIdentifier}`
- `DELETE /customers/{customerIdentifier}`
- `GET /services`
- `GET /services/{name}`
- `POST /services`
- `PUT /services/{name}`
- `PATCH /services/{name}`
- `DELETE /services/{name}`
- `POST /service-offerings`
- `POST /service-offerings/{name}/versions`
- `GET /bills`
- `POST /bills`
- `PUT /bills/{customerIdentifier}/{bookedTime}`
- `PATCH /bills/{customerIdentifier}/{bookedTime}`
- `DELETE /bills/{customerIdentifier}/{bookedTime}`

The billing workflow is still modeled with the legacy `Bill` concept. Future
iterations are expected to move toward bookings, completed sessions, invoices,
and duplicate-billing protection.

## Running Locally

Requirements:

- Java 21
- Maven or the included Maven wrapper
- MySQL database named `SessionBillingAPI`

Run the Spring Boot application:

```bash
./mvnw spring-boot:run
```

The CLI runner is disabled by default. To start the legacy CLI prototype flow,
enable it with:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--session-billing.cli.enabled=true
```

## Testing

Run the test suite with:

```bash
./mvnw test
```

## Project Direction

The goal is to evolve this prototype into a focused Spring Boot REST API for a
realistic billing workflow:

- manage customers
- manage service offerings
- record completed sessions
- generate invoices from unbilled sessions
- prevent the same completed session from being billed twice
- track invoice payment status

The project is intentionally scoped as a backend portfolio project, not a full
accounting product.
