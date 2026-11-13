import { useNavigate } from 'react-router-dom';
import './LandingPage.css';

export default function LandingPage({ onSelectRole }) {
  const navigate = useNavigate();

  const handleSelect = (role) => {
    onSelectRole(role);
    navigate(role === 'traveler' ? '/traveler' : '/supplier');
  };

  return (
    <main className="landing">
      <div className="landing-hero">
        <h1 className="landing-title">Airport Taxi Ride Offers</h1>
        <p className="landing-subtitle">
          Find reliable airport transfers at a price that works for you.
        </p>
      </div>

      <div className="role-cards">
        <div className="role-card">
          <div className="role-card-icon">🧳</div>
          <h2>Traveler</h2>
          <p>Find and book an airport taxi for your journey.</p>
          <button className="btn-primary" onClick={() => handleSelect('traveler')}>
            Continue as Traveler
          </button>
        </div>

        <div className="role-card">
          <div className="role-card-icon">🚖</div>
          <h2>Taxi Supplier</h2>
          <p>Manage ride requests and offer available vehicles.</p>
          <button className="btn-primary" onClick={() => handleSelect('supplier')}>
            Continue as Supplier
          </button>
        </div>
      </div>
    </main>
  );
}
