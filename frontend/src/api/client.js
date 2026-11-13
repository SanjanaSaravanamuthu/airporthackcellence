import {
  MOCK_FLIGHTS, MOCK_HOTELS, MOCK_ROUTES,
  MOCK_COMPANIES, MOCK_OPEN_REQUESTS,
} from './mockData';

const BASE_URL = '/api/v1'; // proxied by Vite in dev; same-origin in prod

// ─── Mock stores ─────────────────────────────────────────────────────────────
const mockRequests = [...MOCK_OPEN_REQUESTS];
const mockOffers = [];
const mockBookings = [];
const mockChangeRequests = [];
const mockFlightStatuses = {};
const mockFlightStatusResponses = {};
let offerIdCounter = 1;
let bookingIdCounter = 1;
let changeRequestIdCounter = 1;

const FLIGHT_STATUS_VALUES = ['ON_TIME', 'DELAYED', 'EARLY', 'LANDED', 'CANCELLED', 'UNAVAILABLE'];

function getOrInitFlightStatus(flightId) {
  if (!mockFlightStatuses[flightId]) {
    const flight = MOCK_FLIGHTS.find((f) => f.flight_id === flightId);
    if (!flight) return null;
    mockFlightStatuses[flightId] = {
      flight_id: flightId,
      flight_number: flight.flight_number,
      airline: flight.airline,
      from: flight.from,
      to: flight.to,
      scheduled_arrival: flight.arrival_local,
      estimated_arrival: flight.arrival_local,
      status: 'ON_TIME',
      updated_at: new Date().toISOString().slice(0, 19),
    };
  }
  return mockFlightStatuses[flightId];
}

