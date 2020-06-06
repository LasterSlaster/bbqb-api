package de.bbqb.backend.gcp.firestore.document;

import org.springframework.cloud.gcp.data.firestore.Document;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.PropertyName;

@Document(collectionName="devicesCollection")
public class DeviceDoc {

	@DocumentId
	private String id;
	private String number;
	private String publishTime;
	private String status;
	private String location; //TODO: Change location address type
	private String address;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	//@PropertyName("number")
	public String getNumber() {
		return number;
	}
	//@PropertyName("number")
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
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	
}
