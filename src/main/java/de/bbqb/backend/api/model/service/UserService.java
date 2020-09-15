package de.bbqb.backend.api.model.service;

import de.bbqb.backend.api.model.entity.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Business logic to manage users
 *
 * @author Marius Degen
 */
public interface UserService {
    Mono<User> createUser(User user);
    Mono<User> updateUser(User user);
    Mono<User> readUser(String id);
    Flux<User> readAllUsers();
}
