# Airport Taxi Platform - API Contract Specification

This document provides the complete API specification for the **Airport Taxi Ride Offers Backend**. 

- **Base URL**: `http://localhost:8080/api/v1`
- **Content Type**: `application/json`
- **Time Format**: ISO-8601 Local Date Time (`yyyy-MM-dd'T'HH:mm:ss` or `yyyy-MM-dd'T'HH:mm`)
- **Currency**: Whole INR (`₹`)

---

## Standard Error Response Format

All error responses produced by the `GlobalExceptionHandler` conform to the following JSON structure:

```json
{
  "timestamp": "2027-04-10T10:30:00",
  "rule": "R2",
  "message": "[R2 Violation] Pickup time (2027-04-10T11:00) must be at least 60 minutes after landing time (2027-04-10T10:30). Earliest pickup is 2027-04-10T11:30"
}
```

| HTTP Status | Trigger Condition |
| :--- | :--- |
| `400 Bad Request` | Business rule violations (`R1`, `R2`, `R3`, `R4`, `R5`, `R6`, `R7`, `R8`, `R9`, `M6`) or input validation errors (`INPUT_VALIDATION`). |
| `404 Not Found` | Entity not found in database or catalog (`NOT_FOUND`). |
| `500 Internal Server Error` | Unexpected runtime failures (`INTERNAL_ERROR`). |

---

## 1. Catalog Endpoints (`/catalog`)

Read-only reference endpoints populated from the static catalog cache in $O(1)$ time.

---

### 1.1 Route Suggested Price Lookup
- **Route**: `GET /api/v1/catalog/routes/lookup`
- **Purpose**: Retrieves road distance, drive time, and route price range for traveler form validation (Rule R3).
- **Query Parameters**:
  - `airport` (String, e.g., `NRT`)
  - `city` (String, e.g., `Tokyo`)
  - `area` (String, e.g., `Ueno`)
- **Request Body**: *None*
- **Response Payload**: `200 OK`
```json
{
  "airport": "NRT",
  "city": "Tokyo",
  "area": "Ueno",
  "distance_km": 73.3,
  "drive_minutes": 90,
  "price_min_inr": 10200,
  "price_max_inr": 16800
}
```
- **Error Responses**:
  - `404 Not Found`: Route between given airport and city/area not defined.

---

### 1.2 Get All Flights
- **Route**: `GET /api/v1/catalog/flights`
- **Purpose**: Returns all flights in the catalog (flights landing in Japan/UAE: `NRT`, `HND`, `KIX`, `DXB`, `AUH`).
- **Request Body**: *None*
- **Response Payload**: `200 OK`
```json
[
  {
    "flight_id": "FL-401",
    "airline": "AirVeda",
    "flight_number": "AV 520",
    "from": "BLR",
    "to": "NRT",
    "departure_local": "2027-04-09T23:50:00",
    "arrival_local": "2027-04-10T10:30:00",
    "stops": 0,
    "duration_minutes": 430,
    "cabin": "ECONOMY"
  }
]
```

---

### 1.3 Get All Hotels
- **Route**: `GET /api/v1/catalog/hotels`
- **Purpose**: Returns all destination hotels and their city/area mappings.
- **Request Body**: *None*
- **Response Payload**: `200 OK`
```json
[
  {
    "hotel_id": "HTL-TYO-004",
    "name": "Ueno Garden Inn",
    "city": "Tokyo",
    "area": "Ueno",
    "lat": 35.712,
    "lng": 139.7728,
    "rating": 7.9,
    "stars": 3,
    "check_in_from": "15:00",
    "check_out_until": "11:00",
    "family_friendly": true
  }
]
```

---

### 1.4 Get All Airports
- **Route**: `GET /api/v1/catalog/airports`
- **Purpose**: Returns airports and the cities they serve (Rule R1 check).
- **Request Body**: *None*
- **Response Payload**: `200 OK`
```json
[
  {
    "code": "NRT",
    "name": "Narita International Airport",
    "city": "Tokyo",
    "country": "JP",
    "timezone": "Asia/Tokyo",
    "lat": 35.772,
    "lng": 140.3929,
    "serves_cities": [
      "Tokyo"
    ]
  }
]
```

