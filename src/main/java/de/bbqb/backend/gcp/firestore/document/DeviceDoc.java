package de.bbqb.backend.gcp.firestore.document;

import org.springframework.cloud.gcp.data.firestore.Document;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.PropertyName;

// TODO: Rework data types:  use geopoint for lat/lngt,status -> int or string?, are number and id correct?
@Document(collectionName = "devicesCollection")
public class DeviceDoc {

	@DocumentId
	private String id;
	private String deviceId;
	private String number;
	private Timestamp publishTime;
	private String status;
	private GeoPoint location;
	private String addressName;
	private String street;
	private String houseNumber;
	private String city;
	private String postalCode;
	private String country;

	public DeviceDoc() {
	}

	// TODO: Think about hinding TImestamp and GeoPoint dependencies
	public DeviceDoc(String id, String deviceId, String number, Timestamp publishTime, String status, GeoPoint location,
			String addressName, String street, String houseNumber, String city, String postalCode, String country) {
		super();
		this.id = id;
		this.deviceId = deviceId;
		this.number = number;
		this.publishTime = publishTime;
		this.status = status;
		this.location = location;
		this.addressName = addressName;
		this.street = street;
		this.houseNumber = houseNumber;
		this.city = city;
		this.postalCode = postalCode;
		this.country = status;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getDeviceId() {
		return deviceId;
	}
	
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	// @PropertyName("number")
	public String getNumber() {
		return number;
	}

	// @PropertyName("number")
	public void setNumber(String number) {
		this.number = number;
	}

	public Timestamp getPublishTime() {
		return publishTime;
	}

	public void setPublishTime(Timestamp publishTime) {
		this.publishTime = publishTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public GeoPoint getLocation() {
		return location;
	}

	public void setLocation(GeoPoint location) {
		this.location = location;
	}

	public String getAddressName() {
		return addressName;
	}

	public void setAddressName(String addressName) {
		this.addressName = addressName;
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

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

}
