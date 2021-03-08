package de.bbqb.backend.api.controller;

import de.bbqb.backend.api.model.entity.*;
import de.bbqb.backend.api.model.service.BookingService;
import de.bbqb.backend.api.model.service.DeviceService;
import de.bbqb.backend.api.model.service.UserService;
import de.bbqb.backend.stripe.StripeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
public class BookingControllerTest {

    BookingController sut;

    @Mock
    Authentication sub;
    @Mock
    DeviceService deviceService;
    @Mock
    UserService userService;
    @Mock
    StripeService stripeService;
    @Mock
    BookingService bookingService;

    @BeforeEach
    void setup() {
        this.sut = new BookingController(deviceService, userService, stripeService, bookingService);
    }

    @Test
    void testReadAllBookings() {
        //given
        Payment payment1 = new Payment("1", "clientSecret", "paymentMethodId", 1L, "country", "currentcy", "label");
        Booking result1 = new Booking("1", "intent", "bbqbId", "user", "status", new Date(), new Date(), payment1, 45);
        Payment payment2 = new Payment("2", "clientSecret", "paymentMethodId", 1L, "country", "currentcy", "label");
        Booking result2 = new Booking("2", "intent", "bbqbId", "user", "status", new Date(), new Date(), payment2, 45);

        when(sub.getName()).thenReturn("userId");
        when(bookingService.findAllBookingsByUserId(sub.getName())).thenReturn(Flux.just(result1, result2));

        //when
        List<Booking> bookings = this.sut.getBookings(sub).collectList().block();

        //then
        assertNotNull(bookings);
        // Assert that controller returns all existing bookings
        assertEquals(2, bookings.size());
        // Assert that returned bookings are the ones from the booking service
        assertTrue(bookings.stream().map(booking -> booking.getId()).collect(Collectors.toList()).equals(List.of("1", "2")));
    }

    @Test
    void testReadBooking() {
        //given
        String userId = "userId";
        String bookingId = "1";
        Payment payment = new Payment("1", "clientSecret", "paymentMethodId", 1L, "country", "currentcy", "label");
        Booking result = new Booking(bookingId, "intent", "bbqbId", userId, "status", new Date(), new Date(), payment, 45);

        when(sub.getName()).thenReturn(userId);
        when(bookingService.findBooking(bookingId, userId)).thenReturn(Mono.just(result));

        //when
        ResponseEntity<Booking> resultBooking = this.sut.getBooking(sub, "1").block();

        //then
        // Assert that the Response contains a booking
        assertNotNull(resultBooking);
        // Assert that returned booking is the one we searched for by id
        assertTrue(resultBooking.hasBody());
        assertTrue(resultBooking.getBody().getId().equals(bookingId));
    }

    @Test
    void testBookABBQB() {
        //given
        String deviceId = "bbqbId";
        Timeslot timeslot = Timeslot.FORTY_FIVE;
        String paymentMethodId = "stripeCreditCardId";
        String paymentIntentId = "payentIntentId";
        String userId = "userId";
        BookingRequest bookingRequest = new BookingRequest(deviceId, timeslot.getTime(), paymentMethodId);
        // BBQB to create a booking for
        Device bbqb = new Device("id",//TODO: Check if we search devices by database id(id) or Iot-Id(deviceId)
                deviceId,
                "number",
                1L,
                false,
                true,
                true,
                1d,
                1d,
                1d,
                1d,
                1d,
                new Location(1d,
                        2d),
                new Address("country",
                        "postalcode",
                        "city",
                        "street",
                        "houseNumber",
                        "name"));
        User currentUser = new User(userId,
                "stripeCustomerId",
                "lastBookingId",
                "firstName",
                "lastName",
                "email");
        Payment payment = new Payment("id", //TODO: Check if payment id is equal to paymentIntentId
                        "clientSecret",
                        paymentMethodId,
                        timeslot.getCost(),
                        "country",
                        "EUR",
                        "label");
        Booking booking = new Booking("id",
                paymentIntentId,
                deviceId,
                userId,
                "pending",
                new Date(),
                new Date(),
                payment,
                timeslot.getTime());

        when(deviceService.readDevice(deviceId)).thenReturn(Mono.just(bbqb));
        // When updating a device return the device which was requested for update
        when(deviceService.updateDevice(any())).thenAnswer(argument -> Mono.just((argument.getArgument(0))));
        when(sub.getName()).thenReturn(userId);
        when(userService.readUser(userId)).thenReturn(Mono.just(currentUser));
        when(stripeService.createCardPaymentIntent(userId, timeslot.getCost(), paymentMethodId)).thenReturn(Mono.just(payment));
        // When creating a booking return the booking which was requested for creation
        when(bookingService.createBooking(paymentMethodId, deviceId, userId, timeslot)).thenReturn(Mono.just(booking));

        //when
        ResponseEntity<Booking> resultBooking = this.sut.postBookings(sub, bookingRequest).block();

        //then
        assertNotNull(resultBooking);
        assertTrue(resultBooking.hasBody());
        assertTrue(resultBooking.getBody().getDeviceId().contentEquals(deviceId));
    }

}