---

### 1.5 Get All Taxi Companies
- **Route**: `GET /api/v1/catalog/companies`
- **Purpose**: Returns taxi suppliers, operational airports/areas, working hours, and car fleets.
- **Request Body**: *None*
- **Response Payload**: `200 OK`
```json
[
  {
    "company_id": "TX-JP-01",
    "name": "Sakura Cabs",
    "airports": ["NRT", "HND"],
    "areas": [
      { "city": "Tokyo", "area": "Ueno" }
    ],
    "service_from": "06:00",
    "service_until": "23:00",
    "rating": 4.4,
    "vehicles": [
      {
        "vehicle_id": "SC-1",
        "type": "SEDAN",
        "seats": 4,
        "large_bags": 2,
        "small_bags": 2
      },
      {
        "vehicle_id": "SC-3",
        "type": "SUV",
        "seats": 6,
        "large_bags": 4,
        "small_bags": 3
      }
    ]
  }
]
```

---

## 2. Traveler Endpoints (`/traveler`)

---

### 2.1 Post Ride Request
- **Route**: `POST /api/v1/traveler/requests`
- **Purpose**: Creates a new ride request. Validates Rule **R1** (passengers, flight/hotel catalog check, airport serving city), Rule **R2** (pickup $\ge$ landing + 60m), and Rule **R3** (price within route bounds).
- **Request Payload**:
```json
{
  "traveler_name": "Menon family",
  "flight_id": "FL-401",
  "hotel_id": "HTL-TYO-004",
  "adults": 2,
  "child_ages": [6],
  "large_bags": 3,
  "small_bags": 2,
  "pickup": "2027-04-10T11:30:00",
  "car_type": null,
  "price_inr": 12000
}
```
*Note: `pickup` is optional. If omitted, it automatically defaults to landing time + 60 minutes. `car_type` can be `HATCHBACK`, `SEDAN`, `SUV`, `BUS`, or `null` for ANY.*

- **Response Payload**: `201 Created`
```json
{
  "request_id": "REQ-7F8A1B2C",
  "traveler_name": "Menon family",
  "flight_id": "FL-401",
  "hotel_id": "HTL-TYO-004",
  "adults": 2,
  "child_ages": [6],
  "large_bags": 3,
  "small_bags": 2,
  "pickup_time": "2027-04-10T11:30:00",
  "car_type": null,
  "price_inr": 12000,
  "status": "OPEN",
  "created_at": "2027-04-10T10:35:00"
}
```
- **Error Responses**:
  - `400 Bad Request` with `rule: "R1"`: Arrival airport does not serve hotel city, or adults $< 1$, or child age $> 17$.
  - `400 Bad Request` with `rule: "R2"`: Pickup time earlier than landing + 60 minutes.
  - `400 Bad Request` with `rule: "R3"`: Offered price outside route price range.
  - `404 Not Found` with `rule: "R1"`: Flight ID or Hotel ID not found in catalog.

---

### 2.2 Get Ride Request Details
- **Route**: `GET /api/v1/traveler/requests/{requestId}`
- **Purpose**: Retrieves a submitted ride request by ID.
- **Request Body**: *None*
- **Response Payload**: `200 OK`
```json
{
  "request_id": "REQ-7F8A1B2C",
  "traveler_name": "Menon family",
  "flight_id": "FL-401",
  "hotel_id": "HTL-TYO-004",
  "adults": 2,
  "child_ages": [6],
  "large_bags": 3,
  "small_bags": 2,
  "pickup_time": "2027-04-10T11:30:00",
  "car_type": null,
  "price_inr": 12000,
  "status": "OPEN",
  "created_at": "2027-04-10T10:35:00"
}
```
- **Error Responses**:
  - `404 Not Found`: Request ID not found.

---

