# Status — TheParkingManager

_Last updated: 2026-10-04_

## Where we are
The core CRUD scaffold is built and running. We just finished reviewing the
legacy app and locking the resulting data-model changes. Next is applying those
changes, then rewriting the authorization + lookup (the correctness core).

## Entities currently built (in code)
- `Property`: id, name, parkingMode (⚠ being removed), visitorSpotCapacity
- `Apartment`: id, unitNumber, property (FK)
- `Vehicle`: id, plate, makeModel, color, apartment (FK)
- `ParkingSpot`: id, label, property (FK)
- `SpotAssignment`: id, apartment (FK), spot (FK), validUntil (⚠ being reshaped)
- Enums: `ParkingMode` (⚠ being deleted), `PlateStatus`
- Service: `PlateLookupService` (basic — to be rewritten)
- Controllers: Property, Apartment, Vehicle, ParkingSpot, SpotAssignment,
  PlateLookup, Ping

## Endpoints built
- `GET /ping`
- `GET,POST /properties`
- `GET,POST /properties/{propertyId}/apartments`
- `GET,POST /apartments/{apartmentId}/vehicles`
- `GET,POST /properties/{propertyId}/spots`
- `POST /apartments/{apartmentId}/assignments`
- `GET /properties/{propertyId}/lookup?plate=`

## Decided but NOT yet coded (the next edits)
1. **Property**: remove `parkingMode` (and delete the `ParkingMode` enum); add
   `permitSpotCapacity` (int), `mapPdfUrl` (String, used later), plus
   property-wide settings for the config screen (e.g. `visitorPassDurationHours`,
   permit-expiry policy).
2. **Apartment**: add `maxVehicles` (int), `maxVisitorPasses` (int), `status`
   (enum `ApartmentStatus` = ACTIVE / MOVED_OUT). Add the `ApartmentStatus` enum.
3. **Rename `SpotAssignment` → `ParkingAuthorization`**: apartment (FK), spot
   (FK, **nullable** — null = permit, set = reserved), validUntil (**nullable**
   — null = never expires). This unifies permit + reserved into ONE mechanism.
4. **Vehicle**: stays as-is (no parkingType — the type lives on the
   authorization, because authorization is unit-level).
5. **Rewrite `PlateLookupService`**: car → apartment → any active authorization?
   Needs a `@Query` for "validUntil IS NULL OR validUntil >= today". Statuses:
   `RESIDENT_AUTHORIZED` (with spot labels), `RESIDENT_REGISTERED` (known but not
   authorized — the dangerous case), `UNKNOWN`. (`VISITOR_PASS_ACTIVE` arrives
   with visitor passes.)
6. Make Property/Apartment settings **editable** (PUT/PATCH) for the config screen.

## Next step
Apply edits 1–2 (pure additions, low risk), confirm the app starts clean, then
do 3 + 5 (the authorization rename + lookup rewrite — the heart of the system).

## Backlog (later, rough order)
Visitor passes (with limit enforcement) → tow records + photos (file storage) →
DTOs → input validation (plate, visitor id) → Spring Security + RBAC → audit log
→ i18n (English default/fallback) → map PDF upload → onboarding email →
QR scanning.

## Known legacy bugs we are fixing
- Hardcoded 4-vehicle / 6-visitor caps.
- Reserved spots double-bookable (occupied numbers not removed from available).
- No expiry alerts on reserved/permit.
- Visitor id unvalidated ("can be anything").
