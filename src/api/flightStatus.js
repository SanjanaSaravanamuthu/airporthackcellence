import { apiFetch } from './client';

export const getFlightStatus = (flightId) =>
  apiFetch(`/flights/${flightId}/status`);

export const updateFlightStatus = (flightId, payload) =>
  apiFetch(`/flights/${flightId}/status`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  });

export const getFlightStatusNotification = (bookingId) =>
  apiFetch(`/bookings/${bookingId}/flight-status-notification`);

export const submitFlightStatusResponse = (bookingId, payload) =>
  apiFetch(`/bookings/${bookingId}/flight-status-response`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
