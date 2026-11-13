import { apiFetch } from './client';

export const getBookingByNumber = (bookingNumber) =>
  apiFetch(`/bookings/by-number/${bookingNumber}`);

export const submitChangeRequest = (bookingId, payload) =>
  apiFetch(`/bookings/${bookingId}/change-requests`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });

export const getChangeRequests = (bookingId) =>
  apiFetch(`/bookings/${bookingId}/change-requests`);
