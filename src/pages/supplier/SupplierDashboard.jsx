import { useState, useEffect } from 'react';
import { getSupplierOpenRequests } from '../../api/supplier';
import AssignCarModal from './AssignCarModal';
import ChangeRequestsPanel from './ChangeRequestsPanel';
import './SupplierDashboard.css';

function formatDateTime(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' });
}

function RequestCard({ item, onAssignCar }) {
  const { request, eligible_vehicles, price_min_inr, price_max_inr } = item;

  return (
    <div className="request-card">
      <div className="request-card-top">
        <div className="request-route">
          <span className="request-flight">{request.flight_id}</span>
          <span className="request-arrow">→</span>
          <span className="request-hotel">{request.hotel_id}</span>
        </div>
        <span className="request-status">{request.status}</span>
      </div>

      <div className="request-grid">
        <div className="request-field">
          <span className="field-label">Flight</span>
          <span className="field-value">{request.flight_id}</span>
        </div>
        <div className="request-field">
          <span className="field-label">Pickup Time</span>
          <span className="field-value">{formatDateTime(request.pickup_time)}</span>
        </div>
        <div className="request-field">
          <span className="field-label">Passengers</span>
          <span className="field-value">
            {request.adults} adult{request.adults !== 1 ? 's' : ''}
            {request.child_ages?.length > 0 && (
              <>, {request.child_ages.length} child{request.child_ages.length !== 1 ? 'ren' : ''} (ages: {request.child_ages.join(', ')})</>
            )}
          </span>
        </div>
        <div className="request-field">
          <span className="field-label">Luggage</span>
          <span className="field-value">{request.large_bags} large, {request.small_bags} small</span>
        </div>
        <div className="request-field">
          <span className="field-label">Traveler's Price</span>
          <span className="field-value field-price">₹{Number(request.price_inr).toLocaleString('en-IN')}</span>
        </div>
        <div className="request-field">
          <span className="field-label">Route Price Range</span>
          <span className="field-value">₹{Number(price_min_inr).toLocaleString('en-IN')} – ₹{Number(price_max_inr).toLocaleString('en-IN')}</span>
        </div>
        {request.car_type && (
          <div className="request-field">
            <span className="field-label">Car Preference</span>
            <span className="field-value">{request.car_type}</span>
          </div>
        )}
        <div className="request-field">
          <span className="field-label">Eligible Vehicles</span>
          <span className="field-value">
            {eligible_vehicles.map((v) => `${v.type} (${v.seats} seats)`).join(', ')}
          </span>
        </div>
      </div>

      <div className="request-card-actions">
        <button className="btn-primary" onClick={() => onAssignCar(item)}>
          Assign Car
        </button>
      </div>
    </div>
  );
}

export default function SupplierDashboard({ company }) {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeItem, setActiveItem] = useState(null);

  const fetchRequests = () => {
    if (!company) return;
    setLoading(true);
    setError('');
    getSupplierOpenRequests(company.company_id)
      .then(setRequests)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchRequests(); }, [company]);

  if (!company) return null;

  const submittedCount = requests.filter((r) => r.request.status !== 'OPEN').length;
  const availableCount = requests.filter((r) => r.request.status === 'OPEN').length;

  return (
    <main className="supplier-dashboard">
      <div className="dashboard-header">
        <div>
          <h2>{company.name}</h2>
          <p className="dashboard-subtitle">Eligible ride requests for your company</p>
        </div>
      </div>

      <div className="dashboard-stats">
        <div className="stat-card">
          <span className="stat-value">{loading ? '—' : requests.length}</span>
          <span className="stat-label">Total Eligible</span>
        </div>
        <div className="stat-card">
          <span className="stat-value">{loading ? '—' : availableCount}</span>
          <span className="stat-label">Available</span>
        </div>
        <div className="stat-card">
          <span className="stat-value">{loading ? '—' : submittedCount}</span>
          <span className="stat-label">Offers Submitted</span>
        </div>
      </div>

      {loading && <div className="dashboard-status">Loading eligible requests…</div>}
      {error && <div className="dashboard-status dashboard-error">⚠ Backend unavailable — start the Spring Boot server on port 8080.</div>}

      {!loading && !error && requests.length === 0 && (
        <div className="dashboard-empty">
          <div className="dashboard-empty-icon">🚖</div>
          <p>No ride requests are currently available for your company.</p>
        </div>
      )}

      {!loading && !error && requests.length > 0 && (
        <div className="requests-list">
          {requests.map((item) => (
            <RequestCard
              key={item.request.request_id}
              item={item}
              onAssignCar={setActiveItem}
            />
          ))}
        </div>
      )}

      {activeItem && (
        <AssignCarModal
          item={activeItem}
          company={company}
          onClose={() => setActiveItem(null)}
          onSuccess={() => { setActiveItem(null); fetchRequests(); }}
        />
      )}

      <ChangeRequestsPanel company={company} />
    </main>
  );
}
