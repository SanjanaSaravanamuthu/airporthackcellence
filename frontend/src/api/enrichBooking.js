import { getFlights, getHotels } from './catalog';
import { getRequest } from './traveler';

let _flightsCache = null;
let _hotelsCache = null;

async function getCatalog() {
  if (!_flightsCache || !_hotelsCache) {
    [_flightsCache, _hotelsCache] = await Promise.all([getFlights(), getHotels()]);
  }
  return { flights: _flightsCache, hotels: _hotelsCache };
}

/**
 * Takes a raw BookingResponse from the backend and attaches enriched fields
 * (_flight, _hotel, _vehicle_type, _adults, _child_ages, _large_bags,
 *  _small_bags, _traveler_name) so BookingSummary renders correctly.
 *
 * If the booking already has _flight (mock mode), it is returned as-is.
 */
export async function enrichBooking(booking) {
  if (booking._flight) return booking; // already enriched (mock mode)

  const { flights, hotels } = await getCatalog();
  const flight = flights.find((f) => f.flight_id === booking.flight_id) ?? null;
  const hotel = hotels.find((h) => h.hotel_id === booking.hotel_id) ?? null;

  let request = null;
  try {
    request = await getRequest(booking.request_id);
  } catch {
    // non-fatal — display what we have
  }

  return {
    ...booking,
    _flight: flight,
    _hotel: hotel,
    _vehicle_type: booking.car_id ? booking.car_id.split('-')[0] : null, // fallback
    _adults: request?.adults ?? null,
    _child_ages: request?.child_ages ?? [],
    _large_bags: request?.large_bags ?? null,
    _small_bags: request?.small_bags ?? null,
    _traveler_name: request?.traveler_name ?? null,
  };
}
