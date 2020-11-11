package de.bbqb.backend.stripe;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.*;
import com.stripe.param.checkout.SessionCreateParams;
import de.bbqb.backend.api.model.entity.Payment;
import de.bbqb.backend.api.model.entity.User;
import de.bbqb.backend.api.model.entity.Card;
import de.bbqb.backend.api.model.service.CustomerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class StripeService implements CustomerService {

    public StripeService(@Value("${bbq.backend.stripe.apikey}") String apiKey) {
        Stripe.apiKey = apiKey;
    }

    public Mono<User> createCustomer(User user) {
        CustomerCreateParams customerParam = new CustomerCreateParams.Builder()
                .setEmail(user.getEmail())
                .setName(user.getFirstName() + " " + user.getLastName())
                .build();
        return Mono.create(userMonoSink -> {
            User newCustomer = null;
            try {
                Customer stripeCustomer = Customer.create(customerParam); // Blocking request
                newCustomer = new User(user.getId(), stripeCustomer.getId(), user.getFirstName(), user.getLastName(), user.getEmail());
                userMonoSink.success(newCustomer);
            } catch (StripeException e) {
                e.printStackTrace();
                userMonoSink.error(e);
            }
        });
    }

    /**
     * Setup a stripe setup session to allow a client to add a card to an existing user.
     *
     * @param user The user for which a card should be added
     * @return A stripe session id that a client can use to call stripe API and add card details
     */
    public Mono<String> createSetupCardSession(User user) {
        return Mono.create(monoSink -> {
            SessionCreateParams params =
                    SessionCreateParams.builder()
                            .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                            .setMode(SessionCreateParams.Mode.SETUP)
                            .addExpand("setup_intent") // TODO: Check if this is the correct identifier and if we have to process a setupintend anyway
                            .setCustomer(user.getStripeCustomerId())
                            // TODO: Check which values to insert here
                            .setSuccessUrl("https://example.com/success?session_id={CHECKOUT_SESSION_ID}")
                            .setCancelUrl("https://example.com/cancel")
                            .build();
            try {
                Session session = Session.create(params); // TODO: Check if this a blocking network call and if so think about creating a separate thread
                /*
                Mono blockingWrapper = Mono.fromCallable(() -> {
                    return callsomethingSynchronous;
                });
                blockingWrapper = blockingWrapper.subscribeOn(Schedulers.boundedElastic());
                */
                monoSink.success(session.getId());
            } catch (StripeException e) {
                monoSink.error(e);
            }
        });
    }

    public Mono<Card> createSetupCardIntent(User user) {
        return Mono.create(monoSink -> {
            try {
                SetupIntentCreateParams setupIntentParams = SetupIntentCreateParams.builder()
                        .setCustomer(user.getStripeCustomerId())
                        .build();
                SetupIntent setupIntent = SetupIntent.create(setupIntentParams);
                monoSink.success(new de.bbqb.backend.api.model.entity.Card(setupIntent.getClientSecret()));
            } catch (StripeException e) {
                monoSink.error(e);
            }
        });
    }

    public Mono<Payment> createCardPaymentIntent(User user, Long amount) {
        return Mono.create(monoSink -> {
            try {
                PaymentMethodListParams params =
                        PaymentMethodListParams.builder()
                                .setCustomer(user.getStripeCustomerId())
                                .setType(PaymentMethodListParams.Type.CARD)
                                .build();
                // Retrieve all card payment methods for this user
                PaymentMethodCollection paymentMethods = PaymentMethod.list(params);

                PaymentIntentCreateParams paymentIntentParams = PaymentIntentCreateParams.builder()
                        .setCustomer(user.getStripeCustomerId())
                        .setCurrency("eur")
                        .setAmount(amount)
                        // Use the first card payment method found for the payment
                        .setReceiptEmail(user.getEmail())
                        .setPaymentMethod(paymentMethods.getData().get(0).getId())
                        .setConfirm(true) // TODO: Check the effects of this setting
                        .setOffSession(true) // With this set to true PaymentIntent throws an error if authentication is required!
                        .build();
                PaymentIntent paymentIntent = PaymentIntent.create(paymentIntentParams);

                monoSink.success(
                        new Payment(
                                paymentIntent.getId(),
                                paymentIntent.getClientSecret(),
                                paymentIntent.getAmount(),
                                "germany", // The country where the service is deliverd. Currently hard coded but can be read from the device document
                                paymentIntent.getCurrency(),
                                "BBQ BUTLER Miete"));
            } catch (StripeException e) {
                monoSink.error(e);
            }
        });
    }

    // befor calling this method make sure the customer has no open subscritions if your about to delete the last remaining card
    public Mono<Card> deleteCard(String id, User user) {
        return Mono.create(monoSink -> {
            Map<String, Object> retrieveParams = new HashMap<>();
            List<String> expandList = new ArrayList<>();
            expandList.add("sources");
            retrieveParams.put("expand", expandList);
            try {
                Customer customer = Customer.retrieve(
                        user.getStripeCustomerId(),
                        retrieveParams,
                        null
                );
                com.stripe.model.Card card = (com.stripe.model.Card) customer.getSources().retrieve(id);
                com.stripe.model.Card deletedCard = card.delete();
                monoSink.success(new de.bbqb.backend.api.model.entity.Card(card.getId(), null, card.getBrand(), card.getExpMonth(), card.getExpYear(), card.getLast4()));
            } catch (StripeException e) {
                monoSink.error(e);
            }
        });
    }

    public Mono<List<Card>> readCards(User user) {
        return Mono.create(cardMonoSink -> {
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
                                    return new Card(paymentSource.getId(), null, card.getBrand(), card.getExpMonth(), card.getExpYear(), card.getLast4());
                                })
                                .collect(Collectors.toList()));
            } catch (StripeException e) {
                cardMonoSink.error(e);
            }
        });
    }
}
