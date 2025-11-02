package tqs.data;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRequestRepo extends JpaRepository<BookingRequest, Long> {
        public Optional<BookingRequest> findById(Long id);

        public Optional<BookingRequest> findByToken(String token);

        public long countByMunicipalityAndCollectionDateAndTimeSlot(String municipality, LocalDate collectionDate,
                        Slot timeSlot);

        public List<BookingRequest> findByStatus(Status status);

        public List<BookingRequest> findByCollectionDateBetween(LocalDate start, LocalDate end);

        public List<BookingRequest> findByMunicipalityAndCollectionDate(String municipality,
                        LocalDate collectionDate);

}
