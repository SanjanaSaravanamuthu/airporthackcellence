import { apiFetch } from './client';

export const getFlights = () => apiFetch('/catalog/flights');

export const getHotels = () => apiFetch('/catalog/hotels');

export const getHotelsByAirport = (airportCode) =>
  apiFetch(`/catalog/hotels/by-airport/${encodeURIComponent(airportCode)}`);

export const getRouteLookup = (airport, city, area) =>
  apiFetch(`/catalog/routes/lookup?airport=${encodeURIComponent(airport)}&city=${encodeURIComponent(city)}&area=${encodeURIComponent(area)}`);
