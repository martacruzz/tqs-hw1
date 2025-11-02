import React, { useState } from 'react';
import { bookingService } from '../services/api';

const CheckBooking = () => {
    const [token, setToken] = useState('');
    const [booking, setBooking] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleCheckBooking = async (e) => {
        e.preventDefault();
        if (!token.trim()) return;

        setLoading(true);
        setError('');
        try {
            const response = await bookingService.getBooking(token);
            setBooking(response.data);
        } catch (err) {
            setError('Booking not found or invalid token');
            setBooking(null);
        }
        setLoading(false);
    };

    const handleCancelBooking = async () => {
        if (!window.confirm('Are you sure you want to cancel this booking?')) return;

        try {
            await bookingService.cancelBooking(token);
            setBooking({ ...booking, status: 'CANCELLED' });
            alert('Booking cancelled successfully');
        } catch (err) {
            alert('Error cancelling booking');
        }
    };

    return (
        <div className="page">
            <h2>Check Booking Status</h2>

            <form onSubmit={handleCheckBooking} className="check-booking-form">
                <div className="form-group">
                    <label>Booking Token</label>
                    <input
                        name="token"
                        type="text"
                        value={token}
                        onChange={(e) => setToken(e.target.value)}
                        placeholder="Enter your booking token"
                        required
                    />
                </div>
                <button type="submit" disabled={loading}>
                    {loading ? 'Checking...' : 'Check Status'}
                </button>
            </form>

            {error && <div className="error">{error}</div>}

            {booking && (
                <div className="booking-details">
                    <h3>Booking Details</h3>
                    <div className="detail-grid">
                        <div className="detail-item">
                            <strong>Token:</strong> {booking.token}
                        </div>
                        <div className="detail-item">
                            <strong>Status:</strong>
                            <span className={`status status-${booking.status?.toLowerCase()}`}>
                                {booking.status}
                            </span>
                        </div>
                        <div className="detail-item">
                            <strong>Municipality:</strong> {booking.municipality}
                        </div>
                        <div className="detail-item">
                            <strong>Collection Date:</strong> {booking.date}
                        </div>
                        <div className="detail-item">
                            <strong>Description:</strong> {booking.description}
                        </div>
                    </div>

                    {booking.statusHistory && (
                        <div className="status-history">
                            <h4>Status History</h4>
                            <ul>
                                {booking.statusHistory.map((history, index) => (
                                    <li key={index}>
                                        <strong>{history.status}</strong> - {new Date(history.timestamp).toLocaleString()}
                                    </li>
                                ))}
                            </ul>
                        </div>
                    )}

                    {booking.status !== 'CANCELLED' && booking.status !== 'COMPLETED' && (
                        <button onClick={handleCancelBooking} className="cancel-btn">
                            Cancel Booking
                        </button>
                    )}
                </div>
            )}
        </div>
    );
};

export default CheckBooking;