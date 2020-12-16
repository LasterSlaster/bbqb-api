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
                    .flatMap(user -> this.bookingService.findAllBookingsByUserId(user.getId()).next().map(booking -> Pair.of(user, booking)).defaultIfEmpty(Pair.of(user, null))) // TODO: Find last booking session
                    .map(pair -> {
                        ResponseEntity.BodyBuilder response = ResponseEntity.ok();
                        if (pair.getSecond() != null) {
                            response.header("Link", "</bookings/" + pair.getSecond().getId() + ">; rel=\"currentBooking\"");
                        }
                        return response.body(pair.getFirst());
                    })
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
     * @param request: A BookingRequest
     * @return The pending Booking object including the payment information with paymentIntentId and client secret.
     */
    @PostMapping("/bookings")
    public Mono<ResponseEntity<Booking>> postBookings(@AuthenticationPrincipal Authentication sub, @RequestBody BookingRequest request) {
        // Parse timeslot
        Timeslot timeslot;
        switch (request.getTimeslot()) {
            case 45:
                timeslot = Timeslot.FOURTY_FIVE;
                break;
            case 90:
                timeslot = Timeslot.NINETY;
                break;
            default:
                return Mono.just(ResponseEntity.badRequest().build());
        }
        if (request.getDeviceId() != null) {
            return deviceService.readDevice(request.getDeviceId())
                    .flatMap(device -> {
                        if (device.getBlocked()) {
                            return Mono.error(new Exception("Device already blocked"));
                        } else if(device.getLocked()) {
                            return Mono.error(new Exception("Device already locked"));
                        } else {
                            return Mono.just(device);
                        }
                    })
                    .flatMap(device ->
                            deviceService.updateDevice(new Device(
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
                                    device.getAddress()))
                    )
                    .flatMap(device -> userService.readUser(sub.getName()))
                    .flatMap(user -> stripeService.createCardPaymentIntent(
                            user,
                            timeslot.getCost(),
                            request.getPaymentMethodId())
                            .map(payment -> Pair.of(payment, user))) // TODO: Check how to retrieve the price
                    .flatMap(pair -> bookingService.createBooking(
                            pair.getFirst().getId(),
                            request.getDeviceId(),
                            pair.getSecond().getId(),
                            timeslot)
                            .map(booking -> new Booking(booking.getId(), booking.getPaymentIntentId(), booking.getDeviceId(), booking.getUserId(), booking.getStatus(), booking.getRequestTime(), booking.getSessionStart(), pair.getFirst(), booking.getTimeslot()))) // TODO: evaluate what kind of resource to return
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
                    .defaultIfEmpty(ResponseEntity.unprocessableEntity().build())
                    ;
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
    public Mono<ResponseEntity<Booking>> getBookings(@AuthenticationPrincipal Authentication sub, @PathVariable("id") String id) {
        return bookingService.findBooking(id)
                .filter(booking -> booking.getUserId().contentEquals(sub.getName()))
                .map(booking -> ResponseEntity.ok().header("Link", "</devices/" + booking.getDeviceId() + ">; rel=\"device\"").body(booking))
                .defaultIfEmpty(ResponseEntity.notFound().build())
                ;
    }

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
    public Mono<ResponseEntity<Device>> putDevices(@AuthenticationPrincipal Authentication sub, @PathVariable("id") String id, @RequestBody Device device) {
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();
        if (device.getId() != null && device.getId().equals(id)) {
            return deviceService
                    .updateDevice(device)
                    // TODO: Check if there is a active booking for this device and user
                    .flatMap(updatedDevice -> this.checkToOpenDevice(sub.getName(), device, updatedDevice))
                    .map(ResponseEntity.created(builder.build().toUri())::body) // TODO: Think about returning 200/204 instead
                    .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        } else {
            return Mono.just(ResponseEntity.unprocessableEntity().build()); // TODO: Create message "id is missing" or "not equal to deviceId"
        }
    }

    private Mono<Device> checkToOpenDevice(String userId, Device device, Device updatedDevice) {
        // TODO: Think about moving this into deviceService
        if (device.getLocked() != null && device.getLocked() == false) {
            if (device.getDeviceId() != null) {
                return bookingService.findAllBookingsByUserId(userId)
                        .filter(booking -> booking.getDeviceId().contentEquals(device.getDeviceId())) // TODO: Also evaluate if booking/session has not already successfully opened the device, yet(By status?)
                        .switchIfEmpty(Mono.error(new Exception("User has no successful booking for this device"))) // TODO: Check if filtering ever returns empty mono
                        .flatMap(booking -> deviceService.openDevice(device.getDeviceId()))
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