### 2.3 Get Assigned Offers for Request
- **Route**: `GET /api/v1/traveler/requests/{requestId}/offers`
- **Purpose**: Feature **M4**: Displays all taxi offers submitted by suppliers for this request.
- **Request Body**: *None*
- **Response Payload**: `200 OK`
```json
[
  {
    "id": 1,
    "request_id": "REQ-7F8A1B2C",
    "company_id": "TX-JP-01",
    "company_name": "Sakura Cabs",
    "vehicle_id": "SC-3",
    "vehicle_type": "SUV",
    "seats": 6,
    "large_bags": 4,
    "small_bags": 3,
    "offered_price_inr": 12500,
    "status": "OPEN",
    "created_at": "2027-04-10T10:40:00"
  },
  {
    "id": 2,
    "request_id": "REQ-7F8A1B2C",
    "company_id": "TX-JP-03",
    "company_name": "Edo Premium Cars",
    "vehicle_id": "EP-3",
    "vehicle_type": "SUV",
    "seats": 7,
    "large_bags": 5,
    "small_bags": 4,
    "offered_price_inr": 14000,
    "status": "OPEN",
    "created_at": "2027-04-10T10:42:00"
  }
]
```

---

### 2.4 Book an Assigned Taxi Offer
- **Route**: `POST /api/v1/traveler/offers/{offerId}/book`
- **Purpose**: Features **M4**, **M6** & Rule **R8**: Confirms booking. Atomically calculates busy window $[pickup, pickup + 2 \times drive\_minutes)$ and executes $O(\log N)$ overlap check. Returns unique 10-digit booking number. Marks other offers for the request as `NOT_CHOSEN`.
- **Request Body**: *None*
- **Response Payload**: `200 OK`
```json
{
  "id": 10,
  "booking_number": "3829104756",
  "request_id": "REQ-7F8A1B2C",
  "company_id": "TX-JP-01",
  "company_name": "Sakura Cabs",
  "car_id": "SC-3",
  "flight_id": "FL-401",
  "hotel_id": "HTL-TYO-004",
  "pickup_time": "2027-04-10T11:30:00",
  "busy_start_time": "2027-04-10T11:30:00",
  "busy_end_time": "2027-04-10T14:30:00",
  "price_inr": 12500,
  "status": "CONFIRMED",
  "is_pre_booked": false,
  "created_at": "2027-04-10T10:45:00"
}
```
- **Error Responses**:
  - `400 Bad Request` with `rule: "M6"`: Double booking prevented (car became busy during the window).
  - `400 Bad Request` with `rule: "R8"`: Offer is not in `OPEN` status or request already fulfilled.
  - `404 Not Found`: Offer ID not found.

---

### 2.5 Reject an Assigned Taxi Offer
- **Route**: `POST /api/v1/traveler/offers/{offerId}/reject`
- **Purpose**: Rule **R8**: Traveler rejects a specific offer so it can no longer be booked.
- **Request Body**: *None*
- **Response Payload**: `200 OK`
```json
{
  "id": 2,
  "request_id": "REQ-7F8A1B2C",
  "company_id": "TX-JP-03",
  "company_name": "Edo Premium Cars",
  "vehicle_id": "EP-3",
  "vehicle_type": "SUV",
  "seats": 7,
  "large_bags": 5,
  "small_bags": 4,
  "offered_price_inr": 14000,
  "status": "REJECTED",
  "created_at": "2027-04-10T10:42:00"
}
```
- **Error Responses**:
  - `400 Bad Request` with `rule: "R8"`: Offer is not in `OPEN` status.

---

## 3. Supplier Endpoints (`/suppliers`)

---

### 3.1 Get Eligible Open Requests Dashboard
- **Route**: `GET /api/v1/suppliers/{companyId}/open-requests`
- **Purpose**: Feature **M2** & Rule **R4**: Returns only the open requests the specified supplier can serve. Evaluates airport service, city/area service, operating hours (including midnight crossing), passenger/bag fit (R5), and availability during the busy window (R6). Lists eligible vehicles from the fleet.
- **Request Body**: *None*
- **Response Payload**: `200 OK`
```json
[
  {
    "request": {
      "request_id": "REQ-7F8A1B2C",
      "traveler_name": "Menon family",
      "flight_id": "FL-401",
      "hotel_id": "HTL-TYO-004",
      "adults": 2,
      "child_ages": [6],
      "large_bags": 3,
      "small_bags": 2,
      "pickup_time": "2027-04-10T11:30:00",
      "car_type": null,
      "price_inr": 12000,
      "status": "OPEN",
      "created_at": "2027-04-10T10:35:00"
    },
    "eligible_vehicles": [
      {
        "vehicle_id": "SC-3",
        "type": "SUV",
        "seats": 6,
        "large_bags": 4,
        "small_bags": 3
      }
    ],
    "price_min_inr": 10200,
    "price_max_inr": 16800,
    "drive_minutes": 90
  }
]
```
- **Error Responses**:
  - `404 Not Found` with `rule: "R4"`: Supplier company ID not found.

