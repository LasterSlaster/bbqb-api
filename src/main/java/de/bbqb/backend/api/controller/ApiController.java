package de.bbqb.backend.api.controller;

import de.bbqb.backend.api.model.entity.*;
import de.bbqb.backend.api.model.service.BookingService;
import de.bbqb.backend.api.model.service.DeviceService;
import de.bbqb.backend.api.model.service.UserService;
import de.bbqb.backend.stripe.StripeService;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.retry.Repeat;

import java.net.URI;
import java.time.Duration;
import java.util.List;

/**
 * REST Controller with endpoints to manage device resources like
 * accessing/updating/creating device information and sending messages to
 * devices.
 *
 * @author Marius Degen
 */
@CrossOrigin(origins = "*") // CORS configuration to allow all for the endpoints in this controller
@RestController
public class ApiController {

    private DeviceService deviceService;
    private UserService userService;
    private StripeService stripeService;
    private BookingService bookingService;

    public ApiController(DeviceService deviceService, UserService userService, StripeService stripeService, BookingService bookingService) {
        super();
        this.deviceService = deviceService;
        this.userService = userService;
        this.stripeService = stripeService;
        this.bookingService = bookingService;
    }

    /**
     * Endpoint for gcp appengine to test application availability
     *
     * @return hello world string
     */
    @GetMapping("/")
    public String hello() {
        return "Hello World";
    }

    /**
     * Retrieve all cards of a specific user.
     *
     * @return The list of cards of this user
     */
    @GetMapping("/cards")
    public Mono<ResponseEntity<List<Card>>> getCards(@AuthenticationPrincipal Authentication sub) {
        return userService.readUser(sub.getName())
                .flatMap(stripeService::readCards)
                .map(ResponseEntity::ok);
    }

