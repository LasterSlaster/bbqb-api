package de.bbqb.backend.api.service;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.bbqb.backend.api.ApiApplication.PubsubOutboundGateway;
import de.bbqb.backend.api.model.entity.Device;
import de.bbqb.backend.gcp.firestore.DeviceRepo;
import de.bbqb.backend.gcp.firestore.document.DeviceDoc;

@Service
public class FirestoreDeviceService implements DeviceService{

	private DeviceRepo deviceRepo; // TODO: Think about making this static

	@Autowired
	private PubsubOutboundGateway messagingGateway; // TODO: Move this to deviceService or something

	
	@Override
	public Device createDevice(Device device) {

		DeviceDoc deviceDoc = mapToDeviceDoc(device);

		return mapFromDeviceDoc(deviceRepo.save(deviceDoc).block());	
	}
	
	
	@Override
	public Device updateDevice(Device device) {
		//TODO: Implement updating device inclusive sending messages to device(iot) via gcp pub/sub
		return new Device();
	}
	
	
	public void sendMessage(String message) {
		messagingGateway.sendToPubsub(message); // TODO: Update message payload
	}

	
	@Override
	public Device readDevice(String deviceId) {

		return mapFromDeviceDoc(deviceRepo.findById(deviceId).block());
	}
	

	// TODO: Add to interface and override
	public Stream<DeviceDoc> readAllDevices() {
		// TODO: Map to Device type and return Device stream
		return deviceRepo.findAll().toStream();
	}
	
	
	private DeviceDoc mapToDeviceDoc(Device device) {
		// TODO: Implement
		return new DeviceDoc();
	}
	
	
	private Device mapFromDeviceDoc(DeviceDoc deviceDoc) {
		// TODO: Implement
		return new Device();
	}
	
}
