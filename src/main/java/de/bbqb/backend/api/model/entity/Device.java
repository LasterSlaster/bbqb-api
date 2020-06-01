package de.bbqb.backend.api.model.entity;

import java.util.Date;

public class Device {

	private String id;
	private Integer number;
	private Date publishTime;
	private String status; //TODO: Change to Enum?
	private Location location;
	private Address address;
	
	public Device() {} //TODO:Remove

	public Device(String id, Integer number, Date publishTime, String status, Location location, Address address) {
		super();
		this.id = id;
		this.number = number;
		this.publishTime = publishTime;
		this.status = status;
		this.location = location;
		this.address = address;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Integer getNumber() {
		return number;
	}
	public void setNumber(Integer number) {
		this.number = number;
	}
	public Date getPublishTime() {
		return publishTime;
	}
	public void setPublishTime(Date publishTime) {
		this.publishTime = publishTime;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
}