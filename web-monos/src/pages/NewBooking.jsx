import React, { useState, useEffect } from 'react';
import { bookingService, municipalityService } from '../services/api';

const NewBooking = () => {
    const [municipalities, setMunicipalities] = useState([]);
    const [formData, setFormData] = useState({
        contactInfo: '',
        address: '',
        municipality: '',
        collectionDate: '',
        timeSlot: '',
        description: ''
    });
    const [loading, setLoading] = useState(false);
    const [result, setResult] = useState(null);

    useEffect(() => {
        loadMunicipalities();
    }, []);

    const loadMunicipalities = async () => {
        try {
            const response = await municipalityService.getMunicipalities(); // call external api
            setMunicipalities(response.data);
        } catch (error) {
            console.error('Error loading municipalities:', error);
            // fallback mock data
            setMunicipalities([
                { id: 1, name: 'Lisbon' },
                { id: 2, name: 'Porto' },
                { id: 3, name: 'Aveiro' }
            ]);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        console.log(formData);
        try {
            const response = await bookingService.createBooking(formData);
            setResult({
                success: true,
                token: response.data.token,
                message: 'Booking created successfully!'
            });
            // Reset form
            setFormData({
                contactInfo: '',
                address: '',
                municipality: '',
                collectionDate: '',
                timeSlot: '',
                description: ''
            });
        } catch (error) {
            setResult({
                success: false,
                message: error.response?.data?.message || 'Error creating booking. Please try again.'
            });
        }
        setLoading(false);
    };

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    return (
        <div className="page">
            <h2>New Collection Booking</h2>

            {result && (
                <div className={`result ${result.success ? 'success' : 'error'}`}>
                    {result.message}
                    {result.token && (
                        <div>
                            <strong>Your booking token: {result.token}</strong>
                            <p>Save this token to check your booking status later.</p>
                        </div>
                    )}
                </div>
            )}

            <form onSubmit={handleSubmit} className="booking-form">
                <div className="form-group">
                    <label>Contact info (email/phone) *</label>
                    <input
                        type="text"
                        name="contactInfo"
                        value={formData.contactInfo}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className="form-group">
                    <label>Address *</label>
                    <input
                        type="text"
                        name="address"
                        value={formData.address}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className="form-group">
                    <label>Municipality *</label>
                    <select name="municipality" value={formData.municipality} onChange={handleChange} required>
                        <option value="">Select Municipality</option>
                        {municipalities.map(muni => (
                            <option key={muni.code} value={muni.code}>
                                {muni.name}
                            </option>
                        ))}
                    </select>
                </div>

                <div className="form-group">
                    <label>Collection Date *</label>
                    <input
                        type="date"
                        name="collectionDate"
                        value={formData.collectionDate}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className="form-group">
                    <label>Preferred Time Slot*</label>
                    <select
                        name="timeSlot"
                        value={formData.timeSlot}
                        onChange={handleChange}
                        required
                    >
                        <option value="">Select Time</option>
                        <option value="MORNING">Morning (8:00-12:00)</option>
                        <option value="AFTERNOON">Afternoon (12:00-17:00)</option>
                        <option value="EVENING">Evening (17:00-20:00)</option>
                    </select>
                </div>

                <div className="form-group">
                    <label>Items Description *</label>
                    <textarea
                        name="description"
                        value={formData.description}
                        onChange={handleChange}
                        placeholder="Describe the items to be collected (e.g., old mattress, refrigerator, furniture...)"
                        required
                    />
                </div>

                <button type="submit" disabled={loading}>
                    {loading ? 'Creating Booking...' : 'Book Collection'}
                </button>
            </form>
        </div>
    );
};

export default NewBooking;