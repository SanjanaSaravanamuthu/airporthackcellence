import { useState } from 'react';
import { getBookingByNumber } from '../../api/bookings';
import BookingSummary from './BookingSummary';
import './BookingLookup.css';

export default function BookingLookup() {
  const [input, setInput] = useState('');
  const [booking, setBooking] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const isValidFormat = /^\d{10}$/.test(input.trim());

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!isValidFormat) return;
    setLoading(true);
    setError('');
    setBooking(null);
    try {
      const result = await getBookingByNumber(input.trim());
      setBooking(result);
    } catch (err) {
      if (err.status === 404) {
        setError('Booking not found. Please check your booking number.');
      } else {
        setError(err.message);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="lookup-main">
      <div className="lookup-header">
        <h2>Look Up Your Booking</h2>
        <p>Enter your 10-digit booking number to view your booking details.</p>
      </div>

      <form className="lookup-form" onSubmit={handleSearch} noValidate>
        <input
          className={`form-input lookup-input${input && !isValidFormat ? ' input-error' : ''}`}
          type="text"
          placeholder="e.g. 3829104756"
          value={input}
          maxLength={10}
          onChange={(e) => { setInput(e.target.value.replace(/\D/g, '')); setBooking(null); setError(''); }}
        />
        {input && !isValidFormat && (
          <div className="form-error">Booking number must be exactly 10 digits.</div>
        )}
        <button className="btn-primary" type="submit" disabled={!isValidFormat || loading}>
          {loading ? 'Searching…' : 'Search Booking'}
        </button>
      </form>

      {error && <div className="lookup-error">{error}</div>}
      {booking && <BookingSummary booking={booking} allowChangeRequest />}
    </main>
  );
}
