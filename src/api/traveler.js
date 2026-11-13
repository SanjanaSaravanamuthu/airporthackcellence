import { apiFetch } from './client';

export const submitRideRequest = (payload) =>
  apiFetch('/traveler/requests', {
    method: 'POST',
    body: JSON.stringify(payload),
  });

export const getRequest = (requestId) =>
  apiFetch(`/traveler/requests/${requestId}`);

export const getOffers = (requestId) =>
  apiFetch(`/traveler/requests/${requestId}/offers`);

export const bookOffer = (offerId) =>
  apiFetch(`/traveler/offers/${offerId}/book`, { method: 'POST' });

export const declineOffer = (offerId) =>
  apiFetch(`/traveler/offers/${offerId}/decline`, { method: 'POST' });
