import { useState, useEffect, useCallback } from 'react';
import { getBookingByNumber } from '../api/bookings';
import { submitChangeRequest, getChangeRequests } from '../api/bookings';
import {
  getFlightStatusNotification,
  updateFlightStatus,
  submitFlightStatusResponse,
} from '../api/flightStatus';
import './FlightStatusPage.css';

// ─── helpers ────────────────────────────────────────────────────────────────

function fmt(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' });
}

function addMinutes(iso, mins) {
  const d = new Date(iso);
  d.setMinutes(d.getMinutes() + mins);
  const p = (v) => String(v).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth()+1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`;
}

const STATUS_META = {
  ON_TIME:     { label: 'On Time',           cls: 'fs-on_time',     icon: '✅' },
  DELAYED:     { label: 'Delayed',           cls: 'fs-delayed',     icon: '⏰' },
  EARLY:       { label: 'Early',             cls: 'fs-early',       icon: '⚡' },
  LANDED:      { label: 'Landed',            cls: 'fs-landed',      icon: '🛬' },
  CANCELLED:   { label: 'Cancelled',         cls: 'fs-cancelled',   icon: '❌' },
  UNAVAILABLE: { label: 'Status Unavailable',cls: 'fs-unavailable', icon: '❓' },
};

function FlightStatusBadge({ status }) {
  const meta = STATUS_META[status] || STATUS_META.UNAVAILABLE;
  return (
    <span className={`fs-badge ${meta.cls}`}>
      {meta.icon} {meta.label}
    </span>
  );
}

function Field({ label, value, highlight }) {
  return (
    <div className={`fs-field${highlight ? ' fs-field-highlight' : ''}`}>
      <span className="fs-label">{label}</span>
      <span className="fs-value">{value ?? '—'}</span>
    </div>
  );
}

function InfoBanner({ type, children }) {
  return <div className={`fs-banner fs-banner-${type}`}>{children}</div>;
}

// ─── Supplier response panel ─────────────────────────────────────────────────

function SupplierResponsePanel({ booking, notification, onResponded }) {
  const [response, setResponse] = useState('');
  const [note, setNote] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const flightStatus = notification?.flight_status?.status;
  const needsResponse = ['DELAYED', 'EARLY', 'CANCELLED'].includes(flightStatus);
  const alreadyResponded = !!notification?.supplier_response;

  if (!needsResponse) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!response) return;
    setBusy(true);
    setError('');
    try {
      const result = await submitFlightStatusResponse(booking.id, { response, note: note.trim() || null });
      onResponded(result);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  if (alreadyResponded) {
    const r = notification.supplier_response;
    return (
      <section className="fs-section">
        <h4>Your Response</h4>
        <div className={`fs-response-card fs-response-${r.response.toLowerCase()}`}>
          <span className="fs-response-label">
            {r.response === 'CONFIRMED_REVISED' ? '✅ Confirmed revised pickup time' : '⚠ Reported unavailable'}
          </span>
          {r.note && <span className="fs-response-note">{r.note}</span>}
          <span className="fs-response-meta">Responded {fmt(r.responded_at)}</span>
        </div>
      </section>
    );
  }

  return (
    <section className="fs-section">
      <h4>Respond to Flight Update</h4>
      <InfoBanner type="warning">
        The flight status has changed. Please confirm whether you can accommodate the revised pickup time.
      </InfoBanner>
      <form className="fs-response-form" onSubmit={handleSubmit} noValidate>
        <div className="fs-response-options">
          <label className={`fs-option${response === 'CONFIRMED_REVISED' ? ' fs-option-active' : ''}`}>
            <input
              type="radio"
              name="response"
              value="CONFIRMED_REVISED"
              checked={response === 'CONFIRMED_REVISED'}
              onChange={() => setResponse('CONFIRMED_REVISED')}
            />
            <span className="fs-option-icon">✅</span>
            <span className="fs-option-text">
              <strong>Confirm Revised Pickup</strong>
              <small>I can accommodate the updated pickup time</small>
            </span>
          </label>
          <label className={`fs-option${response === 'UNAVAILABLE' ? ' fs-option-active fs-option-danger' : ''}`}>
            <input
              type="radio"
              name="response"
              value="UNAVAILABLE"
              checked={response === 'UNAVAILABLE'}
              onChange={() => setResponse('UNAVAILABLE')}
            />
            <span className="fs-option-icon">⚠</span>
            <span className="fs-option-text">
              <strong>Report Unavailable</strong>
              <small>I cannot accommodate the revised pickup time</small>
            </span>
          </label>
        </div>
        <div className="fs-note-field">
          <label className="fs-label">Note to traveler (optional)</label>
          <input
            className="form-input"
            type="text"
            placeholder="e.g. Vehicle already dispatched, cannot reschedule"
            value={note}
            onChange={(e) => setNote(e.target.value)}
          />
        </div>
        {error && <div className="fs-error">⚠ {error}</div>}
        <div className="fs-form-actions">
          <button className="btn-primary" type="submit" disabled={!response || busy}>
            {busy ? 'Submitting…' : 'Submit Response'}
          </button>
        </div>
      </form>
    </section>
  );
}

// ─── Traveler view of supplier response ──────────────────────────────────────

function TravelerSupplierResponseView({ notification, booking, onChangeRequestSubmitted, changeRequests }) {
  const [showCRForm, setShowCRForm] = useState(false);
  const [crMode, setCrMode] = useState('pickup');
  const [newPickupTime, setNewPickupTime] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [crError, setCrError] = useState('');

  const supplierResponse = notification?.supplier_response;
  const flightStatus = notification?.flight_status?.status;
  const needsAttention = ['DELAYED', 'EARLY', 'CANCELLED'].includes(flightStatus);
  const hasPendingCR = changeRequests.some((c) => c.status === 'PENDING');

  const handleCRSubmit = async (e) => {
    e.preventDefault();
    if (crMode === 'pickup' && !newPickupTime) return;
    setSubmitting(true);
    setCrError('');
    try {
      const payload = {
        new_flight_id: null,
        new_pickup_time: crMode === 'pickup' ? `${newPickupTime}:00` : null,
        new_flight_id: crMode === 'cancel' ? null : null,
      };
      if (crMode === 'cancel') {
        payload.new_pickup_time = null;
        payload.cancellation_requested = true;
      }
      const result = await submitChangeRequest(booking.id, {
        new_flight_id: null,
        new_pickup_time: crMode === 'pickup' ? `${newPickupTime}:00` : null,
      });
      onChangeRequestSubmitted(result);
      setShowCRForm(false);
    } catch (err) {
      setCrError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  if (!needsAttention) return null;

  return (
    <section className="fs-section">
      <h4>Supplier Update</h4>
      {!supplierResponse ? (
        <InfoBanner type="info">
          Awaiting supplier confirmation on the revised pickup arrangement.
        </InfoBanner>
      ) : supplierResponse.response === 'CONFIRMED_REVISED' ? (
        <div className="fs-response-card fs-response-confirmed_revised">
          <span className="fs-response-label">✅ Supplier confirmed the revised pickup time</span>
          {supplierResponse.note && <span className="fs-response-note">"{supplierResponse.note}"</span>}
          <span className="fs-response-meta">— {supplierResponse.company_name}, {fmt(supplierResponse.responded_at)}</span>
        </div>
      ) : (
        <div className="fs-response-card fs-response-unavailable">
          <span className="fs-response-label">⚠ Supplier reported unavailable for the revised pickup time</span>
          {supplierResponse.note && <span className="fs-response-note">"{supplierResponse.note}"</span>}
          <span className="fs-response-meta">— {supplierResponse.company_name}, {fmt(supplierResponse.responded_at)}</span>
        </div>
      )}

      {booking.status === 'CONFIRMED' && (
        <div className="fs-cr-actions">
          {showCRForm ? (
            <form className="fs-cr-form" onSubmit={handleCRSubmit} noValidate>
              <div className="cr-mode-tabs">
                <button type="button" className={`cr-tab${crMode === 'pickup' ? ' cr-tab-active' : ''}`} onClick={() => setCrMode('pickup')}>
                  Reschedule Pickup
                </button>
                <button type="button" className={`cr-tab${crMode === 'cancel' ? ' cr-tab-active' : ''}`} onClick={() => setCrMode('cancel')}>
                  Request Cancellation
                </button>
              </div>
              {crMode === 'pickup' && (
                <div className="fs-cr-field">
                  <label className="fs-label">New Pickup Date & Time</label>
                  <input
                    className="form-input"
                    type="datetime-local"
                    value={newPickupTime}
                    onChange={(e) => setNewPickupTime(e.target.value)}
                    required
                  />
                </div>
              )}
              {crMode === 'cancel' && (
                <InfoBanner type="warning">
                  A cancellation request will be sent to the supplier for review.
                </InfoBanner>
              )}
              {crError && <div className="fs-error">⚠ {crError}</div>}
              <div className="fs-form-actions">
                <button type="button" className="btn-secondary-active" onClick={() => setShowCRForm(false)}>Cancel</button>
                <button type="submit" className="btn-primary" disabled={submitting || (crMode === 'pickup' && !newPickupTime)}>
                  {submitting ? 'Submitting…' : 'Submit Request'}
                </button>
              </div>
            </form>
          ) : (
            <button
              className="btn-primary"
              onClick={() => setShowCRForm(true)}
              disabled={hasPendingCR}
              title={hasPendingCR ? 'A change request is already pending' : ''}
            >
              {hasPendingCR ? 'Change Request Pending…' : 'Request Rescheduling / Cancellation'}
            </button>
          )}
        </div>
      )}
    </section>
  );
}

// ─── Demo flight status simulator (visible to all for demo purposes) ──────────

function FlightStatusSimulator({ booking, onStatusUpdated }) {
  const [status, setStatus] = useState('ON_TIME');
  const [delayMins, setDelayMins] = useState(30);
  const [busy, setBusy] = useState(false);

  const handleApply = async () => {
    setBusy(true);
    try {
      const flight = booking._flight;
      const base = new Date(flight.arrival_local);
      let estimatedArrival = flight.arrival_local;
      if (status === 'DELAYED') {
        base.setMinutes(base.getMinutes() + Number(delayMins));
        estimatedArrival = base.toISOString().slice(0, 19);
      } else if (status === 'EARLY') {
        base.setMinutes(base.getMinutes() - Number(delayMins));
        estimatedArrival = base.toISOString().slice(0, 19);
      }
      await updateFlightStatus(booking.flight_id, { status, estimated_arrival: estimatedArrival });
      onStatusUpdated();
    } finally {
      setBusy(false);
    }
  };

  return (
    <section className="fs-section fs-simulator">
      <h4>✈ Flight Status Simulator <span className="fs-demo-tag">Demo</span></h4>
      <p className="fs-sim-hint">Simulate a flight status change to see how the page responds.</p>
      <div className="fs-sim-row">
        <select className="form-input fs-sim-select" value={status} onChange={(e) => setStatus(e.target.value)}>
          {Object.entries(STATUS_META).map(([val, { label }]) => (
            <option key={val} value={val}>{label}</option>
          ))}
        </select>
        {(status === 'DELAYED' || status === 'EARLY') && (
          <label className="fs-sim-mins">
            <span className="fs-label">Minutes</span>
            <input
              className="form-input fs-sim-input"
              type="number"
              min="5"
              max="300"
              value={delayMins}
              onChange={(e) => setDelayMins(e.target.value)}
            />
          </label>
        )}
        <button className="btn-primary" onClick={handleApply} disabled={busy}>
          {busy ? 'Applying…' : 'Apply'}
        </button>
      </div>
    </section>
  );
}

// ─── Change request history ───────────────────────────────────────────────────

function ChangeRequestHistory({ changeRequests }) {
  if (!changeRequests?.length) return null;
  return (
    <section className="fs-section">
      <h4>Change Request History</h4>
      <div className="fs-cr-history">
        {changeRequests.map((cr) => (
          <div key={cr.id} className={`fs-cr-item fs-cr-${cr.status.toLowerCase()}`}>
            <div className="fs-cr-top">
              <span className="fs-cr-type">
                {cr.new_pickup_time ? `🕐 New Pickup: ${fmt(cr.new_pickup_time)}` : ''}
                {cr.new_flight_id ? `✈ New Flight: ${cr.new_flight_id}` : ''}
                {!cr.new_pickup_time && !cr.new_flight_id ? 'Cancellation Request' : ''}
              </span>
              <span className={`fs-cr-badge fs-cr-badge-${cr.status.toLowerCase()}`}>{cr.status}</span>
            </div>
            {cr.rejection_reason && <div className="fs-cr-reason">Reason: {cr.rejection_reason}</div>}
            <div className="fs-cr-meta">Submitted {fmt(cr.created_at)}{cr.resolved_at ? ` · Resolved ${fmt(cr.resolved_at)}` : ''}</div>
          </div>
        ))}
      </div>
    </section>
  );
}

// ─── Main page ────────────────────────────────────────────────────────────────

export default function FlightStatusPage({ role, company }) {
  const [input, setInput] = useState('');
  const [booking, setBooking] = useState(null);
  const [notification, setNotification] = useState(null);
  const [changeRequests, setChangeRequests] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const isValid = /^\d{10}$/.test(input.trim());
  const isSupplier = role === 'supplier';

  const loadNotification = useCallback(async (bk) => {
    try {
      const n = await getFlightStatusNotification(bk.id);
      setNotification(n);
    } catch (_) {}
  }, []);

  const loadChangeRequests = useCallback(async (bk) => {
    try {
      const crs = await getChangeRequests(bk.id);
      setChangeRequests(crs);
    } catch (_) {}
  }, []);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!isValid) return;
    setLoading(true);
    setError('');
    setBooking(null);
    setNotification(null);
    setChangeRequests([]);
    try {
      const bk = await getBookingByNumber(input.trim());
      setBooking(bk);
      await Promise.all([loadNotification(bk), loadChangeRequests(bk)]);
    } catch (err) {
      setError(err.status === 404
        ? 'Booking not found. Please check your booking number.'
        : err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleStatusUpdated = async () => {
    if (booking) await loadNotification(booking);
  };

  const handleSupplierResponded = async (resp) => {
    setNotification((prev) => ({ ...prev, supplier_response: resp }));
    if (booking) await loadNotification(booking);
  };

  const handleCRSubmitted = (cr) => {
    setChangeRequests((prev) => [...prev, cr]);
  };

  const flightStatus = notification?.flight_status;
  const suggestedPickup = notification?.suggested_pickup;
  const flight = booking?._flight;
  const hotel = booking?._hotel;

  const delayMins = flightStatus && flightStatus.estimated_arrival !== flightStatus.scheduled_arrival
    ? Math.round((new Date(flightStatus.estimated_arrival) - new Date(flightStatus.scheduled_arrival)) / 60000)
    : 0;

  return (
    <main className="fs-main">
      <div className="fs-header">
        <h2>Flight Status & Pickup Updates</h2>
        <p>Enter your 10-digit booking number to view live flight status and pickup arrangements.</p>
      </div>

      <form className="fs-search-form" onSubmit={handleSearch} noValidate>
        <input
          className={`form-input fs-search-input${input && !isValid ? ' input-error' : ''}`}
          type="text"
          placeholder="10-digit booking number"
          value={input}
          maxLength={10}
          onChange={(e) => { setInput(e.target.value.replace(/\D/g, '')); setBooking(null); setError(''); }}
        />
        {input && !isValid && <div className="fs-error">Booking number must be exactly 10 digits.</div>}
        <button className="btn-primary" type="submit" disabled={!isValid || loading}>
          {loading ? 'Loading…' : 'View Status'}
        </button>
      </form>

      {error && <div className="fs-error-box">⚠ {error}</div>}

      {booking && (
        <div className="fs-results">

          {/* ── Flight status card ── */}
          <div className="fs-flight-card">
            <div className="fs-flight-header">
              <div className="fs-flight-identity">
                <span className="fs-flight-number">{flight?.flight_number ?? booking.flight_id}</span>
                <span className="fs-airline">{flight?.airline}</span>
              </div>
              {flightStatus && <FlightStatusBadge status={flightStatus.status} />}
            </div>

            <div className="fs-flight-route">
              <span className="fs-airport">{flight?.from ?? '—'}</span>
              <span className="fs-route-arrow">✈</span>
              <span className="fs-airport">{flight?.to ?? booking._flight?.to ?? '—'}</span>
            </div>

            <div className="fs-times-grid">
              <Field label="Scheduled Arrival" value={fmt(flightStatus?.scheduled_arrival ?? flight?.arrival_local)} />
              <Field
                label="Estimated Arrival"
                value={fmt(flightStatus?.estimated_arrival ?? flight?.arrival_local)}
                highlight={flightStatus?.status === 'DELAYED' || flightStatus?.status === 'EARLY'}
              />
              {delayMins !== 0 && (
                <Field
                  label={delayMins > 0 ? 'Delay' : 'Early by'}
                  value={`${Math.abs(delayMins)} min`}
                  highlight
                />
              )}
              <Field label="Last Updated" value={fmt(flightStatus?.updated_at)} />
            </div>

            {flightStatus?.status === 'DELAYED' && (
              <InfoBanner type="warning">
                ⏰ This flight is delayed by {delayMins} minutes. The suggested pickup time has been updated.
              </InfoBanner>
            )}
            {flightStatus?.status === 'EARLY' && (
              <InfoBanner type="info">
                ⚡ This flight is arriving {Math.abs(delayMins)} minutes early. Please be ready sooner.
              </InfoBanner>
            )}
            {flightStatus?.status === 'CANCELLED' && (
              <InfoBanner type="error">
                ❌ This flight has been cancelled. Please contact your airline and consider rescheduling.
              </InfoBanner>
            )}
            {flightStatus?.status === 'LANDED' && (
              <InfoBanner type="success">
                🛬 Flight has landed. Your taxi should be waiting at the arrival area.
              </InfoBanner>
            )}
          </div>

          {/* ── Booking & pickup summary ── */}
          <div className="fs-booking-card">
            <div className="fs-booking-header">
              <div>
                <span className="fs-booking-label">Booking</span>
                <span className="fs-booking-number">{booking.booking_number}</span>
              </div>
              <span className={`fs-status-pill fs-status-${booking.status?.toLowerCase()}`}>{booking.status}</span>
            </div>

            <section className="fs-section">
              <h4>Journey</h4>
              <div className="fs-grid">
                <Field label="Traveler" value={booking._traveler_name} />
                <Field label="Arrival Airport" value={flight?.to} />
                <Field label="Hotel" value={hotel?.name ?? booking.hotel_id} />
                <Field label="Destination" value={hotel ? `${hotel.city}, ${hotel.area}` : '—'} />
                <Field label="Supplier" value={booking.company_name} />
                <Field label="Vehicle" value={booking._vehicle_type} />
              </div>
            </section>

            <section className="fs-section">
              <h4>Pickup Arrangement</h4>
              <div className="fs-grid">
                <Field label="Confirmed Pickup" value={fmt(booking.pickup_time)} />
                {suggestedPickup && suggestedPickup !== booking.pickup_time && (
                  <Field label="Suggested Revised Pickup" value={fmt(suggestedPickup)} highlight />
                )}
              </div>
              {suggestedPickup && suggestedPickup !== booking.pickup_time && (
                <InfoBanner type="warning">
                  Based on the updated flight ETA, the suggested pickup time is <strong>{fmt(suggestedPickup)}</strong>.
                </InfoBanner>
              )}
            </section>

            {/* Supplier panel */}
            {isSupplier && notification && (
              <SupplierResponsePanel
                booking={booking}
                notification={notification}
                onResponded={handleSupplierResponded}
              />
            )}

            {/* Traveler panel */}
            {!isSupplier && notification && (
              <TravelerSupplierResponseView
                notification={notification}
                booking={booking}
                onChangeRequestSubmitted={handleCRSubmitted}
                changeRequests={changeRequests}
              />
            )}

            <ChangeRequestHistory changeRequests={changeRequests} />
          </div>

          {/* ── Demo simulator ── */}
          {booking && (
            <FlightStatusSimulator booking={booking} onStatusUpdated={handleStatusUpdated} />
          )}
        </div>
      )}
    </main>
  );
}
