package de.bbqb.backend.api.service;

import de.bbqb.backend.api.model.entity.Device;

public interface DeviceService {

	public Device createDevice(Device device);
	
	public Device updateDevice(Device device);
	
	public Device readDevice(String deviceId);

}
