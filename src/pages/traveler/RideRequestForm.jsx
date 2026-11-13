import { useState, useEffect } from 'react';
import { getFlights, getHotels, getRouteLookup } from '../../api/catalog';
import { submitRideRequest } from '../../api/traveler';
import './RideRequestForm.css';

function formatDateTime(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  return d.toLocaleString('en-IN', {
    dateStyle: 'medium',
    timeStyle: 'short',
  });
}

function addMinutes(iso, mins) {
  const d = new Date(iso);
  d.setMinutes(d.getMinutes() + mins);
  const pad = (value) => String(value).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function formatCurrency(value) {
  return new Intl.NumberFormat('en-IN', { maximumFractionDigits: 0 }).format(Number(value || 0));
}

export default function RideRequestForm({ onSuccess }) {
  const [flights, setFlights] = useState([]);
  const [hotels, setHotels] = useState([]);
  const [loadingCatalog, setLoadingCatalog] = useState(true);
  const [catalogError, setCatalogError] = useState('');

  const [flightId, setFlightId] = useState('');
  const [hotelId, setHotelId] = useState('');
  const [adults, setAdults] = useState(1);
  const [children, setChildren] = useState(0);
  const [childAges, setChildAges] = useState([]);
  const [largeBags, setLargeBags] = useState(0);
  const [smallBags, setSmallBags] = useState(0);
  const [pickupTime, setPickupTime] = useState('');
  const [travelerName, setTravelerName] = useState('');
  const [priceInr, setPriceInr] = useState('');

  const [route, setRoute] = useState(null);
  const [routeLoading, setRouteLoading] = useState(false);
  const [routeError, setRouteError] = useState('');

  const [pickupError, setPickupError] = useState('');
  const [priceError, setPriceError] = useState('');

  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState('');

  useEffect(() => {
    Promise.all([getFlights(), getHotels()])
      .then(([f, h]) => {
        setFlights(f);
        setHotels(h);
      })
      .catch((e) => setCatalogError(e.message))
      .finally(() => setLoadingCatalog(false));
  }, []);

  const selectedFlight = flights.find((f) => f.flight_id === flightId);
  const selectedHotel = hotels.find((h) => h.hotel_id === hotelId);

  useEffect(() => {
    if (!selectedFlight || !selectedHotel) {
      setRoute(null);
      setRouteError('');
      setPriceInr('');
      setPriceError('');
      return;
    }

    setRouteLoading(true);
    setRouteError('');
    setPriceInr('');
    setPriceError('');

    getRouteLookup(selectedFlight.to, selectedHotel.city, selectedHotel.area)
      .then((result) => {
        setRoute(result);
      })
      .catch((e) => {
        setRoute(null);
        setRouteError(e.message);
      })
      .finally(() => setRouteLoading(false));
  }, [selectedFlight, selectedHotel]);

  useEffect(() => {
    if (!pickupTime || !selectedFlight) {
      setPickupError('');
      return;
    }

    const landing = new Date(selectedFlight.arrival_local);
    const pickup = new Date(pickupTime);
    const diffMins = (pickup - landing) / 60000;

    if (diffMins < 60) {
      const earliest = addMinutes(selectedFlight.arrival_local, 60);
      setPickupError(
        `Pickup must be at least 60 minutes after the flight lands at ${formatDateTime(selectedFlight.arrival_local)}. Earliest valid option: ${formatDateTime(earliest)}.`
      );
    } else {
      setPickupError('');
    }
  }, [pickupTime, selectedFlight]);

  useEffect(() => {
    if (!priceInr || !route) {
      setPriceError('');
      return;
    }

    const p = Number(priceInr);
    if (p < route.price_min_inr || p > route.price_max_inr) {
      setPriceError(
        `Price must be between ₹${formatCurrency(route.price_min_inr)} and ₹${formatCurrency(route.price_max_inr)}.`
      );
    } else {
      setPriceError('');
    }
  }, [priceInr, route]);

  const handleChildrenChange = (val) => {
    const n = Math.max(0, Number(val) || 0);
    setChildren(n);
    setChildAges((prev) => {
      const next = [...prev];
      while (next.length < n) next.push('');
      return next.slice(0, n);
    });
  };

  const handleChildAge = (index, val) => {
    setChildAges((prev) => {
      const next = [...prev];
      next[index] = val;
      return next;
    });
  };

  const isValidChildAges = () => childAges.every((age) => {
    const num = Number(age);
    return age !== '' && Number.isFinite(num) && num >= 0 && num <= 17;
  });

  const isFormValid = () => {
    if (!travelerName.trim()) return false;
    if (!flightId || !hotelId) return false;
    if (Number(adults) < 1) return false;
    if (Number(children) < 0) return false;
    if (!isValidChildAges()) return false;
    if (Number(largeBags) < 0 || Number(smallBags) < 0) return false;
    if (pickupError) return false;
    if (!route) return false;
    if (!priceInr || priceError) return false;
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!isFormValid() || submitting) return;

    setSubmitting(true);
    setSubmitError('');

    const payload = {
      traveler_name: travelerName.trim(),
      flight_id: flightId,
      hotel_id: hotelId,
      adults: Number(adults),
      child_ages: childAges.map((age) => Number(age)),
      large_bags: Number(largeBags),
      small_bags: Number(smallBags),
      pickup_time: pickupTime ? `${pickupTime}:00` : null,
      price_inr: Number(priceInr),
    };

    try {
      const result = await submitRideRequest(payload);
      onSuccess(result);
    } catch (e) {
      setSubmitError(e.message);
    } finally {
      setSubmitting(false);
    }
  };

  if (loadingCatalog) return <div className="form-status">Loading flight and hotel data…</div>;

  const minPickup = selectedFlight ? addMinutes(selectedFlight.arrival_local, 60) : '';

  return (
    <form className="ride-form" onSubmit={handleSubmit} noValidate>
      <h2 className="form-title">New Ride Request</h2>

      <section className="form-section">
        <h3>Your name</h3>
        <input
          className="form-input"
          type="text"
          placeholder="Full name"
          value={travelerName}
          onChange={(e) => setTravelerName(e.target.value)}
        />
      </section>

      {catalogError && (
        <div className="form-error submit-error">
          ⚠ Backend unavailable: {catalogError}
        </div>
      )}

      <section className="form-section">
        <h3>Flight selection</h3>
        <select
          className="form-input"
          value={flightId}
          onChange={(e) => {
            setFlightId(e.target.value);
            setPickupTime('');
          }}
        >
          <option value="">Select a flight</option>
          {flights.map((flight) => (
            <option key={flight.flight_id} value={flight.flight_id}>
              {flight.flight_number} · {flight.from || 'Departure city unavailable'} → {flight.to} · {new Date(flight.arrival_local).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })} · {new Date(flight.arrival_local).toLocaleTimeString('en-IN', { hour: 'numeric', minute: '2-digit' })}
            </option>
          ))}
        </select>
        {selectedFlight && (
          <div className="form-hint">
            Flight {selectedFlight.flight_number} arrives at <strong>{formatDateTime(selectedFlight.arrival_local)}</strong> at {selectedFlight.to}.
          </div>
        )}
      </section>

      <section className="form-section">
        <h3>Hotel selection</h3>
        <select
          className="form-input"
          value={hotelId}
          onChange={(e) => setHotelId(e.target.value)}
        >
          <option value="">Select a hotel</option>
          {hotels.map((hotel) => (
            <option key={hotel.hotel_id} value={hotel.hotel_id}>
              {hotel.name} · {hotel.city} · {hotel.area}
              {hotel.hotel_id ? ` · ID ${hotel.hotel_id}` : ''}
            </option>
          ))}
        </select>
        {selectedHotel && (
          <div className="form-hint">
            {selectedHotel.name} · {selectedHotel.city} · {selectedHotel.area}
            {selectedHotel.hotel_id ? ` · ID ${selectedHotel.hotel_id}` : ''}
          </div>
        )}
      </section>

      {(flightId && hotelId) && (
        <section className="form-section">
          {routeLoading && <div className="form-hint">Loading suggested route price range…</div>}
          {routeError && <div className="form-error">{routeError}</div>}
          {route && (
            <div className="price-range-card">
              <span className="price-range-label">Suggested price range</span>
              <span className="price-range-route">
                {route.airport || selectedFlight?.to} to {route.city || selectedHotel?.city}
              </span>
              <span className="price-range-value">
                ₹{formatCurrency(route.price_min_inr)} – ₹{formatCurrency(route.price_max_inr)}
              </span>
              <span className="price-range-meta">INR</span>
            </div>
          )}
        </section>
      )}

      <section className="form-section">
        <h3>Passenger details</h3>
        <div className="form-row">
          <label className="form-label">
            Adults
            <input
              className="form-input form-input-sm"
              type="number"
              min="1"
              value={adults}
              onChange={(e) => setAdults(Math.max(1, Number(e.target.value) || 1))}
            />
          </label>
          <label className="form-label">
            Children
            <input
              className="form-input form-input-sm"
              type="number"
              min="0"
              value={children}
              onChange={(e) => handleChildrenChange(e.target.value)}
            />
          </label>
        </div>
        {childAges.length > 0 && (
          <div className="child-ages">
            <span className="form-hint">Enter each child age (0–17):</span>
            <div className="child-ages-row">
              {childAges.map((age, index) => (
                <label key={index} className="form-label">
                  Child {index + 1}
                  <input
                    className="form-input form-input-sm"
                    type="number"
                    min="0"
                    max="17"
                    value={age}
                    onChange={(e) => handleChildAge(index, e.target.value)}
                  />
                </label>
              ))}
            </div>
          </div>
        )}
      </section>

      <section className="form-section">
        <h3>Luggage details</h3>
        <div className="form-row">
          <label className="form-label">
            Large bags
            <input
              className="form-input form-input-sm"
              type="number"
              min="0"
              value={largeBags}
              onChange={(e) => setLargeBags(Math.max(0, Number(e.target.value) || 0))}
            />
          </label>
          <label className="form-label">
            Small bags
            <input
              className="form-input form-input-sm"
              type="number"
              min="0"
              value={smallBags}
              onChange={(e) => setSmallBags(Math.max(0, Number(e.target.value) || 0))}
            />
          </label>
        </div>
      </section>

      <section className="form-section">
        <h3>Pickup time</h3>
        {selectedFlight && (
          <div className="form-hint">
            Selected flight landing time: <strong>{formatDateTime(selectedFlight.arrival_local)}</strong>. Earliest valid pickup: <strong>{formatDateTime(minPickup)}</strong>.
          </div>
        )}
        <input
          className={`form-input${pickupError ? ' input-error' : ''}`}
          type="datetime-local"
          value={pickupTime}
          min={minPickup}
          onChange={(e) => setPickupTime(e.target.value)}
        />
        {pickupError && <div className="form-error">{pickupError}</div>}
        {!pickupTime && selectedFlight && (
          <div className="form-hint">If you leave pickup blank, the system will use the earliest valid time after landing.</div>
        )}
      </section>

      <section className="form-section">
        <h3>Expected price (INR)</h3>
        {route && (
          <div className="form-hint">
            Allowed range: ₹{formatCurrency(route.price_min_inr)} – ₹{formatCurrency(route.price_max_inr)}
          </div>
        )}
        <input
          className={`form-input${priceError ? ' input-error' : ''}`}
          type="number"
          min={route?.price_min_inr ?? 0}
          max={route?.price_max_inr}
          placeholder="Enter expected price in INR"
          value={priceInr}
          onChange={(e) => setPriceInr(e.target.value)}
          disabled={!route}
        />
        {priceError && <div className="form-error">{priceError}</div>}
      </section>

      {submitError && <div className="form-error submit-error">{submitError}</div>}
      <button type="submit" className="btn-primary btn-submit" disabled={!isFormValid() || submitting}>
        {submitting ? 'Submitting ride request…' : 'Submit Ride Request'}
      </button>
    </form>
  );
}
