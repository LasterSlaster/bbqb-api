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

/**
 * Service to manage booking resources
 *
 * @author Marius Degen
 */
@Service
public class FirestoreBookingService implements BookingService {
    private BookingRepo repo;
    private Firestore firestore;

    public FirestoreBookingService(BookingRepo repo, Firestore firestore) {
        this.repo = repo;
        this.firestore = firestore;
    }

    /**
     * Find a particular booking by its id
     *
     * @param bookingId must not be null
     * @throws IllegalAccessException in case the bookingId is null
     * @return A Mono emitting a booking or Mono.empty if none ist found
     */
    public Mono<Booking> findBooking(String bookingId) {
        return this.repo.findById(bookingId).map(this::fromBookingDocToBooking);
    }

    /**
     * Find all bookings that where created by particular user
     *
     * @param userId must not be null
     * @throws IllegalAccessException in case the userId is null
     * @return A Flux emitting all bookings found for the user identified by the userId or Flux.empty if none ist found
     */
    public Flux<Booking> findAllBookingsByUserId(String userId) {
        return this.repo.findAllByUserId(userId).map(this::fromBookingDocToBooking);
    }

    /**
     * Find all bookings that where created for a particular device(BBQB)
     *
     * @param deviceId must not be null
     * @throws IllegalAccessException in case the deviceId is null
     * @return A Flux emitting all bookings found for the device identified by the deviceId or Flux.empty if none ist found
     */
    public Flux<Booking> findAllBookingsByDeviceId(String deviceId) {
        return this.repo.findAllByDeviceId(deviceId).map(this::fromBookingDocToBooking);
    }

    /**
     * Find a booking by its payment id
     *
     * @param paymentIntentId must not be null
     * @throws IllegalAccessException in case the paymentIntentId is null
     * @return Mono emitting the requested booking or Mono.empty if none is found
     */
    public Mono<Booking> findBookingByPaymentIntentId(String paymentIntentId) {
        return this.repo.findByPaymentIntentId(paymentIntentId).map(this::fromBookingDocToBooking);
    }

    /**
     * Create a new booking with the provided values
     *
     * @param paymentIntentId
     * @param deviceId id of the BBQB to create a booking for
     * @param userId id of the user who wants to create a booking
     * @param timeslot time the BBQB will be reserved
     * @return Mono emitting the created booking
     */
    public Mono<Booking> createBooking(String paymentIntentId,
                                       String deviceId,
                                       String userId,
                                       Timeslot timeslot) {
        String id = firestore.collection("bookings").document().getId();
        return repo.save(
                new BookingDoc(
                    id,
                    paymentIntentId,
                    deviceId,
                    userId,
                    "pending",
                    Timestamp.now(),
                    timeslot.getTime(),
                    null))
                .map(this::fromBookingDocToBooking);
    }

    /**
     * Update a booking with the provided booking values.
     * The booking to update is identified by booking.getId()
     * If the provided booking does not exists no write operation is performed.
     *
     * @param booking The new values for the booking identified by its id
     * @throws IllegalAccessException in case the given id is null
     * @return Mono emitting the updated booking object or Mono.empty() if no booking was found
     */
    public Mono<Booking> updateBooking(Booking booking) {
        return this.repo.findById(booking.getId())
                // TODO: Complete this implementation
                .flatMap(bookingDoc -> this.repo.save(fromBookingToBookingDoc(booking)))
                .map(this::fromBookingDocToBooking);
    }

    private BookingDoc fromBookingToBookingDoc(Booking booking) {
        return new BookingDoc(
                booking.getId(),
                booking.getPaymentIntentId(),
                booking.getDeviceId(),
                booking.getUserId(),
                booking.getStatus(),
                booking.getRequestTime() != null ? Timestamp.of(booking.getRequestTime()) : null,
                booking.getTimeslot(),
                booking.getSessionStart() != null ? Timestamp.of(booking.getSessionStart()) : null);
    }

    private Booking fromBookingDocToBooking(BookingDoc bookingDoc) {
        return new Booking(bookingDoc.getId(),
                bookingDoc.getPaymentIntentId(),
                bookingDoc.getDeviceId(),
                bookingDoc.getUserId(),
                bookingDoc.getStatus(),
                (bookingDoc.getRequestTime() == null ? null : bookingDoc.getRequestTime().toDate()),
                (bookingDoc.getSessionStart() == null ? null :bookingDoc.getSessionStart().toDate()),
                null,
                bookingDoc.getTimeslot());
    }
}
