import { useState } from 'react';
import { submitOffer } from '../../api/supplier';
import './AssignCarModal.css';

function formatDateTime(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' });
}

export default function AssignCarModal({ item, company, onClose, onSuccess }) {
  const { request, eligible_vehicles, price_min_inr, price_max_inr } = item;

  const [selectedVehicleId, setSelectedVehicleId] = useState('');
  const [offerPrice, setOfferPrice] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(null);

  const selectedVehicle = eligible_vehicles.find((v) => v.vehicle_id === selectedVehicleId);

  const priceNum = Number(offerPrice);
  const priceError =
    offerPrice && (priceNum < price_min_inr || priceNum > price_max_inr)
      ? `Price must be between ₹${price_min_inr.toLocaleString('en-IN')} and ₹${price_max_inr.toLocaleString('en-IN')}.`
      : '';

  const canSubmit = selectedVehicleId && offerPrice && !priceError && !submitting;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!canSubmit) return;
    setSubmitting(true);
    setError('');
    try {
      const result = await submitOffer({
        request_id: request.request_id,
        company_id: company.company_id,
        vehicle_id: selectedVehicleId,
        offered_price_inr: priceNum,
      });
      setSuccess(result);
      onSuccess();
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal">
        <div className="modal-header">
          <h3>Assign Car & Submit Offer</h3>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>

        {success ? (
          <div className="modal-success">
            <div className="modal-success-icon">✅</div>
            <h4>Taxi offer submitted successfully.</h4>
            <div className="modal-success-details">
              <div><span>Car</span><strong>{success.vehicle_id} — {success.vehicle_type}</strong></div>
              <div><span>Offered Price</span><strong>₹{Number(success.offered_price_inr).toLocaleString('en-IN')}</strong></div>
              <div><span>Status</span><strong>{success.status}</strong></div>
            </div>
            <button className="btn-primary" onClick={onClose}>Back to Dashboard</button>
          </div>
        ) : (
          <form onSubmit={handleSubmit} noValidate>
            {/* Request summary */}
            <section className="modal-section">
              <h4>Ride Request</h4>
              <div className="modal-grid">
                <div className="modal-field">
                  <span className="field-label">Request ID</span>
                  <span className="field-value">{request.request_id}</span>
                </div>
                <div className="modal-field">
                  <span className="field-label">Flight</span>
                  <span className="field-value">{request.flight_id}</span>
                </div>
                <div className="modal-field">
                  <span className="field-label">Hotel</span>
                  <span className="field-value">{request.hotel_id}</span>
                </div>
                <div className="modal-field">
                  <span className="field-label">Pickup Time</span>
                  <span className="field-value">{formatDateTime(request.pickup_time)}</span>
                </div>
                <div className="modal-field">
                  <span className="field-label">Passengers</span>
                  <span className="field-value">
                    {request.adults} adult{request.adults !== 1 ? 's' : ''}
                    {request.child_ages?.length > 0 && `, ${request.child_ages.length} child${request.child_ages.length !== 1 ? 'ren' : ''} (ages: ${request.child_ages.join(', ')})`}
                  </span>
                </div>
                <div className="modal-field">
                  <span className="field-label">Luggage</span>
                  <span className="field-value">{request.large_bags} large, {request.small_bags} small</span>
                </div>
                <div className="modal-field">
                  <span className="field-label">Traveler's Price</span>
                  <span className="field-value field-price">₹{Number(request.price_inr).toLocaleString('en-IN')}</span>
                </div>
                <div className="modal-field">
                  <span className="field-label">Route Price Range</span>
                  <span className="field-value">₹{Number(price_min_inr).toLocaleString('en-IN')} – ₹{Number(price_max_inr).toLocaleString('en-IN')}</span>
                </div>
              </div>
            </section>

            {/* Vehicle selection */}
            <section className="modal-section">
              <h4>Select a Car</h4>
              <div className="vehicle-list">
                {eligible_vehicles.map((v) => (
                  <label
                    key={v.vehicle_id}
                    className={`vehicle-card ${selectedVehicleId === v.vehicle_id ? 'vehicle-card-selected' : ''}`}
                  >
                    <input
                      type="radio"
                      name="vehicle"
                      value={v.vehicle_id}
                      checked={selectedVehicleId === v.vehicle_id}
                      onChange={() => setSelectedVehicleId(v.vehicle_id)}
                    />
                    <div className="vehicle-info">
                      <span className="vehicle-id">{v.vehicle_id}</span>
                      <span className="vehicle-type">{v.type}</span>
                    </div>
                    <div className="vehicle-specs">
                      <span>💺 {v.seats} seats</span>
                      <span>🧳 {v.large_bags} large</span>
                      <span>👜 {v.small_bags} small</span>
                    </div>
                    <div className="vehicle-fit">
                      <span className={request.adults + (request.child_ages?.length ?? 0) <= v.seats ? 'fit-ok' : 'fit-warn'}>
                        {request.adults + (request.child_ages?.length ?? 0)} / {v.seats} seats
                      </span>
                      <span className={request.large_bags <= v.large_bags ? 'fit-ok' : 'fit-warn'}>
                        {request.large_bags} / {v.large_bags} large bags
                      </span>
                      <span className={request.small_bags <= v.small_bags ? 'fit-ok' : 'fit-warn'}>
                        {request.small_bags} / {v.small_bags} small bags
                      </span>
                    </div>
                  </label>
                ))}
              </div>
            </section>

            {/* Offer price */}
            <section className="modal-section">
              <h4>Your Offer Price (₹)</h4>
              <p className="modal-hint">
                Route range: ₹{Number(price_min_inr).toLocaleString('en-IN')} – ₹{Number(price_max_inr).toLocaleString('en-IN')}.
                Your price does not need to match the traveler's expected price.
              </p>
              <input
                className={`form-input${priceError ? ' input-error' : ''}`}
                type="number"
                min={price_min_inr}
                max={price_max_inr}
                placeholder="Enter amount in INR"
                value={offerPrice}
                onChange={(e) => setOfferPrice(e.target.value)}
              />
              {priceError && <div className="form-error">{priceError}</div>}
            </section>

            {error && <div className="form-error submit-error">⚠ {error}</div>}

            <div className="modal-actions">
              <button type="button" className="btn-secondary-active" onClick={onClose}>Cancel</button>
              <button type="submit" className="btn-primary" disabled={!canSubmit}>
                {submitting ? 'Submitting…' : 'Submit Taxi Offer'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
