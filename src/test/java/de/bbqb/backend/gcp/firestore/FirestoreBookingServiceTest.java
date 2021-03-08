package de.bbqb.backend.gcp.firestore;

import com.google.cloud.firestore.Firestore;
import de.bbqb.backend.api.ApiApplication;
import de.bbqb.backend.api.model.entity.Booking;
import de.bbqb.backend.api.model.service.DeviceService;
import de.bbqb.backend.api.model.service.UserService;
import de.bbqb.backend.gcp.firestore.BookingRepo;
import de.bbqb.backend.gcp.firestore.FirestoreBookingService;
import de.bbqb.backend.stripe.StripeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.Comparator;
import java.util.List;

/**
 * @author Marius Degen
 */
@SpringBootTest(classes = {ApiApplication.class})
public class FirestoreBookingServiceTest {

    private FirestoreBookingService sut;

    @Autowired
    private BookingRepo bookingRepo;

    @Autowired
    private Firestore firestoreMock;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private UserService userService;

    @Autowired
    private StripeService stripeService;

    @BeforeEach
    public void setUp() {
        this.sut = new FirestoreBookingService(bookingRepo, firestoreMock, deviceService, userService, stripeService);
    }

    @Test
    public void testCreateDevice() {
        // given

        // when
        Flux<Booking> bookingDocs = this.sut.findAllBookingsByUserId("RpwfizsTZTeq6t1Usbq5DWk1o8I2");

        // then
        List<Booking> bookings = bookingDocs.sort(Comparator.comparing(Booking::getRequestTime, Comparator.reverseOrder())).buffer().next().block();
    }
}