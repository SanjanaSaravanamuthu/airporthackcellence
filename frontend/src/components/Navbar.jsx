import { Link, useNavigate } from 'react-router-dom';
import './Navbar.css';

export default function Navbar({ role, onSwitchRole }) {
  const navigate = useNavigate();

  const handleSwitch = () => {
    onSwitchRole();
    navigate('/');
  };

  return (
    <nav className="navbar">
      <Link to="/" className="navbar-brand">
        ✈ Airport Taxi Ride Offers
      </Link>
      <div className="navbar-actions">
        <Link to="/" className="navbar-link">Home</Link>
        <Link to="/booking" className="navbar-link">Look Up Booking</Link>
        <Link to="/track" className="navbar-link">Track My Ride</Link>
        <Link to="/flight-status" className="navbar-link">Flight Status</Link>
        {role && (
          <>
            <span className="navbar-role">
              {role === 'traveler' ? '🧳 Traveler' : '🚖 Supplier'}
            </span>
            <button className="navbar-switch" onClick={handleSwitch}>
              Switch Role
            </button>
          </>
        )}
      </div>
    </nav>
  );
}
