# Airport Taxi Platform - Ride Offers Backend

A robust, high-performance Spring Boot & MySQL backend for the Airport Taxi Ride Offers platform (Intern Hackathon - Problem 2).

The application bridges travelers arriving at international airports with regional taxi suppliers through an on-demand pricing, car assignment, and double-booking prevention engine.

---

## 1. Quick Start Instructions (1–2 Commands)

### Prerequisites
- **Java 17 or 21+** installed (`java -version`)
- **Maven 3.8+** installed (`mvn -v`)
- **MySQL 8.0+** running on `localhost:3306` with database `airport_taxi` (or update credentials in `application.yml`)

### Run Application in 1 Command
From this directory:

```bash
mvn spring-boot:run
```

Or build and run the packaged JAR:

```bash
mvn clean package -DskipTests && java -jar target/airport-taxi-backend-0.0.1-SNAPSHOT.jar
```

The server starts on port `8080`: `http://localhost:8080/api/v1`

---

## 2. Architecture & Algorithmic Design

### A. $O(1)$ In-Memory Catalog Cache (`@PostConstruct`)
All catalog datasets (`flights.json`, `hotels.json`, `airports.json`, `routes.json`, `taxi_companies.json`) represent read-only reference data. 
- At startup, `CatalogCacheService` loads all catalog files once into unmodifiable `ConcurrentHashMap` structures.
- Lookups for airport city-serving mappings, hotel routes, flight schedules, and supplier service hours execute in strictly **$O(1)$ time complexity**.
- Input validation for Rules **R1**, **R2**, **R3**, and **R4** runs with sub-millisecond latency without issuing database roundtrips.

### B. $O(\log N)$ Double-Booking Prevention Engine (Rules R6 & M6)
To satisfy the strict concurrency requirement that **a car is never double-booked** across overlapping busy windows $[pickup, pickup + 2 \times drive\_minutes)$:
- The `bookings` table defines a **Composite B-Tree Index** on `(car_id, busy_start_time, busy_end_time)`.
- Pre-event rides from `booked_rides.json` are seeded directly into `bookings` on startup (`is_pre_booked = true`), unifying past bookings and active platform bookings under a single database index.
- Overlap evaluation uses the interval condition:
  $$\text{start}_A < \text{end}_B \land \text{end}_A > \text{start}_B$$
- Executed via JPQL in `BookingRepository`:
  ```sql
  SELECT COUNT(b) FROM Booking b
  WHERE b.carId = :carId
    AND b.status = 'CONFIRMED'
    AND b.busyStartTime < :newEndTime
    AND b.busyEndTime > :newStartTime
  ```
- Because the query operates directly over the composite index, MySQL traverses `car_id` and performs a tight range scan over the datetime boundaries in **$O(\log N)$ time complexity**, eliminating full-table scans.

### C. Self-Booking Exclusion for Change Requests (Rules R9 & M7)
When a traveler submits a flight or pickup change request and the supplier accepts it, the system validates that the assigned car is free in the new time window while **explicitly excluding the current booking ID**:
```sql
SELECT COUNT(b) FROM Booking b
WHERE b.carId = :carId
  AND b.id <> :excludeBookingId
  AND b.status = 'CONFIRMED'
  AND b.busyStartTime < :newEndTime
  AND b.busyEndTime > :newStartTime
```
This guarantees the car is not flagged as busy by its own existing booking.

---

## 3. Completed Features Checklist (M1–M7)

