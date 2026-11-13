import { apiFetch } from './client';

export const getCompanies = () => apiFetch('/catalog/companies');

export const getSupplierOpenRequests = (companyId) =>
  apiFetch(`/suppliers/${encodeURIComponent(companyId)}/open-requests`);

export const submitOffer = (payload) =>
  apiFetch('/suppliers/offers', { method: 'POST', body: JSON.stringify(payload) });

export const getSupplierChangeRequests = (companyId) =>
  apiFetch(`/suppliers/${encodeURIComponent(companyId)}/change-requests`);

export const resolveChangeRequest = (changeRequestId, payload) =>
  apiFetch(`/bookings/change-requests/${changeRequestId}`, {
    method: 'PATCH',
    body: JSON.stringify(payload),
  });
