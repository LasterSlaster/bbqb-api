package de.bbqb.backend.api.model.service;

import java.util.stream.Stream;

import de.bbqb.backend.api.model.entity.Device;

public interface DeviceService {

	public Device createDevice(Device device);
	
	public Device updateDevice(Device device);
	
	public Device readDevice(String deviceId);
	
	public Stream<Device> readAllDevices();
	
	public void openDevice(Device device); // TODO: Add return type
	
	public void lockDevice(Device device);

}
