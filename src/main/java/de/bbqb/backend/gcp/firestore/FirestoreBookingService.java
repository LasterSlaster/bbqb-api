package de.bbqb.backend.gcp.firestore;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import de.bbqb.backend.api.model.entity.Booking;
import de.bbqb.backend.api.model.entity.Timeslot;
import de.bbqb.backend.api.model.service.BookingService;
import de.bbqb.backend.gcp.firestore.document.BookingDoc;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FirestoreBookingService implements BookingService {
    private BookingRepo repo;
    private Firestore firestore;

    public FirestoreBookingService(BookingRepo repo, Firestore firestore) {
        this.repo = repo;
        this.firestore = firestore;
    }

    public Mono<Booking> findBooking(String bookingId) {
        return this.repo.findById(bookingId).map(this::fromBookingDocToBooking);
    }

    public Flux<Booking> findAllBookingsByUserId(String userId) {
        return this.repo.findAllByUserId(userId).map(this::fromBookingDocToBooking);
    }

    public Flux<Booking> findAllBookingsByDeviceId(String deviceId) {
        return this.repo.findAllByDeviceId(deviceId).map(this::fromBookingDocToBooking);
    }

    public Mono<Booking> findBookingByPaymentIntentId(String paymentIntentId) {
        return this.repo.findByPaymentIntentId(paymentIntentId).map(this::fromBookingDocToBooking);
    }

    public Mono<Booking> createBooking(String paymentIntentId, String deviceId, String userId, Timeslot timeslot) {
        // TODO: Validate timeslot
        String id = firestore.collection("bookings").document().getId();
        return repo.save(new BookingDoc(id, paymentIntentId, deviceId, userId, "pending", Timestamp.now(), timeslot.getTime()))
                .map(this::fromBookingDocToBooking);
    }

    public Mono<Booking> updateBooking(Booking booking) {
        return this.repo.findById(booking.getId())
                // TODO: Complete this implementation
                .flatMap(bookingDoc -> this.repo.save(fromBookingToBookingDoc(booking)))
                .map(this::fromBookingDocToBooking);
    }

    private BookingDoc fromBookingToBookingDoc(Booking booking) {
        return new BookingDoc(booking.getId(), booking.getPaymentIntentId(), booking.getDeviceId(), booking.getUserId(), booking.getStatus(), Timestamp.of(booking.getTimestamp()), booking.getTimeslot());
    }
    private Booking fromBookingDocToBooking(BookingDoc bookingDoc) {
        return new Booking(bookingDoc.getId(), bookingDoc.getPaymentIntentId(), bookingDoc.getDeviceId(), bookingDoc.getUserId(), bookingDoc.getStatus(), bookingDoc.getTimestamp().toDate(), null, bookingDoc.getTimeslot());
    }
}
