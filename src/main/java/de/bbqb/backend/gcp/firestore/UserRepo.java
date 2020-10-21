package de.bbqb.backend.gcp.firestore;

import de.bbqb.backend.gcp.firestore.document.UserDoc;
import org.springframework.cloud.gcp.data.firestore.FirestoreReactiveRepository;

public interface UserRepo extends FirestoreReactiveRepository<UserDoc> {
}
