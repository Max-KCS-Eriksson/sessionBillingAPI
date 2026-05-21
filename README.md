# Session Billing API

REST-first Spring Boot API for session billing workflows.

Session Billing API is an early backend for tracking billable customer
sessions, service rates, bookings, and invoices. The project started as a CLI
prototype and now exposes the core workflow through REST endpoints.

## Current Stage

This repository currently represents an active REST API migration stage.

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
- REST endpoints for listing customers, services, bookings, and invoices.
- REST create, replace, patch, and delete operations for customers and services.
- REST create, replace, patch, and delete operations for billing records.
- REST create and version operations for service offerings.
- REST create and version operations for session types.
- REST create and status transition operations for bookings.
- REST invoice generation from completed bookings.
- REST delete protection for unpaid invoices.
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
- `POST /session-types`
- `POST /session-types/{name}/versions`
- `GET /bookings`
- `POST /bookings`
- `PATCH /bookings/{id}/status`
- `GET /invoices`
- `DELETE /invoices/{id}`
- `GET /bills`
- `POST /bills`
- `PUT /bills/{customerIdentifier}/{bookedTime}`
- `PATCH /bills/{customerIdentifier}/{bookedTime}`
- `DELETE /bills/{customerIdentifier}/{bookedTime}`

## Running Locally

Requirements:

- Java 21
- Maven or the included Maven wrapper
- MySQL database named `SessionBillingAPI`

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

The project is intentionally scoped as a backend portfolio project, not a full
accounting product.
