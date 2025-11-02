import React, { useState, useEffect } from 'react';
import { bookingService } from '../services/api';

const StaffDashboard = () => {
    const [bookings, setBookings] = useState([]);
    const [loading, setLoading] = useState(false);
    const [filter, setFilter] = useState('');

    useEffect(() => {
        loadBookings();
    }, []);

    const loadBookings = async () => {
        setLoading(true);
        try {
            const response = await bookingService.getAllBookings();
            setBookings(response.data);
        } catch (error) {
            console.error('Error loading bookings:', error);
        }
        setLoading(false);
    };

    const updateStatus = async (token, newStatus) => {
        try {
            await bookingService.updateBookingStatus(token, newStatus);
            loadBookings(); // Reload to get updated data
        } catch (error) {
            alert('Error updating status');
        }
    };

    const filteredBookings = bookings.filter(booking =>
        booking.municipality?.toLowerCase().includes(filter.toLowerCase()) ||
        booking.status?.toLowerCase().includes(filter.toLowerCase())
    );

    return (
        <div className="page">
            <h2>Staff Dashboard</h2>

            <div className="dashboard-controls">
                <input
                    type="text"
                    placeholder="Filter by municipality or status..."
                    value={filter}
                    onChange={(e) => setFilter(e.target.value)}
                    className="filter-input"
                />
                <button onClick={loadBookings} disabled={loading}>
                    {loading ? 'Refreshing...' : 'Refresh'}
                </button>
            </div>

            <div className="bookings-grid">
                {filteredBookings.map(booking => (
                    <div key={booking.token} className="booking-card">
                        <div className="card-header">
                            <strong>Token: {booking.token}</strong>
                            <span
                                data-testid={`current-status-${booking.token}`}
                                className={`status status-${booking.status?.toLowerCase()}`}
                            >
                                {booking.status}
                            </span>
                        </div>

                        <div className="card-body">
                            <p><strong>Municipality:</strong> {booking.municipality}</p>
                            <p><strong>Date:</strong> {booking.date}</p>
                            <p><strong>Items:</strong> {booking.description}</p>

                            {booking.history && (
                                <div className="status-history">
                                    <strong>History:</strong>
                                    <ul>
                                        {booking.history.slice(-3).map((entry, idx) => (
                                            <li key={`${entry.status}-${entry.ts}`}>
                                                <span className={`status-badge status-${entry.status.toLowerCase()}`}>
                                                    {entry.status.replace('_', ' ')}
                                                </span>
                                                {' on '}
                                                {new Date(entry.ts).toLocaleString()}
                                            </li>
                                        ))}
                                    </ul>
                                </div>
                            )}
                        </div>

                        <div className="card-actions">
                            <select
                                data-testid={`status-select-${booking.token}`}
                                value={booking.status}
                                onChange={(e) => updateStatus(booking.token, e.target.value)}
                            >
                                <option value="RECEIVED">Received</option>
                                <option value="ASSIGNED">Assigned</option>
                                <option value="IN_PROGRESS">In Progress</option>
                                <option value="COMPLETED">Completed</option>
                                <option value="CANCELLED">Cancelled</option>
                            </select>
                        </div>
                    </div>
                ))}
            </div>

            {filteredBookings.length === 0 && !loading && (
                <p>No bookings found.</p>
            )}
        </div>
    );
};

export default StaffDashboard;