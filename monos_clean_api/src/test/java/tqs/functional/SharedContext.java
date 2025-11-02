package tqs.functional;

public class SharedContext {
    private String bookingToken;

    public String getBookingToken() {
        return bookingToken;
    }

    public void setBookingToken(String token) {
        this.bookingToken = token;
    }
}