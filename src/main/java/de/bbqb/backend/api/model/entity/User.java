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

    private  String id;
    private  String stripeCustomerId;
    private  String lastBookingId;
    private  String firstName;
    private  String lastName;
    private  String email;
}
