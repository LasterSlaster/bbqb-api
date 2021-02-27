package de.bbqb.backend.api.controller;

import de.bbqb.backend.api.model.entity.*;
import de.bbqb.backend.api.model.service.UserService;
import de.bbqb.backend.stripe.StripeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * REST Controller with endpoints to manage card resources
 *
 * @author Marius Degen
 */
@CrossOrigin(origins = "*") // CORS configuration to allow all for the endpoints in this controller
@RestController
public class CardController {

    private UserService userService;
    private StripeService stripeService;

    public CardController(UserService userService, StripeService stripeService) {
        super();
        this.userService = userService;
        this.stripeService = stripeService;
    }

    /**
     * Retrieve all cards for a specific user.
     *
     * @return The list of cards for this user
     */
    @GetMapping("/cards")
    public Mono<ResponseEntity<List<Card>>> getCards(@AuthenticationPrincipal Authentication sub) {
        // TODO: Extend stripeService to do user validation
        return userService.readUser(sub.getName())
                .flatMap(stripeService::readCards)
                .map(ResponseEntity::ok);
    }

    /**
     * Delete a specific card for a user
     *
     * @return The deleted card
     */
    @DeleteMapping("/cards/{id}")
    public Mono<ResponseEntity<Card>> deleteCard(@AuthenticationPrincipal Authentication sub, @PathVariable("id") String cardId) {
        // TODO: Extend stripeService to do user validation
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
        // TODO: Extend stripeService to do user validation
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
        // TODO: Extend bookingService to do user validation
        return userService.readUser(sub.getName())
                .flatMap(user -> stripeService.createCardPaymentIntent(user, 100L, request.getPaymentMethodId())) // TODO: Check how to retrieve the price
                .map(ResponseEntity::ok);
    }
}
