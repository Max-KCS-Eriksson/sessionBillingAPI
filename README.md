# Session Billing API

REST API for session billing workflows.

Session Billing API is for tracking billable customer sessions, service rates,
bookings, and invoices.

It models a small billing domain:

- customers who can be billed
- services with hourly rates
- service offerings and version history for price changes
- session types and version history for duration changes
- bookings tied to a customer and session type version
- invoices generated from completed bookings

The application uses Spring Boot, Spring Web, Spring Data JPA, and PostgreSQL.
REST controllers are kept thin and delegate to service-layer workflow classes.
Tests cover the current controller and service behavior.

## API Endpoints

### Customers

- `GET /customers` lists all customers.
- `GET /customers/{personalId}` fetches one customer by personal id.
- `POST /customers` creates a customer.
- `PUT /customers/{personalId}` fully replaces a customer.
- `PATCH /customers/{personalId}` partially updates a customer.
- `DELETE /customers/{personalId}` removes a customer.
- Create requests require `dateOfBirth`, `idLastFour`, `firstName`, `lastName`, and `address`.
- Replace and patch requests require customer name and address fields.

### Services

- `GET /services` lists all services.
- `GET /services/{name}` fetches one service by name.
- `POST /services` creates a service.
- `PUT /services/{name}` fully replaces a service.
- `PATCH /services/{name}` updates a service rate.
- `DELETE /services/{name}` removes a service.
- Create and replace requests require `name` and `sekPerHour`.
- Patch requests require `sekPerHour`.

### Service Offerings

- `POST /service-offerings` creates a service offering and its first version.
- `POST /service-offerings/{name}/versions` creates a new version for an existing offering.
- Requests require `name`, `hourlyChargeAmount`, and `currencyCode` for creation.
- Version requests require `hourlyChargeAmount` and `currencyCode`.

### Session Types

- `POST /session-types` creates a session type and its first version.
- `POST /session-types/{name}/versions` creates a new version for an existing session type.
- Requests require `name`, `durationMinutes`, and `serviceOfferingName` for creation.
- Version requests require `durationMinutes` and `serviceOfferingName`.

### Bookings

- `GET /bookings` lists all bookings.
- `POST /bookings` creates a booking in the `BOOKED` state.
- `PATCH /bookings/{id}/status` updates the booking status.
- Create requests require `customerPersonalId`, `sessionTypeName`, and `bookedTime`.
- Status updates accept `BOOKED`, `CANCELLED`, or `COMPLETED`.
- Marking a booking `COMPLETED` automatically generates an invoice if one does not already exist.

### Invoices

- `GET /invoices` lists all invoices.
- `DELETE /invoices/{id}` deletes an invoice only when the invoice is not unpaid.
- Unpaid invoices are protected from deletion.

## Deployment

- Docker and Docker Compose are required for the standard deployment path.
- Run the stack with `docker compose up --build`.
- The PostgreSQL database container creates the database automatically.
- Hibernate creates or updates the tables automatically when the API starts.
- The API is exposed on `http://localhost:8080`.
- Stop the stack with `docker compose down`.

## Testing

Run the test suite with:

```bash
./mvnw test
```
