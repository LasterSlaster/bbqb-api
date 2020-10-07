package de.bbqb.backend.api.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * BBQ-Butler business object to hold user information
 *
 * @author Marius Degen
 */
@Getter
@AllArgsConstructor
public class User {

    private final String id;
    private final String firstName;
    private final String lastName;
    private final String stripeId;
    private final String email;
    // private final Group group;
    // private final Role role;
}
