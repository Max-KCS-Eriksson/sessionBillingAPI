# Session Billing API

REST-first Spring Boot API for session billing workflows.

Session Billing API is a backend MVP for tracking billable customer sessions,
service rates, bookings, and invoices. The project started as a CLI prototype
and now exposes the core workflow through REST endpoints.

The implemented system is centered on REST workflows for:

- customers
- services and hourly rates
- service offerings and versions
- session types and versions
- bookings
- invoices

## What Works Today

- Spring Boot application using Java 21.
- JPA repositories for customer, service, booking, and invoice data.
- MySQL-backed persistence configuration.
- REST endpoints for listing customers, services, service offerings, session types, bookings, and invoices.
- REST create, replace, patch, and delete operations for customers and services.
- REST create and version operations for service offerings.
- REST create and version operations for session types.
- REST create and status transition operations for bookings.
- REST invoice generation from completed bookings.
- REST delete protection for unpaid invoices.
- Customer, service, booking, and invoice REST workflows now route through service layers.
- Controller and service tests covering the current REST surface.

## Current REST Surface

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
- `POST /session-types`
- `POST /session-types/{name}/versions`
- `GET /bookings`
- `POST /bookings`
- `PATCH /bookings/{id}/status`
- `GET /invoices`
- `DELETE /invoices/{id}`

## Running Locally

Requirements:

- Java 21
- Maven or the included Maven wrapper
- MySQL database named `SessionBillingAPI`
- A local-only `src/main/resources/application.properties` created from `src/main/resources/application.example.properties`

Run the Spring Boot application:

```bash
./mvnw spring-boot:run
```

## Testing

Run the test suite with:

```bash
./mvnw test
```

## Project Direction

The goal is to continue hardening this REST API for a realistic billing
workflow:

- manage customers
- manage service offerings
- record completed sessions
- generate invoices from unbilled sessions
- prevent the same completed session from being billed twice
- track invoice payment status

The project is intentionally scoped as a backend API, not a full accounting
product.