    /**
     * Delete a specific card of a user
     *
     * @return The deleted card
     */
    @DeleteMapping("/cards/{id}")
    public Mono<ResponseEntity<Card>> deleteCard(@AuthenticationPrincipal Authentication sub, @PathVariable("id") String cardId) {
        return userService.readUser(sub.getName())
                .flatMap(user -> stripeService.deleteCard(cardId, user))
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.notFound().build());
    }

    /**
     * Create a SetupIntent to add a card as a payment method for a customer/user on stripe.
     *
     * @return A client secret to complete the SetupIntent
     */
    @PostMapping("/cards")
    public Mono<ResponseEntity<Card>> postCardSetup(@AuthenticationPrincipal Authentication sub) {
        return userService.readUser(sub.getName())
                .flatMap(stripeService::createSetupCardIntent)
                .map(ResponseEntity::ok);
    }

    /**
     * Create a PaymentIntent to pay for a bbqb booking/session.
     *
     * @return A client secret to complete the PaymentIntent
     */
    @PostMapping("/payments")
    public Mono<ResponseEntity<Payment>> postCardPaymentSetup(@AuthenticationPrincipal Authentication sub, BookingRequest request) {
        return userService.readUser(sub.getName())
                .flatMap(user -> stripeService.createCardPaymentIntent(user, 100L, request.getPaymentMethodId())) // TODO: Check how to retrieve the price
                .map(ResponseEntity::ok);
    }

    /**
     * Retrieve all users.
     *
     * @return An Array of user objects.
     */
    @GetMapping("/users")
    public Flux<User> getUsers() {
        return userService.readAllUsers();
    }

    /**
     * Retrieve a user by its ID.
     *
     * @param userId: The ID of the user to retrieve.
     * @return The user identified by the userId parameter.
     */
    // OidcUser.getAuthorities()  OAuth2User.getAuthorities()
    @GetMapping("/users/{id}")
    public Mono<ResponseEntity<User>> getUser(@AuthenticationPrincipal Authentication sub, @PathVariable("id") String userId) {
        if (sub.getName().equals(userId)) {
            return userService.readUser(userId)
                    .map(ResponseEntity.ok()::body)
                    .defaultIfEmpty(ResponseEntity.notFound().build());
        } else {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
        }
    }

    /**
     * Create/register a user with our backend service/database
     *
     * @param user: The user object to register
     * @return The user information that was stored in the database
     */
    @PostMapping("/users")
    public Mono<ResponseEntity<Object>> postUser(@AuthenticationPrincipal Authentication sub, @RequestBody User user) {
        if (sub.getName().equals(user.getId())) {
            ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();
            return userService.readUser(user.getId())
                    .flatMap(alreadyExistingUser -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build())) // User already exists
                    .switchIfEmpty(
                            stripeService.createCustomer(user)
                                    // TODO: createUser overrides id attribute
                                    .flatMap(customer -> userService.createUser(customer))
                                    .map(ResponseEntity.created(builder.build().toUri())::body));
        } else {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
        }
    }

    /**
     * Update the information of a user.
     * TODO: Currently not idempotent! Because it does not use the id from the request but creates a new one
     *
     * @param id:   The ID of the user to be updated. Must be identical to the id field in the user object in the request body.
     * @param user: The user object which will be used to update the user.
     * @return The updated user object.
     */
    @PutMapping("/users/{id}")
    public Mono<ResponseEntity<User>> putUser(@AuthenticationPrincipal Authentication sub, @PathVariable("id") String id, @RequestBody User user) {
        if (sub.getName().equals(id)) {
            ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();
            if (user.getId() != null && user.getId().equals(id)) {
                return userService
                        .updateUser(user)
                        .map(ResponseEntity.created(builder.build().toUri())::body) // TODO: Think about returning 200/204 instead
                        .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
            } else {
                return Mono.just(ResponseEntity.unprocessableEntity().build()); // TODO: Create message "id is missing" or "not equal to deviceId"
            }
        } else {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
        }
    }

    /**
     * Send an open device signal to a device.
     *
     * @param request: The device to send the signal to.
     * @return The device to which the signal was send to with its up to date information.
     */
    @PostMapping("/bookings")
    public Mono<ResponseEntity<Payment>> postBookings(@AuthenticationPrincipal Authentication sub, @RequestBody BookingRequest request) {
        // Parse timeslot
        Timeslot timeslot;
        switch (request.getTimeslot()) {
            case 45:
                timeslot = Timeslot.FOURTY_FIVE;
                break;
            default:
                return Mono.just(ResponseEntity.badRequest().build());
        }
        if (request.getDeviceId() != null) {
            return deviceService.readDevice(request.getDeviceId())
                    .flatMap(device -> userService.readUser(sub.getName()))
                    .flatMap(user -> stripeService.createCardPaymentIntent(
                            user,
                            100L,
                            request.getPaymentMethodId())
                            .map(payment -> Pair.of(payment, user))) // TODO: Check how to retrieve the price
                    .flatMap(pair -> bookingService.createBooking(
                            pair.getFirst().getId(),
                            request.getDeviceId(),
                            pair.getSecond().getId(),
                            timeslot)
                            .map(booking -> pair.getFirst())) // TODO: evaluate what kind of resource to return
                    .map(ResponseEntity::ok)
                    .defaultIfEmpty(ResponseEntity.unprocessableEntity().build());
        } else {
            return Mono.just(ResponseEntity.unprocessableEntity().build());
        }
    }

    // TODO: Endpoint to retrieve all grill sessions for a specific user

    /**
     * Endpoint to retrieve all bookings for the current user
     */
    @GetMapping("/bookings")
    public Flux<Booking> getBookings(@AuthenticationPrincipal Authentication sub) {
        return bookingService.findAllBookingsByUserId(sub.getName());
    }


    /**
     * Endpoint to retrieve all bookings for the current user
     */
    //@GetMapping("/bookings/{id}")
    //public ResponseEntity<Flux<Booking>> getBookings(@AuthenticationPrincipal Authentication sub, @PathVariable("id") String id) {
    //    return bookingService.findBooking(id)
    //            .filter(booking -> booking.getUserId().contentEquals(sub.getName()))
    //            .map(ResponseEntity::ok)
    //            //.switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
    //            ;
    //}

    /**
     * Retrieve all devices.
     *
     * @return An Array of device objects.
     */
    @GetMapping("/devices")
    public Flux<Device> getDevices() {
        return deviceService.readAllDevices();
    }

    /**
     * Retrieve a device by its ID.
     *
     * @param deviceId: The ID of the device to retrieve.
     * @return The device identified by the deviceId parameter.
     */
    @GetMapping("/devices/{id}")
    public Mono<ResponseEntity<Device>> getDevice(@PathVariable("id") String deviceId) {
        return deviceService.readDevice(deviceId)
                .map(ResponseEntity.ok()::body)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Create a new device.
     *
     * @param device: The device to be created.
     * @return The created device with additional information.
     */
    @PostMapping("/devices")
    public Mono<ResponseEntity<Device>> postDevices(@RequestBody Device device) {
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();
        // TODO: Validate device object
        return deviceService.createDevice(device).map((Device savedDevice) -> {
            URI uri = builder.path("/{id}").buildAndExpand(savedDevice.getId()).toUri();
            return ResponseEntity.created(uri).body(savedDevice);
        });
    }

    /**
     * Update the information of a device.
     * TODO: Currently not idempotent! Because it does not use the id from the request but creates a new one
     *
     * @param id:     The ID of the device to be updated. Must be identical to the id field in the device object in the request body.
     * @param device: The device object which will be used to update the device.
     * @return The updated device.
     */
    @PutMapping("/devices/{id}")
    public Mono<ResponseEntity<Device>> putDevices(@PathVariable("id") String id, @RequestBody Device device) {
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();
        if (device.getId() != null && device.getId().equals(id)) {
            return deviceService
                    .updateDevice(device)
                    // TODO: Check if there is a active booking for this device and user
                    .flatMap(updatedDevice -> this.checkToOpenDevice(device, updatedDevice))
                    .map(ResponseEntity.created(builder.build().toUri())::body) // TODO: Think about returning 200/204 instead
                    .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        } else {
            return Mono.just(ResponseEntity.unprocessableEntity().build()); // TODO: Create message "id is missing" or "not equal to deviceId"
        }
    }

    private Mono<Device> checkToOpenDevice(Device device, Device updatedDevice) {
        // TODO: Think about moving this into deviceService
        if (device.getLocked() != null && device.getLocked() == false) {
            if (device.getDeviceId() != null) {
                return deviceService.openDevice(device.getDeviceId())
                        .then(deviceService.readDevice(device.getId()))
                        .switchIfEmpty(Mono.error(new Exception())) // TODO: Implement better exception
                        // Repeatedly fetch the device the signal was send to until it is unlocked. 5 times with 1 second delays
                        .filter(pendingDevice -> pendingDevice.getLocked() == false)
                        .repeatWhenEmpty(Repeat.times(10).fixedBackoff(Duration.ofSeconds(1)))
                        .doOnSuccess(result -> {
                            // If the device is still locked after repeats throw exception
                            if (result == null) {
                                throw Exceptions.propagate(new Exception());
                            }
                        });
            } else {
                return Mono.error(new Exception()); // TODO: Implement better exception
            }
        } else {
            // Continue with updatedDevice if it doesn't have to be unlocked
            return Mono.just(updatedDevice);
        }
    }
}