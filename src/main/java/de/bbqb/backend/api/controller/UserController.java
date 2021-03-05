package de.bbqb.backend.api.controller;

import de.bbqb.backend.api.model.entity.*;
import de.bbqb.backend.api.model.service.BookingService;
import de.bbqb.backend.api.model.service.UserService;
import de.bbqb.backend.stripe.StripeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;

/**
 * REST Controller with endpoints to manage user resources
 *
 * @author Marius Degen
 */
@CrossOrigin(origins = "*") // CORS configuration to allow all for the endpoints in this controller
@RestController
public class UserController {

    private UserService userService;
    private StripeService stripeService;
    private BookingService bookingService;

    public UserController(UserService userService, StripeService stripeService, BookingService bookingService) {
        super();
        this.userService = userService;
        this.stripeService = stripeService;
        this.bookingService = bookingService;
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
    @GetMapping("/users/{id}")
    public Mono<ResponseEntity<User>> getUser(@AuthenticationPrincipal Authentication sub, @PathVariable("id") String userId) {
        if (sub.getName().equals(userId)) {
            // TODO: Extend bookingService to do user validation
            return userService.readUser(userId)
                    .flatMap(user -> this.bookingService.findAllBookingsByUserId(user.getId()).sort(Comparator.comparing(Booking::getRequestTime, Comparator.reverseOrder())).next().map(booking -> new User(user.getId(), user.getStripeCustomerId(), booking.getId(), user.getFirstName(), user.getLastName(), user.getEmail())).defaultIfEmpty(user)) // TODO: Find last booking session
                    .map(user -> {
                        ResponseEntity.BodyBuilder response = ResponseEntity.ok();
                        if (user.getLastBookingId() != null) {
                            response.header("Link", "</bookings/" + user.getId() + ">; rel=\"currentBooking\"");
                        }
                        return response.body(user);
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
            // TODO: Move evaluation if user already exists to Service layer
            return userService.readUser(user.getId())
                    .flatMap(alreadyExistingUser -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build())) // User already exists
                    .switchIfEmpty(
                            stripeService.createCustomer(user.getId())
                                    // TODO: createUser overrides id attribute
                                    .flatMap(customer -> userService.createUser(customer))
                                    .map(ResponseEntity.created(builder.build().toUri())::body));
        } else {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
        }
    }

    // TODO: Currently not idempotent! Because it does not use the id from the request but creates a new one
    /**
     * Update user information.
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
}
