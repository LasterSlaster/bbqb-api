package de.bbqb.backend.gcp.firestore.document;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.cloud.gcp.data.firestore.Document;

/**
 * Booking bean to represent a booking document from a gcp firestore database.
 * This class is used for Jackson un-/marshaling
 *
 * @author Marius Degen
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collectionName = "bookings")
public class BookingDoc {
    @DocumentId
    private String id;
    private String paymentIntentId;
    private String deviceId;
    // TODO: UserId: store reference here
    private String userId;
    private String status;
    private Timestamp requestTime;
    private Timestamp sessionStart;
    private Integer timeslot;
}
