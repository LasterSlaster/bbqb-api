package de.bbqb.backend.gcp.firestore.document;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.cloud.gcp.data.firestore.Document;

/**
 * User bean to represent a user document from a gcp firestore database.
 * This class is used for Jackson un-/marshaling
 *
 * @author Marius Degen
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collectionName = "users")
public class UserDoc {
    @DocumentId
    private String id;
    private String stripeCustomerId;
    private String firstName;
    private String lastName;
    private String email;
}