---

### 3.2 Assign Car and Submit Offer
- **Route**: `POST /api/v1/suppliers/offers`
- **Purpose**: Feature **M3** & Rules **R4**, **R5**, **R6**, **R7**: Supplier assigns a vehicle and a price. Validates vehicle ownership, operational hours, car size, busy time overlap, and route price range.
- **Request Payload**:
```json
{
  "request_id": "REQ-7F8A1B2C",
  "company_id": "TX-JP-01",
  "vehicle_id": "SC-3",
  "offered_price_inr": 12500
}
```
- **Response Payload**: `201 Created`
```json
{
  "id": 1,
  "request_id": "REQ-7F8A1B2C",
  "company_id": "TX-JP-01",
  "company_name": "Sakura Cabs",
  "vehicle_id": "SC-3",
  "vehicle_type": "SUV",
  "seats": 6,
  "large_bags": 4,
  "small_bags": 3,
  "offered_price_inr": 12500,
  "status": "OPEN",
  "created_at": "2027-04-10T10:40:00"
}
```
- **Error Responses**:
  - `400 Bad Request` with `rule: "R4"`: Supplier does not serve airport/area, or is closed at pickup time.
  - `400 Bad Request` with `rule: "R5"`: Vehicle does not have enough seats, large bags, or small bags.
  - `400 Bad Request` with `rule: "R6"`: Vehicle is already busy during the calculated window.
  - `400 Bad Request` with `rule: "R7"`: Offered price outside route price bounds, or vehicle does not belong to supplier.

---

### 3.3 Get Supplier's Submitted Offers
- **Route**: `GET /api/v1/suppliers/{companyId}/offers`
- **Purpose**: Displays all offers submitted by this taxi supplier.
- **Request Body**: *None*
- **Response Payload**: `200 OK`
```json
[
  {
    "id": 1,
    "request_id": "REQ-7F8A1B2C",
    "company_id": "TX-JP-01",
    "company_name": "Sakura Cabs",
    "vehicle_id": "SC-3",
    "vehicle_type": "SUV",
    "seats": 6,
    "large_bags": 4,
    "small_bags": 3,
    "offered_price_inr": 12500,
    "status": "OPEN",
    "created_at": "2027-04-10T10:40:00"
  }
]
```

---

### 3.4 Get Pending Change Requests for Supplier
- **Route**: `GET /api/v1/suppliers/{companyId}/change-requests`
- **Purpose**: Feature **M7**: Lists change requests waiting for this supplier's decision.
- **Request Body**: *None*
- **Response Payload**: `200 OK`
```json
[
  {
    "id": 5,
    "booking_id": 10,
    "booking_number": "3829104756",
    "new_flight_id": null,
    "new_pickup_time": "2027-04-10T12:00:00",
    "status": "PENDING",
    "rejection_reason": null,
    "created_at": "2027-04-10T10:50:00",
    "resolved_at": null
  }
]
```

---

## 4. Booking & Change Request Endpoints (`/bookings`)

---

### 4.1 Get Booking Summary by 10-Digit Booking Number
- **Route**: `GET /api/v1/bookings/by-number/{bookingNumber}`
- **Purpose**: Feature **M5**: Primary booking summary lookup using the 10-digit number. Survives app restarts.
- **Request Body**: *None*
- **Response Payload**: `200 OK`
```json
{
  "id": 10,
  "booking_number": "3829104756",
  "request_id": "REQ-7F8A1B2C",
  "company_id": "TX-JP-01",
  "company_name": "Sakura Cabs",
  "car_id": "SC-3",
  "flight_id": "FL-401",
  "hotel_id": "HTL-TYO-004",
  "pickup_time": "2027-04-10T11:30:00",
  "busy_start_time": "2027-04-10T11:30:00",
  "busy_end_time": "2027-04-10T14:30:00",
  "price_inr": 12500,
  "status": "CONFIRMED",
  "is_pre_booked": false,
  "created_at": "2027-04-10T10:45:00"
}
```
- **Error Responses**:
  - `404 Not Found`: Booking number not found.

