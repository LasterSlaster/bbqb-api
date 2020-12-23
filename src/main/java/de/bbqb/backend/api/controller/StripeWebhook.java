package de.bbqb.backend.api.controller;

import com.google.gson.JsonSyntaxException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import de.bbqb.backend.api.model.entity.Booking;
import de.bbqb.backend.api.model.entity.Device;
import de.bbqb.backend.api.model.service.BookingService;
import de.bbqb.backend.api.model.service.DeviceService;
import de.bbqb.backend.gcp.firestore.document.BookingDoc;
import de.bbqb.backend.stripe.StripeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

// TODO: Configure the types of events stripe should send to this endpoint on their webpage

/**
 * Controller providing an Endpoint for Stripes Webhook
 * to handle Stripe events.
 *
 * @author Marius Degen
 */
@CrossOrigin(origins = "*") // CORS configuration to allow all for the endpoints in this controller
@Controller
@RequestMapping("/stripe")
public class StripeWebhook {

    private static final Logger LOGGER = LoggerFactory.getLogger(StripeWebhook.class);

    private BookingService bookingService;
    private DeviceService deviceService;
    private StripeService stripeService;
    private String endpointSecret;
    private List<String> validStripeIPs;

    public StripeWebhook(@Value("${bbq.backend.stripe.endpointsecret}") String endpointSecret,
                         @Qualifier("validStripeIps") List<String> validStripeIPs,
                         BookingService bookingService,
                         DeviceService deviceService,
                         StripeService stripeService) {
        this.stripeService = stripeService;
        this.endpointSecret = endpointSecret;
        this.validStripeIPs = validStripeIPs;
        this.bookingService = bookingService;
        this.deviceService = deviceService;
    }

    // TODO: Must be idempotent. Could be done by logging events in a db and checking them on every request. The order of events might not be in the order they occurred
    @PostMapping("/webhook")
    public ResponseEntity handleStripeEvent(HttpServletRequest request) {
        // Validate client IP
        // TODO: Move the ip validation to class AppSecurityConfig
        if (!this.validStripeIPs.contains(request.getRemoteAddr())) {
            LOGGER.warn("Request came from an unknown IP " + request.getRemoteAddr());
            return ResponseEntity.badRequest().build();
        }

        // Validate signature and read event from request body
        Event event = null;
        try {
            String sigHeader = request.getHeader("Stripe-Signature");
            String payload = request.getReader().lines().collect(Collectors.joining("\n"));
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (JsonSyntaxException e) {
            // Invalid payload
            LOGGER.warn("Invalid payload: StripeObject deserialization failed!");
            return ResponseEntity.badRequest().build();
        } catch (SignatureVerificationException e) {
            // Invalid signature
            LOGGER.warn("Invalid signature: Request signature could not be verified with current endpoint secret!");
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            LOGGER.warn("Unable to read request body!");
            return ResponseEntity.badRequest().build();
        }

        // Deserialize the nested object inside the event
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            // Deserialization failed, probably due to an API version mismatch.
            // Refer to the Javadoc documentation on `EventDataObjectDeserializer` for
            // instructions on how to handle this case, or return an error here.
            LOGGER.warn("StripeObject deserialization failed!");
            return ResponseEntity.badRequest().build();
        }

        switch (event.getType()) {
            case "card.issuing_card.created":
                LOGGER.info("Received event of type card.issuing_card.created");
                //Card card = (Card) stripeObject;
                break;
            case "payment_method.attached":
                LOGGER.info("Received event of type payment_method.attached");
                //PaymentMethod paymentMethod = (PaymentMethod) stripeObject;
                break;
            case "setup_intent.succeeded":
                LOGGER.info("Received event of type setup_intent.succeeded");
                //SetupIntent paymentIntent = (SetupIntent) stripeObject;
                break;
            case "setup_intent.setup_failed":
                LOGGER.info("Received event of type setup_intent.setup_failed");
                //SetupIntent paymentIntent = (SetupIntent) stripeObject;
                break;
            case "payment_intent.succeeded":
                LOGGER.info("Received event of type payment_intent.succeeded");
                PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
                // TODO: Query the db for a pending grill session document with this paymentIntent. If found set the grill session to active/create one and send the open signal to the bbqb-device
                this.bookingService.findBookingByPaymentIntentId(paymentIntent.getId())
                        .map(booking -> new Booking(
                                booking.getId(),
                                booking.getPaymentIntentId(),
                                booking.getDeviceId(),
                                booking.getUserId(),
                                "payed",
                                booking.getRequestTime(),
                                booking.getSessionStart(),
                                booking.getPayment(),
                                booking.getTimeslot()))
                        .flatMap(bookingService::updateBooking)
                        .flatMap(booking -> deviceService.readDevice(booking.getDeviceId()))
                        .flatMap(device ->
                                this.deviceService.openDevice(device.getDeviceId())
                                        .retry(3)
                                        .doOnError(e -> LOGGER.warn("Unable to open device " + device.getDeviceId())))
                        .subscribe()
                ;
                break;
            case "payment_intent.payment_failed":
                LOGGER.info("Received event of type payment_intent.payment_failed");
                // TODO: Query the db for a pending grill session document with this paymentIntent and if found set it to failed/delete it.
                PaymentIntent failedPaymentIntent = (PaymentIntent) stripeObject;
                this.bookingService.findBookingByPaymentIntentId(failedPaymentIntent.getId())
                        .map(booking -> new Booking(
                                booking.getId(),
                                booking.getPaymentIntentId(),
                                booking.getDeviceId(),
                                booking.getUserId(),
                                "payment_failed",
                                booking.getRequestTime(),
                                booking.getSessionStart(),
                                booking.getPayment(),
                                booking.getTimeslot()))
                        .flatMap(bookingService::updateBooking)
                        .flatMap(booking -> deviceService.readDevice(booking.getDeviceId()))
                        .flatMap(device -> deviceService.updateDevice(new Device(device.getId(),device.getDeviceId(),device.getNumber(),device.getPublishTime(), false, device.getLocked(), device.getClosed(), device.getWifiSignal(), device.getIsTemperaturePlate1(),device.getIsTemperaturePlate2(),device.getSetTemperaturePlate1(),device.getSetTemperaturePlate2(),device.getLocation(),device.getAddress())))
                        .subscribe();
                break;
            default:
                LOGGER.warn("Unhandled event type: " + event.getType());
        }

        return ResponseEntity.ok().build();
    }
}
