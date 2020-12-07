package de.bbqb.backend.api.model.service;

import de.bbqb.backend.api.model.entity.Booking;
import de.bbqb.backend.api.model.entity.Timeslot;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookingService {
    Mono<Booking> createBooking(String paymentIntentId, String deviceId, String userId, Timeslot timeslot);
    Mono<Booking> findBooking(String bookingId);
    Flux<Booking> findAllBookingsByUserId(String userId);
    Flux<Booking> findAllBookingsByDeviceId(String deviceId);
    Mono<Booking> findBookingByPaymentIntentId(String deviceId);
    Mono<Booking> updateBooking(Booking booking);
}
