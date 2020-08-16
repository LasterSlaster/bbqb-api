package de.bbqb.backend.api.model.entity;

// TODO: Implement toString method for all entity classes
/**
 * BBQ-Butler business object to hold address information
 * @author laster
 */
public class Address {

	private String country;
	private String postalcode;
	private String city;
	private String street;
	private String houseNumber;
	private String name;

	public Address(String country, String postalcode, String city, String street, String houseNumber, String name) {
		super();
		this.country = country;
		this.postalcode = postalcode;
		this.city = city;
		this.street = street;
		this.houseNumber = houseNumber;
		this.name = name;
	}

	public String getCountry() {
		return country;
	}
	public String getPostalcode() {
		return postalcode;
	}
	public String getCity() {
		return city;
	}
	public String getStreet() {
		return street;
	}
	public String getHouseNumber() {
		return houseNumber;
	}
	public String getName() {
		return name;
	}
}