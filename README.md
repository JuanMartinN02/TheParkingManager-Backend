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
- Spring Security (session-based auth, BCrypt, role-based access control)
- Bean Validation (Jakarta Validation)
- PostgreSQL 17
- Maven
- JUnit 5 + Mockito + AssertJ
- springdoc-openapi (Swagger UI)

## Architecture

- Layered monolith: Controller → Service → Repository. Controllers only translate
  HTTP and map DTOs; all business rules live in services, so a rule holds for
  every caller (an admin, a resident, whoever).
- Multi-tenant on a shared database: every tenant table carries a `property_id`
  and every query is scoped to it, so one property can never read another's data.
- Request/response DTOs at the controller boundary: clients only send the fields
  they're allowed to set, and responses expose related entities as ids, never as
  nested object graphs.
- One consistent error format across the whole API (RFC 9457 ProblemDetail).

## Security

- Session-based login (not JWT): after login the server holds the session and the
  browser gets an `HttpOnly` cookie it can't read in JavaScript, so logout and
  revocation are immediate. CSRF protection is on, as cookie sessions require.
- Passwords are stored as BCrypt hashes, never in plain text. Accounts are created
  with a temporary password that the user is forced to change at first login.
- Role-based access control with four roles (SUPER_ADMIN, PROPERTY_ADMIN, RESIDENT,
  TOWING_OPERATOR). Every tenant endpoint runs an explicit access check first: can
  this user act on this property / apartment? If not, 403. This is what blocks
  IDOR — a resident reaching another apartment's data, or an admin crossing into
  another property.
- The access check reloads the user from the database on each request, so
  deactivating a user or changing their role takes effect immediately, not on their
  next login.
- Users are deactivated, never deleted, so historical references (like which
  operator recorded a tow) stay intact.

## Design decisions I think are worth calling out

- UUID primary keys instead of sequential ids, so records can't be guessed or
  enumerated across tenants. Entities also have no `setId()` — ids are assigned by
  the database only, which blocks a client from sending an `id` in a POST and
  overwriting an existing row.
- Enums stored as strings, never ordinals, so reordering an enum value can't
  silently corrupt existing rows.
- Plate normalization lives in one class, used by both the save path and the
  lookup path. If those two ever normalized differently, the mismatch would be a
  wrongful tow, so they share a single source of truth.
- "Today" comes from one app-wide Clock in the property's timezone (Houston), not
  the server's UTC clock. A UTC server rolls over to tomorrow at 7pm local, which
  would read a permit that expires today as already expired — a false
  "unauthorized". Services take "today" as a parameter and never call
  `LocalDate.now()` on their own, which also makes the date logic easy to test
  with a frozen clock.
- The role-to-access mapping is a `switch` expression with no default, so adding a
  new role won't compile until I've decided what it can access — the compiler
  refuses to let a role slip through unconfigured.
- Authorization is unit-level: it belongs to the apartment, not a specific car, so
  any of the apartment's cars can use its spots.
- Tow records store the plate and the verdict as snapshots, not foreign keys, so
  deleting a car (for example on move-out) never erases its tow history.
- Errors are returned as RFC 9457 ProblemDetails from a single
  `@RestControllerAdvice`. Validation failures include a field→message map;
  anything unexpected is logged server-side and never leaked to the client.

## What's built

- Properties, apartments, vehicles, parking spots, and parking authorizations
  (permit + reserved), each with its business rules.
- The plate-verification lookup and a search-by-spot ("who owns spot 480?"),
  covered by automated tests.
- Per-apartment limits (max vehicles) that an admin can change — the legacy system
  hardcoded them, which was its main complaint.
- Bulk spot creation (up to 1,000 labels, all-or-nothing in one transaction).
- Users + role-based access control, BCrypt, forced password change, per-request
  authorization checks.
- Apartment lifecycle: move-out (one transaction: deletes the apartment's
  authorizations and vehicles, deactivates its residents, frees its spots) and
  move-in.
- Input validation at the HTTP boundary + a consistent ProblemDetail error format.
- Around 60 automated tests (unit + service level) run without a database, with the
  verification and authorization paths covered most heavily, since those are the
  code a bug would hurt most.

## Roadmap

- Visitor passes
- Tow records with photo evidence (file storage)
- Audit log for admin changes
- i18n (English / Spanish), map PDF upload, resident onboarding email
- React + MUI frontend
- Production hardening: Flyway migrations, hosted Postgres, HTTPS-only cookies,
  deployment, and integration tests (MockMvc) against a real database

## Running locally

Requirements: Java 17, Maven, Docker.

1. Start PostgreSQL in Docker:

   ```
   docker run --name parking-db -e POSTGRES_PASSWORD=yourpassword \
     -e POSTGRES_DB=parkingmanager -p 5432:5432 -d postgres:17
   ```

2. Point the app at it and set the first admin. The first SUPER_ADMIN is created
   at startup from environment variables (never committed), and the app refuses to
   start if the password is under 12 characters:

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

Most tests are plain unit/service tests (Mockito + a frozen clock, no Docker
needed), so the suite runs fast. The verification logic and the access-control
rules have the heaviest coverage, since that's where a bug does the most damage.
