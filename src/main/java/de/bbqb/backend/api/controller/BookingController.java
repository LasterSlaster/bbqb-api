package de.bbqb.backend.api.controller;

import de.bbqb.backend.api.model.entity.*;
import de.bbqb.backend.api.model.service.BookingService;
import de.bbqb.backend.api.model.service.DeviceService;
import de.bbqb.backend.api.model.service.UserService;
import de.bbqb.backend.stripe.StripeService;
import org.springframework.data.util.Pair;
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
        // TODO: Refactor this code block and extract business logic to service layer
        Timeslot timeslot;
        try {
            // Parse timeslot
            timeslot = Timeslot.getTimeslot(request.getTimeslot());
        } catch (IllegalArgumentException e) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        if (request.getDeviceId() != null) {
            // TODO: Check if database integrity stays consistent. Think about creating a transaction for these database requests or update database schema
            return deviceService.readDevice(request.getDeviceId())
                    .flatMap(device -> {
                        if (device.getBlocked()) {
                            return Mono.empty();
                        } else {
                            return Mono.just(device);
                        }
                    })
                    .onErrorResume(a -> Mono.empty()) // TODO: Add logging
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
                    .flatMap(device -> userService.readUser(sub.getName()))
                    .flatMap(user -> 
							stripeService.createCardPaymentIntent(
								sub.getName(),
								timeslot.getCost(),
								request.getPaymentMethodId())
								.map(payment -> Pair.of(payment, user))) // TODO: Check how to retrieve the price
                    .flatMap(pair -> 
							bookingService.createBooking(
								pair.getFirst().getId(),
								request.getDeviceId(),
								pair.getSecond().getId(),
								timeslot)
                            .map(booking -> 
								new Booking(
									booking.getId(), 
									booking.getPaymentIntentId(), 
									booking.getDeviceId(), 
									booking.getUserId(), 
									booking.getStatus(), 
									booking.getRequestTime(), 
									booking.getSessionStart(), 
									pair.getFirst(), 
									booking.getTimeslot()))) // TODO: evaluate what kind of resource to return
                    .map(ResponseEntity::ok)
                    .doOnError(e -> { // TODO: Error is also created
                        deviceService
                                .readDevice(request.getDeviceId())
                                .flatMap(device ->
                                        deviceService.updateDevice(
                                                new Device(
                                                        device.getId(),
                                                        device.getDeviceId(),
                                                        device.getNumber(),
                                                        device.getPublishTime(),
                                                        false,
                                                        device.getLocked(),
                                                        device.getClosed(),
                                                        device.getWifiSignal(),
                                                        device.getIsTemperaturePlate1(),
                                                        device.getIsTemperaturePlate2(),
                                                        device.getSetTemperaturePlate1(),
                                                        device.getSetTemperaturePlate2(),
                                                        device.getLocation(),
                                                        device.getAddress())))
                                .subscribe();
                    })
                    .defaultIfEmpty(ResponseEntity.unprocessableEntity().build());
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
