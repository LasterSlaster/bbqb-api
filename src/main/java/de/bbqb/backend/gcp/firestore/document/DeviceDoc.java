package de.bbqb.backend.gcp.firestore.document;

import org.springframework.cloud.gcp.data.firestore.Document;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.PropertyName;

// TODO: Rework data types: Use Timestamp on firestore for publishtime(Date?), use DOuble for lat/lngt, status?, are number and id correct?
@Document(collectionName = "devicesCollection")
public class DeviceDoc {

	@DocumentId
	private String id;
	private String number;
	private String publishTime;
	private String status;
	private String longitude;
	private String latitude;
	private String addressName;
	private String street;
	private String houseNumber;
	private String city;
	private String postalCode;
	private String country;

	public DeviceDoc() {
	}

	public DeviceDoc(String id, String number, String publishTime, String status, String longitude, String latitude,
			String addressName, String street, String houseNumber, String city, String postalCode, String country) {
		super();
		this.id = id;
		this.number = number;
		this.publishTime = publishTime;
		this.status = status;
		this.longitude = longitude;
		this.latitude = latitude;
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

	// @PropertyName("number")
	public String getNumber() {
		return number;
	}

	// @PropertyName("number")
	public void setNumber(String number) {
		this.number = number;
	}

	public String getPublishTime() {
		return publishTime;
	}

	public void setPublishTime(String publishTime) {
		this.publishTime = publishTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
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
