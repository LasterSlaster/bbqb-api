package de.bbqb.backend.api.model.entity;

/**
 * BBQ-Butler business object to hold device information
 * 
 * @author Marius Degen
 */
public class Device {

	private String id;
	private String deviceId;
	private String number; // TODO: Change to String?
	private Long publishTime; // TODO: Think about changing this datatype
	private String status; // TODO: Change to Enum?
	private Location location;
	private Address address;

	// required for jackson object creation during marshalling
	public Device () {
		super();
	}
	public Device(String id, String deviceId, String number, Long publishTime, String status, Location location, Address address) {
		super();
		this.id = id;
		this.deviceId = deviceId;
		this.number = number;
		this.publishTime = publishTime;
		this.status = status;
		this.location = location;
		this.address = address;
	}
	
	public Device(String id, Device device) {
		this(id, device.getDeviceId(), device.getNumber(), device.getPublishTime(), device.getStatus(), device.getLocation(), device.getAddress());
	}

	public String getId() {
		return id;
	}

	public String getNumber() {
		return number;
	}
	
	public String getDeviceId() {
		return deviceId;
	}
	
	public Long getPublishTime() {
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