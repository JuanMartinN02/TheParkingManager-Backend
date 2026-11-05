# TheParkingManager — Backend

Multi-tenant REST API for parking control in residential complexes. It replaces
paper parking passes with real-time, digital license-plate verification: a tow
operator enters a plate and immediately sees whether that car is allowed to be
parked where it is.

I'm building it as a real production app for a US towing company, and as my main
project for moving into backend Java.

## The problem it solves

Residential complexes limit parking, and a towing company enforces it. Today that
runs on printed passes and manual checks. This API makes the check instant and
reliable. The verification is the part that has to be right: if it tells the
operator a car is unauthorized when it actually isn't, that's a wrongful tow and a
real legal problem. So correctness of the lookup takes priority over everything
else in the design.

## Tech stack

- Java 17
- Spring Boot 4 (Spring MVC, Spring Data JPA / Hibernate)
- PostgreSQL 17
- Spring Security (session-based auth + role-based access control)
- Maven
- JUnit 5 + Mockito
- springdoc-openapi (Swagger UI)

## Architecture

- Layered monolith: Controller → Service → Repository. Controllers only translate
  HTTP; all business rules live in services, so a rule holds for every caller.
- Multi-tenant on a shared database: every tenant table carries a `property_id`
  and every query is scoped to it, so one property can never read another's data.
- Entities have no `setId()` — ids are assigned by the database only, which blocks
  a client from sending an `id` in a POST and overwriting an existing row.

## Design decisions I think are worth calling out

- UUID primary keys instead of sequential ids, so records can't be guessed or
  enumerated across tenants.
- Enums stored as strings, never ordinals, so reordering an enum value can't
  silently corrupt existing rows.
- Plate normalization lives in one class, used by both the save path and the
  lookup path. If those two ever normalized differently, the mismatch would be a
  wrongful tow, so they share a single source.
- "Today" comes from one app-wide Clock in Houston time, not the server's UTC
  clock. A UTC server rolls over to tomorrow at 7pm local, which would read a
  permit that expires today as already expired — a false "unauthorized".
- Authorization is unit-level: it belongs to the apartment, not a specific car, so
  any of the apartment's cars can use its spots.
- Tow records store the plate and the verdict as snapshots, not foreign keys, so
  deleting a car never erases its tow history.

## What's built

- Properties, apartments, vehicles, parking spots, and parking authorizations
  (permit + reserved), each with its business rules.
- The plate-verification lookup and a search-by-spot ("who owns spot 480?"),
  covered by automated tests.
- Per-apartment limits (max vehicles) that an admin can change — the legacy system
  hardcoded these.
- Users and role-based access control (in progress): SUPER_ADMIN, PROPERTY_ADMIN,
  RESIDENT, TOWING_OPERATOR.

## Roadmap

- Finish Spring Security + RBAC
- Move-in / move-out actions
- Visitor passes
- Tow records with photo evidence (file storage)
- Input validation, i18n (English / Spanish), audit log
- React + MUI frontend
- Production hardening: Flyway migrations, hosted Postgres, deployment

## Running locally

Requirements: Java 17, Maven, Docker.

1. Start PostgreSQL in Docker:

   ```
   docker run --name parking-db -e POSTGRES_PASSWORD=yourpassword \
     -e POSTGRES_DB=parkingmanager -p 5432:5432 -d postgres:17
   ```

2. Point the app at it and set the first admin. The first SUPER_ADMIN is created
   at startup from environment variables (never committed):

   ```
   FIRST_ADMIN_EMAIL=you@example.com
   FIRST_ADMIN_PASSWORD=change-me-at-least-12-chars
   ```

   Adjust the datasource URL, user and password in
   `src/main/resources/application.properties` to match your container (change the
   port if 5432 is already taken).

3. Run it:

   ```
   mvn spring-boot:run
   ```

4. Open the API docs at `http://localhost:8080/swagger-ui.html`.

## Tests

```
mvn test
```

The verification logic is covered with Mockito and a frozen clock, since that's
the code a bug would hurt most.
