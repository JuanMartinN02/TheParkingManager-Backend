# TheParkingManager — Project Context

> Read this first. It is the stable "constitution" of the project.
> For the full reasoning behind every decision, see `../TheParkingManager-TDD.docx`.
> For current progress and the next task, see `status.md`.

## What this is
Multi-tenant SaaS for parking control in residential complexes. Replaces paper
passes with real-time digital plate verification. First client: ESW Towing
(Houston, TX). Built primarily as a **backend-Java learning project**.

## How to work on this project (important)
Juan is transitioning into backend Java and wants to **understand**, not copy-paste.
- Always explain what code does and *why*, before or alongside giving it.
- Do **not** overcomplicate. Simple, understandable designs beat "enterprise"
  patterns that aren't needed. This is not built for millions of users.
- Apply YAGNI: don't build for cases that don't exist yet.
- Keep explanations concise and calibrated to someone without deep dev
  experience; move at a good pace.
- When something would "work by magic," explain the mechanism so it isn't a
  black box.

## Guiding principle
Correctness of plate verification beats every other consideration. A false
"unauthorized" result → wrongful tow → lawsuit. Design and test the
verification path with the most care.

## Stack
- Java 17 (LTS)
- Spring Boot 4.0.6, Maven
- PostgreSQL 17 (Docker container `parking-db`, db `parkingmanager`, user `postgres`)
- Spring Data JPA / Hibernate (`ddl-auto=update`, `show-sql=true` during dev)
- springdoc-openapi (Swagger UI at `/swagger-ui.html`)
- No Spring Security yet (authentication deferred on purpose)

## Locked architecture decisions (do not re-litigate)
- **Layered monolith**: Controller → Service → Repository → DB. Dependencies
  point downward only.
- **Multi-tenancy**: shared DB; every tenant table has `property_id`; every
  tenant query filters by it.
- **UUID** primary keys (`@GeneratedValue`), never sequential ints.
- **Enums stored as STRING** (`@Enumerated(EnumType.STRING)`), never ORDINAL.
- **Limits are editable DATA** (`maxVehicles`, `maxVisitorPasses` on Apartment),
  not fixed schema slots — the headline fix over the legacy app's hardcoded
  4-vehicle / 6-visitor caps.
- **Store a pointer, not the payload**: files (map PDF, tow photos) live in file
  storage; the DB holds only the URL.
- **Reserved spots = individual rows** (`ParkingSpot`). Permit + visitor areas =
  capacity counts on `Property`.
- **Authorization is unit-level**: it belongs to the *apartment*, not the
  vehicle. Any of an apartment's cars may use any of its spots. You pay for a
  spot (the apartment's right), not for a specific car.
- **Config screens = create/edit endpoints** on Property and Apartment; no
  separate settings entity.

## Conventions
- Package: `com.esttowing.parkingmanager`  (⚠ pending cosmetic rename to
  `com.eswtowing.*`)
- Java camelCase fields → Hibernate snake_case columns automatically.
- One entity per file; entity → repository (interface extends `JpaRepository`)
  → service (business logic) → controller (thin).
- Jackson handles JSON ↔ Java. Relations are one-directional to avoid
  serialization loops.
- DTOs at the controller boundary: planned, not yet implemented.

## Out of scope (deferred on purpose)
Billing/payments, parking-map generation (only store the PDF, later), automatic
tow notifications (handled via WhatsApp externally), resident onboarding email
(later), native app + QR scanning (frontend, later), advanced observability,
support for multiple towing companies.
