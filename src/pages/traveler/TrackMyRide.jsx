import { useState } from 'react';
import { getRequest, getOffers, bookOffer, declineOffer } from '../../api/traveler';
import './TrackMyRide.css';

function formatDateTime(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' });
}

function Field({ label, value }) {
  return (
    <div className="tr-field">
      <span className="tr-label">{label}</span>
      <span className="tr-value">{value ?? '—'}</span>
    </div>
  );
}

function StatusBadge({ status }) {
  return (
    <span className={`tr-badge tr-badge-${status?.toLowerCase()}`}>{status}</span>
  );
}

function ConfirmDialog({ message, confirmLabel, confirmClass, onConfirm, onCancel, busy }) {
  return (
    <div className="tr-dialog-overlay">
      <div className="tr-dialog">
        <p className="tr-dialog-msg">{message}</p>
        <div className="tr-dialog-actions">
          <button className="btn-secondary-active" onClick={onCancel} disabled={busy}>Cancel</button>
          <button className={confirmClass} onClick={onConfirm} disabled={busy}>
            {busy ? 'Please wait…' : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}

function OfferCard({ offer, isSettled, onAccept, onDecline, actionBusy }) {
  const [dialog, setDialog] = useState(null); // 'accept' | 'decline' | null
  const isOpen = offer.status === 'OPEN';
  const busy = actionBusy === offer.id;

  const handleConfirm = async () => {
    if (dialog === 'accept') await onAccept(offer);
    else await onDecline(offer);
    setDialog(null);
  };

  return (
    <div className={`tr-offer-card tr-offer-${offer.status?.toLowerCase()}`}>
      {dialog && (
        <ConfirmDialog
          message={
            dialog === 'accept'
              ? `Accept the offer from ${offer.company_name} for ₹${Number(offer.offered_price_inr).toLocaleString('en-IN')}?`
              : `Decline the offer from ${offer.company_name}?`
          }
          confirmLabel={dialog === 'accept' ? 'Yes, Accept' : 'Yes, Decline'}
          confirmClass={dialog === 'accept' ? 'btn-primary' : 'btn-danger'}
          onConfirm={handleConfirm}
          onCancel={() => setDialog(null)}
          busy={busy}
        />
      )}

      <div className="tr-offer-header">
        <span className="tr-offer-company">{offer.company_name}</span>
        <StatusBadge status={offer.status} />
      </div>

      <div className="tr-offer-grid">
        <Field label="Vehicle Type" value={offer.vehicle_type} />
        <Field label="Seats" value={offer.seats} />
        <Field label="Large Bags" value={offer.large_bags} />
        <Field label="Small Bags" value={offer.small_bags} />
        <Field label="Offered Price" value={`₹${Number(offer.offered_price_inr).toLocaleString('en-IN')}`} />
        <Field label="Submitted" value={formatDateTime(offer.created_at)} />
      </div>

      {isOpen && !isSettled && (
        <div className="tr-offer-actions">
          <button
            className="btn-danger"
            onClick={() => setDialog('decline')}
            disabled={busy}
          >
            Decline
          </button>
          <button
            className="btn-primary"
            onClick={() => setDialog('accept')}
            disabled={busy}
          >
            {busy && dialog === 'accept' ? 'Accepting…' : 'Accept'}
          </button>
        </div>
      )}
    </div>
  );
}

export default function TrackMyRide() {
  const [input, setInput] = useState('');
  const [request, setRequest] = useState(null);
  const [offers, setOffers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [actionBusy, setActionBusy] = useState(null); // offerId being actioned
  const [actionError, setActionError] = useState('');
  const [confirmedBooking, setConfirmedBooking] = useState(null);

  const isValid = input.trim().length > 0;
  const isSettled = offers.some((o) => o.status === 'BOOKED');

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!isValid) return;
    setLoading(true);
    setError('');
    setRequest(null);
    setOffers([]);
    setConfirmedBooking(null);
    setActionError('');
    try {
      const [req, offerList] = await Promise.all([
        getRequest(input.trim()),
        getOffers(input.trim()),
      ]);
      setRequest(req);
      setOffers(offerList);
    } catch (err) {
      setError(err.status === 404
        ? 'No ride request found for this ID. Please check and try again.'
        : err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleAccept = async (offer) => {
    setActionBusy(offer.id);
    setActionError('');
    try {
      const booking = await bookOffer(offer.id);
      setConfirmedBooking(booking);
      // Reflect status changes returned by mock: BOOKED + NOT_CHOSEN for others
      setOffers((prev) =>
        prev.map((o) =>
          o.id === offer.id
            ? { ...o, status: 'BOOKED' }
            : o.status === 'OPEN'
            ? { ...o, status: 'NOT_CHOSEN' }
            : o
        )
      );
      setRequest((prev) => ({ ...prev, status: 'FULFILLED' }));
    } catch (err) {
      setActionError(err.message);
    } finally {
      setActionBusy(null);
    }
  };

  const handleDecline = async (offer) => {
    setActionBusy(offer.id);
    setActionError('');
    try {
      await declineOffer(offer.id);
      setOffers((prev) =>
        prev.map((o) => (o.id === offer.id ? { ...o, status: 'DECLINED' } : o))
      );
    } catch (err) {
      setActionError(err.message);
    } finally {
      setActionBusy(null);
    }
  };

  return (
    <main className="tr-main">
      <div className="tr-header">
        <h2>Track My Ride</h2>
        <p>Enter your Ride Request ID to view your request details and supplier offers.</p>
      </div>

      <form className="tr-form" onSubmit={handleSearch} noValidate>
        <input
          className="form-input tr-input"
          type="text"
          placeholder="e.g. REQ-0001"
          value={input}
          onChange={(e) => { setInput(e.target.value); setRequest(null); setError(''); setConfirmedBooking(null); }}
        />
        <button className="btn-primary" type="submit" disabled={!isValid || loading}>
          {loading ? 'Searching…' : 'View My Ride'}
        </button>
      </form>

      {error && <div className="tr-error">⚠ {error}</div>}

      {confirmedBooking && (
        <div className="tr-confirmed">
          <span className="tr-confirmed-icon">✅</span>
          <div>
            <strong>Booking Confirmed!</strong>
            <div className="tr-confirmed-sub">
              Your booking number is{' '}
              <span className="tr-confirmed-number">{confirmedBooking.booking_number}</span>.
              Save this number to look up your booking anytime.
            </div>
          </div>
        </div>
      )}

      {actionError && <div className="tr-error">⚠ {actionError}</div>}

      {request && (
        <div className="tr-results">
          <div className="tr-request-card">
            <div className="tr-request-header">
              <div className="tr-request-id-block">
                <span className="tr-id-label">Request ID</span>
                <span className="tr-id-value">{request.request_id}</span>
              </div>
              <StatusBadge status={request.status} />
            </div>

            <section className="tr-section">
              <h4>Journey Details</h4>
              <div className="tr-grid">
                <Field label="Traveler" value={request.traveler_name} />
                <Field label="Flight ID" value={request.flight_id} />
                <Field label="Hotel ID" value={request.hotel_id} />
                <Field label="Pickup Time" value={formatDateTime(request.pickup_time)} />
                <Field label="Adults" value={request.adults} />
                <Field label="Children" value={request.child_ages?.length > 0 ? `${request.child_ages.length} (ages: ${request.child_ages.join(', ')})` : 'None'} />
                <Field label="Large Bags" value={request.large_bags} />
                <Field label="Small Bags" value={request.small_bags} />
                <Field label="Car Preference" value={request.car_type || 'Any'} />
                <Field label="Expected Price" value={`₹${Number(request.price_inr).toLocaleString('en-IN')}`} />
                <Field label="Submitted" value={formatDateTime(request.created_at)} />
              </div>
            </section>
          </div>

          <section className="tr-offers-section">
            <h3>Supplier Offers</h3>
            {offers.length === 0 ? (
              <div className="tr-empty">No offers received yet. Please check again later.</div>
            ) : (
              <div className="tr-offers-list">
                {offers.map((offer) => (
                  <OfferCard
                    key={offer.id}
                    offer={offer}
                    isSettled={isSettled}
                    onAccept={handleAccept}
                    onDecline={handleDecline}
                    actionBusy={actionBusy}
                  />
                ))}
              </div>
            )}
          </section>
        </div>
      )}
    </main>
  );
}
