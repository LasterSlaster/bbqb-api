package de.bbqb.backend.gcp.firestore;

import com.google.cloud.firestore.Firestore;
import de.bbqb.backend.api.model.entity.User;
import de.bbqb.backend.api.model.service.UserService;
import de.bbqb.backend.gcp.firestore.document.UserDoc;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementation of a UserService to manage user information
 * in a GCP Firestore.
 *
 * @author Marius Degen
 */
@Service
public class FirestoreUserService implements UserService {

    private UserRepo repo;
    private Firestore firestore;

    public FirestoreUserService(UserRepo repo, Firestore firestore) {
       this.repo = repo;
       this.firestore = firestore;
    }

    @Override
    public Mono<User> createUser(User user) {
        String id = firestore.collection("users").document().getId();
        UserDoc userDoc = new UserDoc(id, user.getStripeCustomerId(), user.getFirstName(), user.getLastName(),  user.getEmail());
        return this.repo.save(mapToUserDoc(user, userDoc)).map(this::mapFromUserDoc);
    }

    @Override
    public Mono<User> updateUser(User user) {
        return this.repo.findById(user.getId())
                .map(userDoc -> this.mapToUserDoc(user, userDoc))
                .flatMap(repo::save) // if existing(not Empty) update userDocument
                .map(this::mapFromUserDoc)
                .switchIfEmpty(this.createUser(user)); // otherwise create a new DeviceDoc
    }

    @Override
    public Mono<User> readUser(String id) {
        return this.repo.findById(id).map(this::mapFromUserDoc);
    }

    @Override
    public Flux<User> readAllUsers() {
        return this.repo.findAll().map(this::mapFromUserDoc);
    }

    private UserDoc mapToUserDoc(User user, UserDoc userDoc) {
        if (user.getId() != null) {
            userDoc.setId(user.getId());
        }
        if (user.getEmail() != null) {
            userDoc.setEmail(user.getEmail());
        }
        if (user.getFirstName() != null) {
            userDoc.setFirstName(user.getFirstName());
        }
        if (user.getLastName() != null) {
            userDoc.setLastName(user.getLastName());
        }
        if (user.getStripeCustomerId() != null) {
            userDoc.setStripeCustomerId(user.getStripeCustomerId());
        }
        return userDoc;
    }

    private User mapFromUserDoc(UserDoc userDoc) {
        return new User(userDoc.getId(), userDoc.getFirstName(), userDoc.getLastName(), userDoc.getStripeCustomerId(), userDoc.getEmail());
    }
}
