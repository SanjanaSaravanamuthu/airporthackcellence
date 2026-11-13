import { useState, useEffect } from 'react';
import { getFlights } from '../../api/catalog';
import { submitChangeRequest, getChangeRequests } from '../../api/bookings';
import './BookingSummary.css';

function formatDateTime(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' });
}

function Field({ label, value }) {
  return (
    <div className="bs-field">
      <span className="bs-label">{label}</span>
      <span className="bs-value">{value ?? '—'}</span>
    </div>
  );
}

function ChangeRequestForm({ booking, onSubmitted, onCancel }) {
  const [flights, setFlights] = useState([]);
  const [mode, setMode] = useState('pickup'); // 'flight' | 'pickup'
  const [newFlightId, setNewFlightId] = useState('');
  const [newPickupTime, setNewPickupTime] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    getFlights().then(setFlights).catch(() => {});
  }, []);

  const selectedFlight = flights.find((f) => f.flight_id === newFlightId);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (mode === 'flight' && !newFlightId) return;
    if (mode === 'pickup' && !newPickupTime) return;
    setSubmitting(true);
    setError('');
    try {
      const payload = {
        new_flight_id: mode === 'flight' ? newFlightId : null,
        new_pickup_time: mode === 'pickup' ? `${newPickupTime}:00` : null,
      };
      const result = await submitChangeRequest(booking.id, payload);
      onSubmitted(result);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form className="cr-form" onSubmit={handleSubmit} noValidate>
      <h4>Request a Change</h4>

      <div className="cr-mode-tabs">
        <button type="button" className={`cr-tab${mode === 'pickup' ? ' cr-tab-active' : ''}`} onClick={() => setMode('pickup')}>
          Change Pickup Time
        </button>
        <button type="button" className={`cr-tab${mode === 'flight' ? ' cr-tab-active' : ''}`} onClick={() => setMode('flight')}>
          Change Flight
        </button>
      </div>

      {mode === 'pickup' && (
        <div className="cr-field">
          <label className="bs-label">New Pickup Date & Time</label>
          <input
            className="form-input"
            type="datetime-local"
            value={newPickupTime}
            onChange={(e) => setNewPickupTime(e.target.value)}
            required
          />
        </div>
      )}

      {mode === 'flight' && (
        <div className="cr-field">
          <label className="bs-label">New Flight</label>
          <select className="form-input" value={newFlightId} onChange={(e) => setNewFlightId(e.target.value)} required>
            <option value="">— Select a flight —</option>
            {flights.map((f) => (
              <option key={f.flight_id} value={f.flight_id}>
                {f.flight_number} · {f.from} → {f.to} · Arrives {formatDateTime(f.arrival_local)}
              </option>
            ))}
          </select>
          {selectedFlight && (
            <div className="cr-hint">
              ✈ Arrives at <strong>{selectedFlight.to}</strong> on <strong>{formatDateTime(selectedFlight.arrival_local)}</strong>
            </div>
          )}
        </div>
      )}

      {error && <div className="cr-error">⚠ {error}</div>}

      <div className="cr-actions">
        <button type="button" className="btn-secondary-active" onClick={onCancel}>Cancel</button>
        <button type="submit" className="btn-primary" disabled={submitting || (mode === 'flight' && !newFlightId) || (mode === 'pickup' && !newPickupTime)}>
          {submitting ? 'Submitting…' : 'Submit Change Request'}
        </button>
      </div>
    </form>
  );
}

