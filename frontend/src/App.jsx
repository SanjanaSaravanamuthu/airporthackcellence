import { useState } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import LandingPage from './pages/LandingPage';
import TravelerDashboard from './pages/traveler/TravelerDashboard';
import TrackMyRide from './pages/traveler/TrackMyRide';
import BookingLookup from './pages/traveler/BookingLookup';
import SupplierSelection from './pages/supplier/SupplierSelection';
import SupplierDashboard from './pages/supplier/SupplierDashboard';
import FlightStatusPage from './pages/FlightStatusPage';

export default function App() {
  const [role, setRole] = useState(null);
  const [company, setCompany] = useState(null);

  const handleSwitchRole = () => {
    setRole(null);
    setCompany(null);
  };

  return (
    <>
      <Navbar role={role} onSwitchRole={handleSwitchRole} />
      <Routes>
        <Route path="/" element={<LandingPage onSelectRole={setRole} />} />
        <Route path="/booking" element={<BookingLookup />} />
        <Route path="/track" element={<TrackMyRide />} />
        <Route path="/flight-status" element={<FlightStatusPage role={role} company={company} />} />
        <Route
          path="/traveler"
          element={role === 'traveler' ? <TravelerDashboard /> : <Navigate to="/" replace />}
        />
        <Route
          path="/supplier"
          element={role === 'supplier' ? <SupplierSelection onSelectCompany={setCompany} /> : <Navigate to="/" replace />}
        />
        <Route
          path="/supplier/dashboard"
          element={role === 'supplier' && company ? <SupplierDashboard company={company} /> : <Navigate to="/" replace />}
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </>
  );
}
