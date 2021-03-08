package de.bbqb.backend.api.controller;

import de.bbqb.backend.api.model.entity.*;
import de.bbqb.backend.api.model.service.BookingService;
import de.bbqb.backend.api.model.service.DeviceService;
import de.bbqb.backend.api.model.service.UserService;
import de.bbqb.backend.stripe.StripeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST Controller with endpoints to manage booking resources
 *
 * @author Marius Degen
 */
@CrossOrigin(origins = "*")
@RestController
public class BookingController {

    private DeviceService deviceService;
    private UserService userService;
    private StripeService stripeService;
    private BookingService bookingService;
    private static final Logger LOGGER = LoggerFactory.getLogger(BookingController.class);

    public BookingController(DeviceService deviceService, UserService userService, StripeService stripeService, BookingService bookingService) {
        super();
        this.deviceService = deviceService;
        this.userService = userService;
        this.stripeService = stripeService;
        this.bookingService = bookingService;
    }

    /**
	 * Create a booking for a BBQB device and lock the device.
     *
     * @param request: A BookingRequest
     * @return The pending Booking object including the payment information with paymentIntentId and client secret.
     */
    @PostMapping("/bookings")
    public Mono<ResponseEntity<Booking>> postBookings(@AuthenticationPrincipal Authentication sub, @RequestBody BookingRequest request) {
        // TODO: Update test for this method because most of the code in here was moved to the service level
        Timeslot timeslot;
        try {
            // Parse timeslot
            timeslot = Timeslot.getTimeslot(request.getTimeslot());
        } catch (IllegalArgumentException e) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        if (request.getDeviceId() != null) {
            return bookingService.createBooking(request.getPaymentMethodId(), request.getDeviceId(), sub.getName(), timeslot)
                    .map(ResponseEntity::ok)
                    .onErrorReturn(ResponseEntity.unprocessableEntity().build());
        } else {
            return Mono.just(ResponseEntity.unprocessableEntity().build());
        }
    }

    /**
     * Endpoint to retrieve all bookings for the current user
     *
     * @param sub
     * @return
     */
    @GetMapping("/bookings")
    public Flux<Booking> getBookings(@AuthenticationPrincipal Authentication sub) {
        return bookingService.findAllBookingsByUserId(sub.getName());
    }


    /**
     * Endpoint to retrieve a specific booking
     *
     * @param sub
     * @param id
     * @return
     */
    @GetMapping("/bookings/{id}")
    public Mono<ResponseEntity<Booking>> getBooking(@AuthenticationPrincipal Authentication sub, @PathVariable("id") String id) {
        return bookingService.findBooking(id, sub.getName())
                .map(booking -> ResponseEntity.ok().header("Link", "</devices/" + booking.getDeviceId() + ">; rel=\"device\"").body(booking))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
