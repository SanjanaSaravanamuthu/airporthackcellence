import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import RideRequestForm from './RideRequestForm';
import BookingSummary from './BookingSummary';
import { getOffers, bookOffer } from '../../api/traveler';
import { enrichBooking } from '../../api/enrichBooking';
import './TravelerDashboard.css';

function formatDateTime(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' });
}

function OffersPanel({ request, onBooked }) {
  const [offers, setOffers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [bookingError, setBookingError] = useState('');
  const [bookingId, setBookingId] = useState(null);

  useEffect(() => {
    getOffers(request.request_id)
      .then(setOffers)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [request.request_id]);

  const handleBook = async (offerId) => {
    setBookingError('');
    setBookingId(offerId);
    try {
      const raw = await bookOffer(offerId);
      onBooked(await enrichBooking(raw));
    } catch (e) {
      setBookingError(e.message);
      setBookingId(null);
    }
  };

  return (
    <div className="offers-panel">
      <h3>Taxi Offers for {request.request_id}</h3>
      {loading && <div className="offers-status">Loading offers…</div>}
      {error && <div className="offers-status offers-error">⚠ {error}</div>}
      {!loading && !error && offers.length === 0 && (
        <div className="offers-status">No offers yet. Suppliers will respond shortly.</div>
      )}
      {bookingError && <div className="offers-status offers-error">⚠ {bookingError}</div>}
      {offers.map((offer) => (
        <div key={offer.id} className={`offer-card offer-${offer.status.toLowerCase()}`}>
          <div className="offer-top">
            <span className="offer-company">{offer.company_name}</span>
            <span className={`offer-status offer-status-${offer.status.toLowerCase()}`}>{offer.status}</span>
          </div>
          <div className="offer-details">
            <span>🚗 {offer.vehicle_type} · {offer.vehicle_id}</span>
            <span>💺 {offer.seats} seats · 🧳 {offer.large_bags} large · 👜 {offer.small_bags} small</span>
            <span className="offer-price">₹{Number(offer.offered_price_inr).toLocaleString('en-IN')}</span>
          </div>
          {offer.status === 'OPEN' && (
            <button
              className="btn-primary offer-book-btn"
              disabled={bookingId !== null}
              onClick={() => handleBook(offer.id)}
            >
              {bookingId === offer.id ? 'Booking…' : 'Book This Offer'}
            </button>
          )}
        </div>
      ))}
    </div>
  );
}

export default function TravelerDashboard() {
  const [submitted, setSubmitted] = useState(null);
  const [booking, setBooking] = useState(null);
  const navigate = useNavigate();

  if (booking) {
    return (
      <main className="dashboard-main">
        <div className="booking-success-header">
          <div className="success-icon">🎉</div>
          <h2>Booking Confirmed!</h2>
          <p>Save your booking number to look up your booking anytime.</p>
        </div>
        <BookingSummary booking={booking} allowChangeRequest />
        <div className="booking-actions">
          <button className="btn-primary" onClick={() => navigate('/booking')}>
            Look Up Booking Later
          </button>
          <button className="btn-secondary-active" onClick={() => { setSubmitted(null); setBooking(null); }}>
            New Request
          </button>
        </div>
      </main>
    );
  }

  if (submitted) {
    return (
      <main className="dashboard-main">
        <div className="success-card">
          <div className="success-icon">✅</div>
          <h2>Ride Request Submitted!</h2>
          <p>Your request has been received. Taxi suppliers will review it and send you offers.</p>
          <div className="success-ref">
            <span className="success-ref-label">Request Reference</span>
            <span className="success-ref-id">{submitted.request_id}</span>
          </div>
          <div className="success-details">
            <div><span>Flight</span><strong>{submitted.flight_id}</strong></div>
            <div><span>Hotel</span><strong>{submitted.hotel_id}</strong></div>
            <div><span>Pickup</span><strong>{submitted.pickup_time?.replace('T', ' ')}</strong></div>
            <div><span>Price</span><strong>₹{Number(submitted.price_inr).toLocaleString('en-IN')}</strong></div>
            <div><span>Status</span><strong>{submitted.status}</strong></div>
          </div>
        </div>
        <OffersPanel request={submitted} onBooked={setBooking} />
        <button className="btn-secondary-active new-request-btn" onClick={() => setSubmitted(null)}>
          ← Submit Another Request
        </button>
      </main>
    );
  }

  return (
    <main className="dashboard-main">
      <RideRequestForm onSuccess={setSubmitted} />
    </main>
  );
}