function ChangeRequestHistory({ changeRequests }) {
  if (!changeRequests?.length) return null;
  return (
    <section className="bs-section">
      <h4>Change Requests</h4>
      <div className="cr-history">
        {changeRequests.map((cr) => (
          <div key={cr.id} className={`cr-item cr-item-${cr.status.toLowerCase()}`}>
            <div className="cr-item-top">
              <span className="cr-item-type">
                {cr.new_flight_id ? `✈ New Flight: ${cr.new_flight_id}` : ''}
                {cr.new_pickup_time ? `🕐 New Pickup: ${formatDateTime(cr.new_pickup_time)}` : ''}
              </span>
              <span className={`cr-badge cr-badge-${cr.status.toLowerCase()}`}>{cr.status}</span>
            </div>
            {cr.rejection_reason && (
              <div className="cr-rejection">Reason: {cr.rejection_reason}</div>
            )}
            <div className="cr-meta">Submitted {formatDateTime(cr.created_at)}{cr.resolved_at ? ` · Resolved ${formatDateTime(cr.resolved_at)}` : ''}</div>
          </div>
        ))}
      </div>
    </section>
  );
}

export default function BookingSummary({ booking, allowChangeRequest = false }) {
  const [showForm, setShowForm] = useState(false);
  const [changeRequests, setChangeRequests] = useState([]);

  useEffect(() => {
    if (booking?.id) {
      getChangeRequests(booking.id).then(setChangeRequests).catch(() => {});
    }
  }, [booking?.id]);

  const flight = booking._flight || null;
  const hotel = booking._hotel || null;

  const handleCopy = () => navigator.clipboard.writeText(booking.booking_number);

  const handleSubmitted = (cr) => {
    setChangeRequests((prev) => [...prev, cr]);
    setShowForm(false);
  };

  const hasPending = changeRequests.some((c) => c.status === 'PENDING');

  return (
    <div className="bs-card">
      <div className="bs-number-section">
        <span className="bs-number-label">Booking Number</span>
        <span className="bs-number">{booking.booking_number}</span>
        <button className="bs-copy-btn" onClick={handleCopy}>📋 Copy</button>
      </div>

      <div className={`bs-status bs-status-${booking.status?.toLowerCase()}`}>
        {booking.status}
      </div>

      <section className="bs-section">
        <h4>Journey Details</h4>
        <div className="bs-grid">
          <Field label="Traveler" value={booking._traveler_name} />
          <Field label="Flight" value={flight ? `${flight.flight_number} (${flight.airline})` : booking.flight_id} />
          <Field label="Arrival Airport" value={flight?.to} />
          <Field label="Flight Arrival" value={formatDateTime(flight?.arrival_local)} />
          <Field label="Hotel" value={hotel?.name ?? booking.hotel_id} />
          <Field label="City / Area" value={hotel ? `${hotel.city}, ${hotel.area}` : '—'} />
          <Field label="Pickup Time" value={formatDateTime(booking.pickup_time)} />
          <Field label="Adults" value={booking._adults} />
          <Field label="Children" value={booking._child_ages?.length > 0 ? `${booking._child_ages.length} (ages: ${booking._child_ages.join(', ')})` : 'None'} />
          <Field label="Large Bags" value={booking._large_bags} />
          <Field label="Small Bags" value={booking._small_bags} />
        </div>
      </section>

      <section className="bs-section">
        <h4>Taxi Details</h4>
        <div className="bs-grid">
          <Field label="Supplier" value={booking.company_name ?? booking._company_name} />
          <Field label="Car Type" value={booking._vehicle_type} />
          <Field label="Car ID" value={booking.car_id} />
          <Field label="Offered Price" value={`₹${Number(booking.price_inr).toLocaleString('en-IN')}`} />
        </div>
      </section>

      <ChangeRequestHistory changeRequests={changeRequests} />

      {allowChangeRequest && booking.status === 'CONFIRMED' && (
        <section className="bs-section">
          {showForm ? (
            <ChangeRequestForm
              booking={booking}
              onSubmitted={handleSubmitted}
              onCancel={() => setShowForm(false)}
            />
          ) : (
            <button
              className="btn-primary"
              onClick={() => setShowForm(true)}
              disabled={hasPending}
              title={hasPending ? 'A change request is already pending' : ''}
            >
              {hasPending ? 'Change Request Pending…' : 'Request a Change'}
            </button>
          )}
        </section>
      )}
    </div>
  );
}
