package de.bbqb.backend.gcp.firestore;

import com.google.cloud.firestore.Firestore;
import de.bbqb.backend.api.model.entity.User;
import de.bbqb.backend.api.model.service.UserService;
import de.bbqb.backend.gcp.firestore.document.UserDoc;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service to manage user resources
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

    /**
     * Create a new user with the provided object
     *
     * @param user The user to be created. Must not be null
     * @throws IllegalAccessException in case the given user is null
     * @return Mono emitting the created user
     */
    @Override
    public Mono<User> createUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("user must not be null");
        }
        String id = firestore.collection("users").document().getId();
        UserDoc userDoc = new UserDoc(id, user.getStripeCustomerId(), user.getFirstName(), user.getLastName(),  user.getEmail());
        return this.repo.save(mapToUserDoc(user, userDoc)).map(this::mapFromUserDoc);
    }

    /**
     * Update a user with the provided user data.
     * The user to update is identified by user.getId()
     * If the provided user does not exists no write operation is performed.
     *
     * @param user The new values for the user identified by its id
     * @throws IllegalAccessException in case the given id is null
     * @return Mono emitting the updated user object or Mono.empty() if no user was found
     */
    @Override
    public Mono<User> updateUser(User user) {
        return this.repo.findById(user.getId())
                .map(userDoc -> this.mapToUserDoc(user, userDoc))
                .flatMap(repo::save) // if existing(not Empty) update userDocument
                .map(this::mapFromUserDoc)
                .switchIfEmpty(this.createUser(user)); // otherwise create a new DeviceDoc
    }

    /**
     * Read a particular user by its id
     *
     * @param id must not be null
     * @throws IllegalAccessException in case the id is null
     * @return A Mono emitting a user or Mono.empty if none ist found
     */
    @Override
    public Mono<User> readUser(String id) {
        return this.repo.findById(id).map(this::mapFromUserDoc);
    }

    /**
     * Find all existing users
     *
     * @return A Flux emitting all users or Flux.empty if none ist found
     */
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
        return new User(userDoc.getId(), userDoc.getStripeCustomerId(), null, userDoc.getFirstName(), userDoc.getLastName(), userDoc.getEmail());
    }
}
