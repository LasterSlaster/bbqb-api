package de.bbqb.backend.api.model.entity;

// TODO: Implement toString/hashCode/equals method for all entity classes

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * BBQ-Butler business object to hold address information
 *
 * @author Marius Degen
 */
@Getter
@AllArgsConstructor
public class Address {

    private final String country;
    private final String postalcode;
    private final String city;
    private final String street;
    private final String houseNumber;
    private final String name;
}