| Feature | Description | Status |
| :--- | :--- | :---: |
| **M1: Ride Request** | Traveler posts flight, hotel, adults, child ages, bags, pickup, price. Form validates R1 (catalog & airport serves city), R2 (pickup $\ge$ landing + 60m), and R3 (route price range). | **DONE** |
| **M2: Supplier Dashboard** | Suppliers view only requests they can serve (R4: airport, area, service hours, car capacity R5, car free R6). Clear error reason returned if unserviceable. | **DONE** |
| **M3: Assign a Car** | Supplier assigns car with price. System validates vehicle ownership, R4, R5, R6, and R7 price bounds; reports exact rule broken. | **DONE** |
| **M4: Book or Reject** | Traveler views all assigned taxis with supplier name, car specs, and price. Can reject individual offers or book one. | **DONE** |
| **M5: Booking Summary** | 10-digit unique booking number opens complete ride summary. State persists across backend restarts. Non-existent booking returns 404. | **DONE** |
| **M6: No Double Booking** | `@Transactional` booking confirmation immediately checks car availability via $O(\log N)$ index query. Concurrent overlaps are blocked. | **DONE** |
| **M7: Flight/Pickup Change** | Traveler submits change request (new flight or pickup). Supplier accepts or rejects. Accepted changes update booking window; rejected changes keep original booking intact. | **DONE** |

---

## 4. API Endpoints Reference

### Traveler Flow
- `POST /api/v1/traveler/requests`: Create ride request (Validates R1, R2, R3)
- `GET /api/v1/traveler/requests/{requestId}`: View ride request
- `GET /api/v1/traveler/requests/{requestId}/offers`: View all supplier offers for request
- `POST /api/v1/traveler/offers/{offerId}/book`: Confirm booking (M4, M6)
- `POST /api/v1/traveler/offers/{offerId}/reject`: Reject taxi offer (R8)

### Supplier Flow
- `GET /api/v1/suppliers/{companyId}/open-requests`: Eligible open requests dashboard (M2, R4)
- `POST /api/v1/suppliers/offers`: Submit taxi offer (M3, R5, R7)
- `GET /api/v1/suppliers/{companyId}/offers`: View supplier's submitted offers
- `GET /api/v1/suppliers/{companyId}/change-requests`: View pending change requests

### Booking & Change Request Flow
- `GET /api/v1/bookings/by-number/{bookingNumber}`: 10-digit booking summary (M5)
- `GET /api/v1/bookings/{bookingId}`: Booking lookup by ID
- `POST /api/v1/bookings/{bookingId}/change-requests`: Traveler submits change request (M7)
- `PATCH /api/v1/bookings/change-requests/{changeRequestId}`: Supplier accepts/rejects change request (M7)
- `GET /api/v1/bookings/{bookingId}/change-requests`: View change requests for a booking

### Catalog Reference Flow
- `GET /api/v1/catalog/routes/lookup?airport=NRT&city=Tokyo&area=Ueno`: Suggested price range & drive time
- `GET /api/v1/catalog/flights`: All flights
- `GET /api/v1/catalog/hotels`: All hotels
- `GET /api/v1/catalog/airports`: All airports
- `GET /api/v1/catalog/companies`: All taxi companies

---

## 5. Live Demo Execution

An automated, sequential REST Client test file is available at:
`api-demo.http`

It reproduces the entire Menon family (Scenario 1) and Sakura Cabs (Scenario 2) lifecycle:
1. Posting valid Menon request
2. Inspecting Sakura Cabs open requests dashboard
3. Attempting invalid car offer (breaks R5) $\to$ `400 Bad Request`
4. Attempting invalid price offer (breaks R7) $\to$ `400 Bad Request`
5. Posting valid Sakura Cabs offer & competing Edo Premium offer
6. Rejecting competitor offer
7. Confirming booking & generating 10-digit booking number
8. Querying booking summary
9. Submitting pickup change request
10. Supplier accepting change request & verifying updated busy window

---

## 6. Known Limitations
1. **Catalog Mutability**: Catalog datasets are strictly read-only per hackathon guidelines. New airlines or hotel destinations cannot be dynamically added at runtime without updating catalog source files.
2. **Sequential Concurrency**: Per Rule M6 specifications, sequential transactional validation ensures no car is double-booked. Pessimistic row-level locking (`SELECT ... FOR UPDATE`) is omitted as simultaneous multi-user racing was designated out of scope for the demo environment.
3. **Currency Conversion**: All prices are natively modeled in Indian Rupees (INR) without multi-currency FX conversion, adhering to hackathon data conventions.

