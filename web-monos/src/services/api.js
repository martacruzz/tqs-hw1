import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

export const bookingService = {
    // citizen
    createBooking: (bookingData) => api.post('/bookings', bookingData),
    getBooking: (token) => api.get(`/bookings/${token}`),
    cancelBooking: (token) => api.delete(`/bookings/${token}`),

    // staff
    getAllBookings: () => api.get('/staff/bookings'),
    updateBookingStatus: (token, status) =>
        api.patch(`/staff/bookings/${token}/update`, null, {
            params: { newStatus: status }
        }),
};

export const municipalityService = {
    getMunicipalities: () => api.get('/municipalities'),
};

export default api;