// ─── Mock router ─────────────────────────────────────────────────────────────
function mockResponse(path, options = {}) {
  const method = options.method || 'GET';
  const body = options.body ? JSON.parse(options.body) : null;

  // POST /bookings/:id/change-requests
  if (method === 'POST' && path.includes('/bookings/') && path.endsWith('/change-requests')) {
    const bookingId = parseInt(path.split('/bookings/')[1].split('/')[0]);
    const booking = mockBookings.find((b) => b.id === bookingId);
    if (!booking) throw Object.assign(new Error('Booking not found.'), { status: 404 });
    if (booking.status !== 'CONFIRMED') throw Object.assign(new Error('Booking is not confirmed.'), { rule: 'R9', status: 400 });
    if (!body.new_flight_id && !body.new_pickup_time) throw Object.assign(new Error('Provide a new flight or pickup time.'), { rule: 'R9', status: 400 });
    const pending = mockChangeRequests.find((c) => c.booking_id === bookingId && c.status === 'PENDING');
    if (pending) throw Object.assign(new Error('A change request is already pending for this booking.'), { rule: 'R9', status: 400 });
    if (body.new_pickup_time) {
      const flight = MOCK_FLIGHTS.find((f) => f.flight_id === (body.new_flight_id || booking.flight_id));
      if (flight) {
        const diffMins = (new Date(body.new_pickup_time) - new Date(flight.arrival_local)) / 60000;
        if (diffMins < 60) throw Object.assign(new Error('New pickup must be at least 60 minutes after landing.'), { rule: 'R2', status: 400 });
      }
    }
    const cr = {
      id: changeRequestIdCounter++,
      booking_id: bookingId,
      booking_number: booking.booking_number,
      new_flight_id: body.new_flight_id || null,
      new_pickup_time: body.new_pickup_time || null,
      status: 'PENDING',
      rejection_reason: null,
      created_at: new Date().toISOString().slice(0, 19),
      resolved_at: null,
    };
    mockChangeRequests.push(cr);
    return cr;
  }

  // GET /bookings/:id/change-requests
  if (method === 'GET' && path.includes('/bookings/') && path.endsWith('/change-requests')) {
    const bookingId = parseInt(path.split('/bookings/')[1].split('/')[0]);
    return mockChangeRequests.filter((c) => c.booking_id === bookingId);
  }

  // GET /suppliers/:companyId/change-requests
  if (method === 'GET' && path.includes('/suppliers/') && path.endsWith('/change-requests')) {
    const companyId = decodeURIComponent(path.split('/suppliers/')[1].split('/')[0]);
    const companyBookings = mockBookings.filter((b) => b.company_id === companyId);
    return mockChangeRequests.filter((cr) => companyBookings.some((b) => b.id === cr.booking_id));
  }

  // PATCH /bookings/change-requests/:id
  if (method === 'PATCH' && path.includes('/bookings/change-requests/')) {
    const crId = parseInt(path.split('/bookings/change-requests/')[1]);
    const cr = mockChangeRequests.find((c) => c.id === crId);
    if (!cr) throw Object.assign(new Error('Change request not found.'), { status: 404 });
    if (cr.status !== 'PENDING') throw Object.assign(new Error('Change request already resolved.'), { rule: 'R9', status: 400 });
    if (body.status === 'ACCEPTED') {
      const booking = mockBookings.find((b) => b.id === cr.booking_id);
      if (cr.new_flight_id) {
        const newFlight = MOCK_FLIGHTS.find((f) => f.flight_id === cr.new_flight_id);
        if (!newFlight) throw Object.assign(new Error('New flight not found.'), { status: 404 });
        const pickupTime = cr.new_pickup_time || (() => { const d = new Date(newFlight.arrival_local); d.setMinutes(d.getMinutes() + 60); return d.toISOString().slice(0, 19); })();
        booking.flight_id = cr.new_flight_id;
        booking._flight = newFlight;
        booking.pickup_time = pickupTime;
      }
      if (cr.new_pickup_time) booking.pickup_time = cr.new_pickup_time;
      cr.status = 'ACCEPTED';
    } else {
      cr.status = 'REJECTED';
      cr.rejection_reason = body.rejection_reason || null;
    }
    cr.resolved_at = new Date().toISOString().slice(0, 19);
    return cr;
  }

  // GET /flights/:flightId/status
  if (method === 'GET' && path.includes('/flights/') && path.endsWith('/status')) {
    const flightId = path.split('/flights/')[1].split('/')[0];
    const status = getOrInitFlightStatus(flightId);
    if (!status) throw Object.assign(new Error('Flight not found.'), { status: 404 });
    return status;
  }

  // PATCH /flights/:flightId/status
  if (method === 'PATCH' && path.includes('/flights/') && path.endsWith('/status')) {
    const flightId = path.split('/flights/')[1].split('/')[0];
    const status = getOrInitFlightStatus(flightId);
    if (!status) throw Object.assign(new Error('Flight not found.'), { status: 404 });
    if (body.status && FLIGHT_STATUS_VALUES.includes(body.status)) status.status = body.status;
    if (body.estimated_arrival) status.estimated_arrival = body.estimated_arrival;
    status.updated_at = new Date().toISOString().slice(0, 19);
    mockBookings.filter((b) => b.flight_id === flightId && b.status === 'CONFIRMED').forEach((b) => {
      const eta = new Date(status.estimated_arrival);
      eta.setMinutes(eta.getMinutes() + 60);
      b._suggested_pickup = eta.toISOString().slice(0, 19);
    });
    return status;
  }

  // GET /bookings/:id/flight-status-notification
  if (method === 'GET' && path.includes('/bookings/') && path.endsWith('/flight-status-notification')) {
    const bookingId = parseInt(path.split('/bookings/')[1].split('/')[0]);
    const booking = mockBookings.find((b) => b.id === bookingId);
    if (!booking) throw Object.assign(new Error('Booking not found.'), { status: 404 });
    return {
      flight_status: getOrInitFlightStatus(booking.flight_id),
      supplier_response: mockFlightStatusResponses[bookingId] || null,
      suggested_pickup: booking._suggested_pickup || null,
    };
  }

  // POST /bookings/:id/flight-status-response
  if (method === 'POST' && path.includes('/bookings/') && path.endsWith('/flight-status-response')) {
    const bookingId = parseInt(path.split('/bookings/')[1].split('/')[0]);
    const booking = mockBookings.find((b) => b.id === bookingId);
    if (!booking) throw Object.assign(new Error('Booking not found.'), { status: 404 });
    if (!['CONFIRMED_REVISED', 'UNAVAILABLE'].includes(body.response)) {
      throw Object.assign(new Error('Invalid response value.'), { status: 400 });
    }
    const resp = {
      booking_id: bookingId,
      company_name: booking.company_name,
      response: body.response,
      note: body.note || null,
      responded_at: new Date().toISOString().slice(0, 19),
    };
    mockFlightStatusResponses[bookingId] = resp;
    if (body.response === 'CONFIRMED_REVISED' && booking._suggested_pickup) {
      booking.pickup_time = booking._suggested_pickup;
    }
    return resp;
  }

  // GET /bookings/by-number/:bookingNumber
  if (method === 'GET' && path.startsWith('/bookings/by-number/')) {
    const bookingNumber = path.split('/bookings/by-number/')[1];
    const booking = mockBookings.find((b) => b.booking_number === bookingNumber);
    if (!booking) throw Object.assign(new Error('Booking not found.'), { status: 404 });
    return booking;
  }

  // GET /traveler/requests/:requestId  (must come before /offers check)
  if (method === 'GET' && path.includes('/traveler/requests/') && !path.endsWith('/offers')) {
    const requestId = path.split('/traveler/requests/')[1];
    const req = mockRequests.find((r) => r.request_id === requestId);
    if (!req) throw Object.assign(new Error('Ride request not found.'), { status: 404 });
    return req;
  }

  // GET /traveler/requests/:requestId/offers
  if (method === 'GET' && path.includes('/traveler/requests/') && path.endsWith('/offers')) {
    const requestId = path.split('/traveler/requests/')[1].split('/')[0];
    return mockOffers.filter((o) => o.request_id === requestId);
  }

  // POST /traveler/offers/:offerId/reject
  if (method === 'POST' && path.includes('/traveler/offers/') && path.endsWith('/reject')) {
    const offerId = parseInt(path.split('/traveler/offers/')[1].split('/')[0]);
    const offer = mockOffers.find((o) => o.id === offerId);
    if (!offer) throw Object.assign(new Error('Offer not found.'), { status: 404 });
    if (offer.status !== 'OPEN') throw Object.assign(new Error('Offer is no longer available.'), { status: 400 });
    offer.status = 'REJECTED';
    return offer;
  }

  // POST /traveler/offers/:offerId/book
  if (method === 'POST' && path.includes('/traveler/offers/') && path.endsWith('/book')) {
    const offerId = parseInt(path.split('/traveler/offers/')[1].split('/')[0]);
    const offer = mockOffers.find((o) => o.id === offerId);
    if (!offer) throw Object.assign(new Error('Offer not found.'), { status: 404 });
    if (offer.status !== 'OPEN') throw Object.assign(new Error('Offer is no longer available.'), { rule: 'R8', status: 400 });
    const request = mockRequests.find((r) => r.request_id === offer.request_id);
    const flight = MOCK_FLIGHTS.find((f) => f.flight_id === request.flight_id);
    const hotel = MOCK_HOTELS.find((h) => h.hotel_id === request.hotel_id);
    const route = MOCK_ROUTES.find((r) => r.airport === flight.to && r.city === hotel.city && r.area === hotel.area);
    const bookingNumber = String(Math.floor(1000000000 + Math.random() * 9000000000));
    const busyEnd = new Date(request.pickup_time);
    busyEnd.setMinutes(busyEnd.getMinutes() + 2 * (route?.drive_minutes ?? 60));
    const booking = {
      id: bookingIdCounter++,
      booking_number: bookingNumber,
      request_id: offer.request_id,
      company_id: offer.company_id,
      company_name: offer.company_name,
      car_id: offer.vehicle_id,
      flight_id: request.flight_id,
      hotel_id: request.hotel_id,
      pickup_time: request.pickup_time,
      busy_start_time: request.pickup_time,
      busy_end_time: busyEnd.toISOString().slice(0, 19),
      price_inr: offer.offered_price_inr,
      status: 'CONFIRMED',
      is_pre_booked: false,
      created_at: new Date().toISOString().slice(0, 19),
      _flight: flight,
      _hotel: hotel,
      _vehicle_type: offer.vehicle_type,
      _company_name: offer.company_name,
      _adults: request.adults,
      _child_ages: request.child_ages,
      _large_bags: request.large_bags,
      _small_bags: request.small_bags,
      _traveler_name: request.traveler_name,
    };
    mockBookings.push(booking);
    offer.status = 'BOOKED';
    mockOffers
      .filter((o) => o.request_id === offer.request_id && o.id !== offerId)
      .forEach((o) => { o.status = 'NOT_CHOSEN'; });
    request.status = 'FULFILLED';
    return booking;
  }

  // GET /catalog/flights
  if (method === 'GET' && path === '/catalog/flights') return MOCK_FLIGHTS;

  // GET /catalog/hotels
  if (method === 'GET' && path === '/catalog/hotels') return MOCK_HOTELS;

  // GET /catalog/companies
  if (method === 'GET' && path === '/catalog/companies') return MOCK_COMPANIES;

  // GET /catalog/routes/lookup
  if (method === 'GET' && path.startsWith('/catalog/routes/lookup')) {
    const params = new URLSearchParams(path.split('?')[1]);
    const route = MOCK_ROUTES.find(
      (r) => r.airport === params.get('airport') &&
             r.city === params.get('city') &&
             r.area === params.get('area')
    );
    if (!route) throw Object.assign(new Error('Route not found for this airport/city/area combination.'), { status: 404 });
    return route;
  }

  // GET /suppliers/:companyId/open-requests
  if (method === 'GET' && path.includes('/open-requests')) {
    const companyId = path.split('/suppliers/')[1].split('/')[0];
    const company = MOCK_COMPANIES.find((c) => c.company_id === companyId);
    if (!company) throw Object.assign(new Error('Company not found.'), { status: 404 });
    return mockRequests
      .filter((r) => r.status === 'OPEN')
      .map((r) => {
        const flight = MOCK_FLIGHTS.find((f) => f.flight_id === r.flight_id);
        const hotel = MOCK_HOTELS.find((h) => h.hotel_id === r.hotel_id);
        if (!flight || !hotel) return null;
        if (!company.airports.includes(flight.to)) return null;
        if (!company.areas.some((a) => a.city === hotel.city && a.area === hotel.area)) return null;
        const route = MOCK_ROUTES.find((ro) => ro.airport === flight.to && ro.city === hotel.city && ro.area === hotel.area);
        const eligibleVehicles = company.vehicles.filter(
          (v) => v.seats >= r.adults + r.child_ages.length &&
                 v.large_bags >= r.large_bags &&
                 v.small_bags >= r.small_bags &&
                 (!r.car_type || v.type === r.car_type)
        );
        if (eligibleVehicles.length === 0) return null;
        return {
          request: r,
          eligible_vehicles: eligibleVehicles,
          price_min_inr: route?.price_min_inr ?? 0,
          price_max_inr: route?.price_max_inr ?? 0,
          drive_minutes: route?.drive_minutes ?? 0,
        };
      })
      .filter(Boolean);
  }

  // POST /traveler/requests
  if (method === 'POST' && path === '/traveler/requests') {
    const flight = MOCK_FLIGHTS.find((f) => f.flight_id === body.flight_id);
    const hotel = MOCK_HOTELS.find((h) => h.hotel_id === body.hotel_id);
    if (!flight) throw Object.assign(new Error('Flight not found.'), { rule: 'R1', status: 400 });
    if (!hotel) throw Object.assign(new Error('Hotel not found.'), { rule: 'R1', status: 400 });
    if (body.adults < 1) throw Object.assign(new Error('At least one adult is required.'), { rule: 'R1', status: 400 });
    if (body.pickup_time) {
      const diffMins = (new Date(body.pickup_time) - new Date(flight.arrival_local)) / 60000;
      if (diffMins < 60) throw Object.assign(new Error('Pickup must be at least 60 minutes after landing.'), { rule: 'R2', status: 400 });
    }
    const route = MOCK_ROUTES.find((r) => r.airport === flight.to && r.city === hotel.city && r.area === hotel.area);
    if (!route) throw Object.assign(new Error('No route found between this airport and hotel.'), { rule: 'R1', status: 400 });
    if (body.price_inr < route.price_min_inr || body.price_inr > route.price_max_inr) {
      throw Object.assign(new Error(`Price must be between ₹${route.price_min_inr} and ₹${route.price_max_inr}.`), { rule: 'R3', status: 400 });
    }
    const pickupTime = body.pickup_time || (() => { const d = new Date(flight.arrival_local); d.setMinutes(d.getMinutes() + 60); return d.toISOString().slice(0, 19); })();
    const newRequest = {
      request_id: `REQ-${String(mockRequests.length + 1).padStart(4, '0')}`,
      traveler_name: body.traveler_name,
      flight_id: body.flight_id,
      hotel_id: body.hotel_id,
      adults: body.adults,
      child_ages: body.child_ages || [],
      large_bags: body.large_bags,
      small_bags: body.small_bags,
      pickup_time: pickupTime,
      car_type: body.car_type || null,
      price_inr: body.price_inr,
      status: 'OPEN',
      created_at: new Date().toISOString().slice(0, 19),
    };
    mockRequests.push(newRequest);
    return newRequest;
  }

  // POST /suppliers/offers
  if (method === 'POST' && path === '/suppliers/offers') {
    const request = mockRequests.find((r) => r.request_id === body.request_id);
    const company = MOCK_COMPANIES.find((c) => c.company_id === body.company_id);
    if (!request) throw Object.assign(new Error('Request not found.'), { status: 404 });
    if (!company) throw Object.assign(new Error('Company not found.'), { status: 404 });
    const vehicle = company.vehicles.find((v) => v.vehicle_id === body.vehicle_id);
    if (!vehicle) throw Object.assign(new Error('Vehicle does not belong to this supplier.'), { rule: 'R7', status: 400 });
    const flight = MOCK_FLIGHTS.find((f) => f.flight_id === request.flight_id);
    const hotel = MOCK_HOTELS.find((h) => h.hotel_id === request.hotel_id);
    const route = MOCK_ROUTES.find((r) => r.airport === flight.to && r.city === hotel.city && r.area === hotel.area);
    if (body.offered_price_inr < route.price_min_inr || body.offered_price_inr > route.price_max_inr) {
      throw Object.assign(new Error(`Offered price must be between ₹${route.price_min_inr} and ₹${route.price_max_inr}.`), { rule: 'R7', status: 400 });
    }
    const offer = {
      id: offerIdCounter++,
      request_id: body.request_id,
      company_id: body.company_id,
      company_name: company.name,
      vehicle_id: body.vehicle_id,
      vehicle_type: vehicle.type,
      seats: vehicle.seats,
      large_bags: vehicle.large_bags,
      small_bags: vehicle.small_bags,
      offered_price_inr: body.offered_price_inr,
      status: 'OPEN',
      created_at: new Date().toISOString().slice(0, 19),
    };
    mockOffers.push(offer);
    return offer;
  }

  throw new Error(`Mock: unhandled route ${method} ${path}`);
}

// ─── Config ───────────────────────────────────────────────────────────────────
export const USE_MOCK = false; // Set to true to use in-memory mock instead of backend

// ─── Core fetch ──────────────────────────────────────────────────────────────
export async function apiFetch(path, options = {}) {
  if (USE_MOCK) {
    return mockResponse(path, options);
  }

  let res;
  try {
    res = await fetch(`${BASE_URL}${path}`, {
      headers: { 'Content-Type': 'application/json' },
      ...options,
    });
  } catch {
    throw new Error('Backend is not reachable. Start the Spring Boot server on port 8080.');
  }

  let data = null;
  try {
    const text = await res.text();
    data = text ? JSON.parse(text) : null;
  } catch {
    throw new Error('Invalid response from server.');
  }

  if (!res.ok) {
    const error = new Error(data?.message || `Request failed (${res.status})`);
    error.rule = data?.rule ?? null;
    error.status = res.status;
    throw error;
  }

  return data;
}
