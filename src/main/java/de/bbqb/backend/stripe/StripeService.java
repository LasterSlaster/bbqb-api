package de.bbqb.backend.stripe;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodListParams;
import com.stripe.param.SetupIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import de.bbqb.backend.api.model.entity.Card;
import de.bbqb.backend.api.model.entity.Payment;
import de.bbqb.backend.api.model.entity.User;
import de.bbqb.backend.api.model.service.CustomerService;
import de.bbqb.backend.api.model.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class StripeService implements CustomerService {

    private UserService userService;

    public StripeService(@Value("${bbq.backend.stripe.apikey}") String apiKey, UserService userService) {
        Stripe.apiKey = apiKey;
        this.userService = userService;
    }

    /**
     * Create a new customer
     *
     * @param userId Id of an existing user
     * @return A Mono emitting the created user
     */
    public Mono<User> createCustomer(String userId) {
        // TODO: Check if it makes sense the require an existing user before creating a stripe customer. Maybe move firbase user creation into this method
        return userService.readUser(userId)
                .flatMap(user ->
                        Mono.create(userMonoSink -> {
                            CustomerCreateParams customerParam = new CustomerCreateParams.Builder()
                                    .setEmail(user.getEmail())
                                    .setName(user.getFirstName() + " " + user.getLastName())
                                    .build();
                            try {
                                Customer stripeCustomer = Customer.create(customerParam); // Blocking request
                                User newCustomer = new User(
                                        user.getId(),
                                        stripeCustomer.getId(),
                                        null,
                                        user.getFirstName(),
                                        user.getLastName(),
                                        user.getEmail());
                                userMonoSink.success(newCustomer);
                            } catch (StripeException e) {
                                e.printStackTrace();
                                userMonoSink.error(e);
                            }
                        }));
    }

    /**
     * Setup a stripe setup session to allow a client to add a card to an existing user.
     *
     * @param userId The id of the user for which a card should be added
     * @return A stripe session id that a client can use to call stripe API and add card details
     * @throws StripeException in case the card session setup fails
     */
    public Mono<String> createSetupCardSession(String userId) {
        return userService.readUser(userId)
                .flatMap(user -> Mono.create(monoSink -> {
                    SessionCreateParams params =
                            SessionCreateParams.builder()
                                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                                    .setMode(SessionCreateParams.Mode.SETUP)
                                    .addExpand("setup_intent")
                                    .setCustomer(user.getStripeCustomerId())
                                    .build();
                    try {
                        Session session = Session.create(params);
                        monoSink.success(session.getId());
                    } catch (StripeException e) {
                        monoSink.error(e);
                    }
                }));
    }

    /**
     * Create a Stripe card setup intent to add a new card to a user
     *
     * @param userId the id of the user to add the new card to
     * @return A Mono emitting the created card
     * @throws StripeException in case creating a setup intent fails
     */
    public Mono<Card> createSetupCardIntent(String userId) {
        return userService.readUser(userId)
                .flatMap(user -> Mono.create(monoSink -> {
                    try {
                        SetupIntentCreateParams setupIntentParams = SetupIntentCreateParams.builder()
                                .setCustomer(user.getStripeCustomerId())
                                .build();
                        SetupIntent setupIntent = SetupIntent.create(setupIntentParams);
                        monoSink.success(new de.bbqb.backend.api.model.entity.Card(setupIntent.getClientSecret()));
                    } catch (StripeException e) {
                        monoSink.error(e);
                    }
                }));
    }

    /**
     * Create a stripe payment intent to pay with a credit card
     *
     * @param userId          card owner
     * @param amount          the amount to charge
     * @param paymentMethodId the id of the payment method(card)
     * @return A Mono emitting a payment object containing the payment intent and payment information
     * @throws Exception       in case no payment method with the given id was found
     * @throws StripeException in case communication with stripe fails
     */
    public Mono<Payment> createCardPaymentIntent(String userId, Long amount, String paymentMethodId) {
        return userService.readUser(userId)
                .flatMap(user -> Mono.create(monoSink -> {
                    try {
                        PaymentMethodListParams params =
                                PaymentMethodListParams.builder()
                                        .setCustomer(user.getStripeCustomerId())
                                        .setType(PaymentMethodListParams.Type.CARD)
                                        .build();
                        // Retrieve all card payment methods for this user
                        PaymentMethodCollection paymentMethods = PaymentMethod.list(params); // May throw StripeException

                        // Check if the paymentMethodId is valid for the selected user
                        if (paymentMethods.getData().stream().anyMatch(paymentMethod -> paymentMethod.getId().contentEquals(paymentMethodId))) {
                            PaymentIntentCreateParams paymentIntentParams = PaymentIntentCreateParams.builder()
                                    .setCustomer(user.getStripeCustomerId())
                                    .setCurrency("eur")
                                    .setAmount(amount)
                                    .setReceiptEmail(user.getEmail())
                                    .setPaymentMethod(paymentMethodId)
                                    .setConfirm(true) // TODO: Check if it's necessary to confirm on the server side
                                    .setOffSession(true) // With this set to true PaymentIntent throws an error if authentication is required!
                                    .build();
                            PaymentIntent paymentIntent = PaymentIntent.create(paymentIntentParams); // May throw StripeException

                            monoSink.success(
                                    new Payment(
                                            paymentIntent.getId(),
                                            paymentIntent.getClientSecret(),
                                            paymentMethodId,
                                            paymentIntent.getAmount(),
                                            "germany", // The country where the service is deliverd. Currently hard coded but can be read from the device document
                                            paymentIntent.getCurrency(),
                                            "BBQ BUTLER Miete"));
                        } else {
                            monoSink.error(new Exception("No payment method with id " + paymentMethodId + " found for user " + user.getId()));
                        }
                    } catch (StripeException e) {
                        monoSink.error(e);
                    }
                }));
    }

    /**
     * Delete a users credit card
     *
     * @param cardId
     * @param userId The id of the user owning the card
     * @return A Mono emitting the deleted card
     */
    public Mono<Card> deleteCard(String cardId, String userId) {
        // TODO: validate if the customer has no open subscriptions if your about to delete the last remaining card otherwise fail
        return userService.readUser(userId)
                .flatMap(user ->
                        // TODO: Validate if the user is the customer associated with the card
                        Mono.create(monoSink -> {
                            try {
                                PaymentMethod paymentMethod =
                                        PaymentMethod.retrieve(cardId);

                                PaymentMethod updatedPaymentMethod =
                                        paymentMethod.detach();
                                monoSink.success(new de.bbqb.backend.api.model.entity.Card(
                                        paymentMethod.getId(),
                                        null,
                                        paymentMethod.getCard().getBrand(),
                                        paymentMethod.getCard().getExpMonth(),
                                        paymentMethod.getCard().getExpYear(),
                                        paymentMethod.getCard().getLast4()));
                            } catch (StripeException e) {
                                monoSink.error(e);
                            }
                        }));
    }

    // TODO: Test the return value if no cards are found
    /**
     * Read all cards for a particular user
     *
     * @param userId card owner. Must not be null.
     * @return A Mono emitting a list of cards owned by the user with id userId or Mono.empty if no user was found.
     * @throws IllegalAccessException in case the given userId is null
     */
    public Mono<List<Card>> readCards(String userId) {
        return userService.readUser(userId)
                .flatMap(user ->
                        Mono.create(cardMonoSink -> {
                            Map<String, Object> params = new HashMap<>();
                            params.put("customer", user.getStripeCustomerId());
                            params.put("type", "card");
                            try {
                                PaymentMethodCollection paymentMethods =
                                        PaymentMethod.list(params);
                                cardMonoSink.success(
                                        paymentMethods.getData()
                                                .stream()
                                                .map(paymentSource -> {
                                                    PaymentMethod.Card card = paymentSource.getCard();
                                                    return new Card(
                                                            paymentSource.getId(),
                                                            null,
                                                            card.getBrand(),
                                                            card.getExpMonth(),
                                                            card.getExpYear(),
                                                            card.getLast4());
                                                })
                                                .collect(Collectors.toList()));
                            } catch (StripeException e) {
                                cardMonoSink.error(e);
                            }
                        }));
    }
}
