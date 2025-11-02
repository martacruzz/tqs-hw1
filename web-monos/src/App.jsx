import React from 'react';
import { BrowserRouter as Router, Routes, Route, NavLink } from 'react-router-dom';
import NewBooking from './pages/NewBooking';
import CheckBooking from './pages/CheckBooking';
import StaffDashboard from './pages/StaffDashboard';
import './App.css';

function App() {
  return (
    <Router>
      <div className="app">
        <nav className="navbar">
          <h1>MonosClean - Bulky Waste Collection</h1>
          <div className="nav-links">
            <NavLink to="/" className={({ isActive }) => isActive ? 'active' : ''}>
              New Booking
            </NavLink>
            <NavLink to="/check-booking" className={({ isActive }) => isActive ? 'active' : ''}>
              Check Booking
            </NavLink>
            <NavLink to="/staff" className={({ isActive }) => isActive ? 'active' : ''}>
              Staff Dashboard
            </NavLink>
          </div>
        </nav>

        <main className="main-content">
          <Routes>
            <Route path="/" element={<NewBooking />} />
            <Route path="/check-booking" element={<CheckBooking />} />
            <Route path="/staff" element={<StaffDashboard />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;