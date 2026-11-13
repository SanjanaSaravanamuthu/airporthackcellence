import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getCompanies } from '../../api/supplier';
import './SupplierSelection.css';

export default function SupplierSelection({ onSelectCompany }) {
  const [companies, setCompanies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    getCompanies()
      .then(setCompanies)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  const handleSelect = (company) => {
    onSelectCompany(company);
    navigate('/supplier/dashboard');
  };

  return (
    <main className="supplier-selection">
      <div className="supplier-selection-header">
        <h2>Select Your Taxi Company</h2>
        <p>Choose your company to view eligible ride requests.</p>
      </div>

      {loading && <div className="ss-status">Loading companies…</div>}
      {error && <div className="ss-status ss-error">⚠ Backend unavailable — start the Spring Boot server on port 8080.</div>}

      {!loading && !error && companies.length === 0 && (
        <div className="ss-status">No taxi companies found.</div>
      )}

      <div className="company-grid">
        {companies.map((c) => (
          <div key={c.company_id} className="company-card">
            <div className="company-card-header">
              <span className="company-name">{c.name}</span>
              <span className="company-rating">★ {c.rating}</span>
            </div>
            <div className="company-meta">
              <span>✈ Airports: {c.airports.join(', ')}</span>
              <span>📍 Areas: {c.areas.map((a) => `${a.city} – ${a.area}`).join(', ')}</span>
              <span>🕐 Hours: {c.service_from} – {c.service_until}</span>
              <span>🚗 Fleet: {c.vehicles.length} vehicle{c.vehicles.length !== 1 ? 's' : ''}</span>
            </div>
            <button className="btn-primary" onClick={() => handleSelect(c)}>
              Continue as {c.name}
            </button>
          </div>
        ))}
      </div>
    </main>
  );
}
