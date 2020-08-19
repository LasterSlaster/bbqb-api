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

	private String country;
	private String postalcode;
	private String city;
	private String street;
	private String houseNumber;
	private String name;
}