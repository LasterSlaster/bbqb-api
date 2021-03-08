package de.bbqb.backend.gcp.firestore;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import de.bbqb.backend.api.model.entity.Booking;
import de.bbqb.backend.api.model.entity.Device;
import de.bbqb.backend.api.model.entity.Timeslot;
import de.bbqb.backend.api.model.service.BookingService;
import de.bbqb.backend.api.model.service.DeviceService;
import de.bbqb.backend.api.model.service.UserService;
import de.bbqb.backend.gcp.firestore.document.BookingDoc;
import de.bbqb.backend.stripe.StripeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service to manage booking resources
 *
 * @author Marius Degen
 */
@Service
public class FirestoreBookingService implements BookingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FirestoreBookingService.class);
    private BookingRepo repo;
    private Firestore firestore;
    private DeviceService deviceService;
    private UserService userService;
    private StripeService stripeService;

    public FirestoreBookingService(BookingRepo repo, Firestore firestore, DeviceService deviceService, UserService userService, StripeService stripeService) {
        this.repo = repo;
        this.firestore = firestore;
        this.deviceService = deviceService;
        this.userService = userService;
    }

    /**
     * Find a particular booking by its id
     *
     * @param bookingId must not be null
     * @return A Mono emitting a booking or Mono.empty if no booking with such an ID is found for this user
     * @throws IllegalArgumentException in case the bookingId is null
     */
    public Mono<Booking> findBooking(String bookingId, String userId) {
        Assert.notNull(userId, "Parameter userId must not be null");
        return this.repo
                .findById(bookingId)
                .map(this::fromBookingDocToBooking)
                .filter(booking -> booking.getUserId().contentEquals(userId));
    }

    /**
     * Find all bookings that where created by particular user
     *
     * @param userId must not be null
     * @return A Flux emitting all bookings found for the user identified by the userId or Flux.empty if none ist found
     * @throws IllegalArgumentException in case the userId is null
     */
    public Flux<Booking> findAllBookingsByUserId(String userId) {
        return this.repo.findAllByUserId(userId).map(this::fromBookingDocToBooking);
    }

    /**
     * Find all bookings that where created for a particular device(BBQB)
     *
     * @param deviceId must not be null
     * @return A Flux emitting all bookings found for the device identified by the deviceId or Flux.empty if none ist found
     * @throws IllegalArgumentException in case the deviceId is null
     */
    public Flux<Booking> findAllBookingsByDeviceId(String deviceId) {
        return this.repo.findAllByDeviceId(deviceId).map(this::fromBookingDocToBooking);
    }

    /**
     * Find a booking by its payment id
     *
     * @param paymentIntentId must not be null
     * @return Mono emitting the requested booking or Mono.empty if none is found
     * @throws IllegalArgumentException in case the paymentIntentId is null
     */
    public Mono<Booking> findBookingByPaymentIntentId(String paymentIntentId) {
        return this.repo.findByPaymentIntentId(paymentIntentId).map(this::fromBookingDocToBooking);
    }

    /**
     * Create a new booking with the provided values.
     * Also blocks the device and creates a stripe payment intent.
     *
     * @param paymentMethodId id of the stripe payment method to use for the payment
     * @param deviceId        id of the BBQB to create a booking for
     * @param userId          id of the user who wants to create a booking
     * @param timeslot        time the BBQB will be reserved
     * @return Mono emitting the created booking
     * @throws RuntimeException in case the device with id deviceId is already blocked by another user or the device or user could not be found
     */
    public Mono<Booking> createBooking(String paymentMethodId,
                                       String deviceId,
                                       String userId,
                                       Timeslot timeslot) {
        // TODO: Update test for this method
        // TODO: Check if database integrity stays consistent. Think about creating a transaction for these database requests or update database schema
        return deviceService.readDevice(deviceId)
                .switchIfEmpty(Mono.error(new RuntimeException("No device with id " + deviceId + " was found")))
                .flatMap(device -> {
                    if (device.getBlocked()) {
                        return Mono.error(new RuntimeException("Device " + deviceId + " is already blocked"));
                    } else {
                        return Mono.just(device);
                    }
                })
                .flatMap(device ->
                        deviceService.updateDevice(
                                new Device(
                                        device.getId(),
                                        device.getDeviceId(),
                                        device.getNumber(),
                                        device.getPublishTime(),
                                        true,
                                        device.getLocked(),
                                        device.getClosed(),
                                        device.getWifiSignal(),
                                        device.getIsTemperaturePlate1(),
                                        device.getIsTemperaturePlate2(),
                                        device.getSetTemperaturePlate1(),
                                        device.getSetTemperaturePlate2(),
                                        device.getLocation(),
                                        device.getAddress())))
                .flatMap(device -> userService.readUser(userId)
                        .switchIfEmpty(Mono.error(new RuntimeException("No user found with id " + userId)))
                        .flatMap(user ->
                                stripeService.createCardPaymentIntent(
                                        userId, //  TODO: Think about pasing a user object so that stripe service does not has to read user again
                                        timeslot.getCost(),// TODO: Check how to retrieve the price. Currently stored as part of the Timeslot enum
                                        paymentMethodId))
                        .flatMap(payment ->
                                repo.save(
                                        new BookingDoc(
                                                firestore.collection("bookings").document().getId(),
                                                payment.getId(),
                                                deviceId,
                                                userId,
                                                "pending",
                                                Timestamp.now(),
                                                timeslot.getTime(),
                                                null))
                                        .map(this::fromBookingDocToBooking))
                        .doOnError(e -> {
                                    LOGGER.warn("Error occurred while creating a booking so device will be unblocked.");
                                    deviceService
                                            .readDevice(deviceId)
                                            .flatMap(blockedDevice ->
                                                    deviceService.updateDevice(
                                                            new Device(
                                                                    blockedDevice.getId(),
                                                                    blockedDevice.getDeviceId(),
                                                                    blockedDevice.getNumber(),
                                                                    blockedDevice.getPublishTime(),
                                                                    false,
                                                                    blockedDevice.getLocked(),
                                                                    blockedDevice.getClosed(),
                                                                    blockedDevice.getWifiSignal(),
                                                                    blockedDevice.getIsTemperaturePlate1(),
                                                                    blockedDevice.getIsTemperaturePlate2(),
                                                                    blockedDevice.getSetTemperaturePlate1(),
                                                                    blockedDevice.getSetTemperaturePlate2(),
                                                                    blockedDevice.getLocation(),
                                                                    blockedDevice.getAddress())))
                                            .subscribe();
                                }
                        )
                );
    }

    /**
     * Update a booking with the provided booking values.
     * The booking to update is identified by booking.getId()
     * If the provided booking does not exists no write operation is performed.
     *
     * @param booking The new values for the booking identified by its id
     * @return Mono emitting the updated booking object or Mono.empty() if no booking was found
     * @throws IllegalArgumentException in case the given id is null
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
