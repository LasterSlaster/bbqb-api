package de.bbqb.backend.api.model.entity;

import java.util.Date;

/**
 * BBQ-Butler business object to hold device information
 * 
 * @author laster
 */
public class Device {

	private String id;
	private Integer number;
	private Date publishTime;
	private String status; // TODO: Change to Enum?
	private Location location;
	private Address address;

	public Device(String id, Integer number, Date publishTime, String status, Location location, Address address) {
		super();
		this.id = id;
		this.number = number;
		this.publishTime = publishTime;
		this.status = status;
		this.location = location;
		this.address = address;
	}
	
	public Device(String id, Device device) {
		this(id, device.getNumber(), device.getPublishTime(), device.getStatus(), device.getLocation(), device.getAddress());
	}

	public String getId() {
		return id;
	}

	public Integer getNumber() {
		return number;
	}

	public Date getPublishTime() {
		return publishTime;
	}

	public String getStatus() {
		return status;
	}

	public Location getLocation() {
		return location;
	}

	public Address getAddress() {
		return address;
	}
}