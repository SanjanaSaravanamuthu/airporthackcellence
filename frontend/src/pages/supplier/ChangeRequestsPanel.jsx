import { useState, useEffect } from 'react';
import { getSupplierChangeRequests, resolveChangeRequest } from '../../api/supplier';
import './ChangeRequestsPanel.css';

function formatDateTime(iso) {
  if (!iso) return '—';
  return new Date(iso).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' });
}

export default function ChangeRequestsPanel({ company }) {
  const [changeRequests, setChangeRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [rejectionReasons, setRejectionReasons] = useState({});
  const [processing, setProcessing] = useState(null);
  const [actionError, setActionError] = useState('');

  const fetchChangeRequests = () => {
    setLoading(true);
    setError('');
    getSupplierChangeRequests(company.company_id)
      .then(setChangeRequests)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchChangeRequests(); }, [company]);

  const handleResolve = async (crId, status) => {
    setProcessing(crId);
    setActionError('');
    try {
      const payload = {
        status,
        rejection_reason: status === 'REJECTED' ? (rejectionReasons[crId] || null) : null,
      };
      const updated = await resolveChangeRequest(crId, payload);
      setChangeRequests((prev) => prev.map((c) => c.id === crId ? updated : c));
    } catch (err) {
      setActionError(err.message);
    } finally {
      setProcessing(null);
    }
  };

  const pendingCount = changeRequests.filter((c) => c.status === 'PENDING').length;

  return (
    <div className="crp-section">
      <div className="crp-header">
        <h3>Change Requests</h3>
        {pendingCount > 0 && <span className="crp-badge">{pendingCount} pending</span>}
      </div>

      {loading && <div className="crp-status">Loading change requests…</div>}
      {error && <div className="crp-status crp-error">⚠ {error}</div>}
      {actionError && <div className="crp-status crp-error">⚠ {actionError}</div>}

      {!loading && !error && changeRequests.length === 0 && (
        <div className="crp-status">No change requests at this time.</div>
      )}

      {changeRequests.map((cr) => (
        <div key={cr.id} className={`crp-card crp-card-${cr.status.toLowerCase()}`}>
          <div className="crp-card-top">
            <span className="crp-booking-number">Booking #{cr.booking_number}</span>
            <span className={`crp-status-badge crp-status-${cr.status.toLowerCase()}`}>{cr.status}</span>
          </div>

          <div className="crp-details">
            {cr.new_flight_id && (
              <div className="crp-detail">
                <span className="crp-detail-label">New Flight</span>
                <span className="crp-detail-value">{cr.new_flight_id}</span>
              </div>
            )}
            {cr.new_pickup_time && (
              <div className="crp-detail">
                <span className="crp-detail-label">New Pickup Time</span>
                <span className="crp-detail-value">{formatDateTime(cr.new_pickup_time)}</span>
              </div>
            )}
            <div className="crp-detail">
              <span className="crp-detail-label">Submitted</span>
              <span className="crp-detail-value">{formatDateTime(cr.created_at)}</span>
            </div>
            {cr.resolved_at && (
              <div className="crp-detail">
                <span className="crp-detail-label">Resolved</span>
                <span className="crp-detail-value">{formatDateTime(cr.resolved_at)}</span>
              </div>
            )}
            {cr.rejection_reason && (
              <div className="crp-detail">
                <span className="crp-detail-label">Rejection Reason</span>
                <span className="crp-detail-value crp-rejection">{cr.rejection_reason}</span>
              </div>
            )}
          </div>

          {cr.status === 'PENDING' && (
            <div className="crp-actions">
              <input
                className="crp-reason-input"
                type="text"
                placeholder="Rejection reason (optional)"
                value={rejectionReasons[cr.id] || ''}
                onChange={(e) => setRejectionReasons((prev) => ({ ...prev, [cr.id]: e.target.value }))}
              />
              <div className="crp-buttons">
                <button
                  className="btn-primary crp-accept-btn"
                  disabled={processing === cr.id}
                  onClick={() => handleResolve(cr.id, 'ACCEPTED')}
                >
                  {processing === cr.id ? '…' : 'Accept Change'}
                </button>
                <button
                  className="crp-reject-btn"
                  disabled={processing === cr.id}
                  onClick={() => handleResolve(cr.id, 'REJECTED')}
                >
                  {processing === cr.id ? '…' : 'Reject Change'}
                </button>
              </div>
            </div>
          )}
        </div>
      ))}
    </div>
  );
}
