package de.bbqb.backend.api.model.entity;

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
	public void setCountry(String country) {
		this.country = country;
	}
	public String getPostalcode() {
		return postalcode;
	}
	public void setPostalcode(String postalcode) {
		this.postalcode = postalcode;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public String getHouseNumber() {
		return houseNumber;
	}
	public void setHouseNumber(String houseNumber) {
		this.houseNumber = houseNumber;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}