---

### 4.2 Get Booking by Internal ID
- **Route**: `GET /api/v1/bookings/{bookingId}`
- **Purpose**: Retrieves a booking by database primary key.
- **Request Body**: *None*
- **Response Payload**: `200 OK` (Same schema as 4.1)

---

### 4.3 Traveler Submits Change Request
- **Route**: `POST /api/v1/bookings/{bookingId}/change-requests`
- **Purpose**: Feature **M7** & Rule **R9**: Traveler requests a change to flight or pickup time. Validates Rule R2 ($\ge landing + 60m$).
- **Request Payload**:
```json
{
  "new_flight_id": null,
  "new_pickup_time": "2027-04-10T12:00:00"
}
```
*Note: Both fields are optional, but at least one must be provided. If `new_flight_id` is provided without `new_pickup_time`, pickup defaults to new landing + 60 minutes.*

- **Response Payload**: `201 Created`
```json
{
  "id": 5,
  "booking_id": 10,
  "booking_number": "3829104756",
  "new_flight_id": null,
  "new_pickup_time": "2027-04-10T12:00:00",
  "status": "PENDING",
  "rejection_reason": null,
  "created_at": "2027-04-10T10:50:00",
  "resolved_at": null
}
```
- **Error Responses**:
  - `400 Bad Request` with `rule: "R2"`: New pickup time is earlier than landing + 60 minutes.
  - `400 Bad Request` with `rule: "R9"`: Booking is not `CONFIRMED`, or neither flight nor pickup time provided.
  - `404 Not Found`: Booking ID or flight ID not found.

---

### 4.4 Supplier Resolves Change Request (Accept or Reject)
- **Route**: `PATCH /api/v1/bookings/change-requests/{changeRequestId}`
- **Purpose**: Feature **M7** & Rule **R9**: Supplier decides whether to accept or reject the change request.
  - **Accept**: Validates car availability in the new window using overlap query **excluding current bookingId**. Recalculates `busyStartTime` and `busyEndTime` and updates booking.
  - **Reject**: Marks change request as `REJECTED`. Preserves original booking unchanged.
- **Request Payload**:
```json
{
  "status": "ACCEPTED",
  "rejection_reason": null
}
```
*Or for rejection:*
```json
{
  "status": "REJECTED",
  "rejection_reason": "Car SC-3 is already scheduled for maintenance at 12:00"
}
```

- **Response Payload**: `200 OK`
```json
{
  "id": 5,
  "booking_id": 10,
  "booking_number": "3829104756",
  "new_flight_id": null,
  "new_pickup_time": "2027-04-10T12:00:00",
  "status": "ACCEPTED",
  "rejection_reason": null,
  "created_at": "2027-04-10T10:50:00",
  "resolved_at": "2027-04-10T10:52:00"
}
```
- **Error Responses**:
  - `400 Bad Request` with `rule: "R9"`: Car is busy with another booking during the new time window, or change request already resolved.
  - `404 Not Found`: Change request ID not found.

---

### 4.5 Get All Change Requests for a Booking
- **Route**: `GET /api/v1/bookings/{bookingId}/change-requests`
- **Purpose**: Retrieves all change requests submitted against a booking.
- **Request Body**: *None*
- **Response Payload**: `200 OK`
```json
[
  {
    "id": 5,
    "booking_id": 10,
    "booking_number": "3829104756",
    "new_flight_id": null,
    "new_pickup_time": "2027-04-10T12:00:00",
    "status": "ACCEPTED",
    "rejection_reason": null,
    "created_at": "2027-04-10T10:50:00",
    "resolved_at": "2027-04-10T10:52:00"
  }
]
```

