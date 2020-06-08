package de.bbqb.backend.api.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Stream;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudiot.v1.CloudIot;
import com.google.api.services.cloudiot.v1.CloudIotScopes;
import com.google.api.services.cloudiot.v1.model.SendCommandToDeviceRequest;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// import de.bbqb.backend.api.ApiApplication.PubsubOutboundGateway;
import de.bbqb.backend.api.model.entity.Device;
import de.bbqb.backend.gcp.firestore.DeviceRepo;
import de.bbqb.backend.gcp.firestore.document.DeviceDoc;

@Service
public class FirestoreDeviceService implements DeviceService {

	private DeviceRepo deviceRepo; // TODO: Think about making this static

	// @Autowired
	// private PubsubOutboundGateway messagingGateway; // TODO: Move this to deviceService or something

	@Override
	public Device createDevice(Device device) {

		DeviceDoc deviceDoc = mapToDeviceDoc(device);

		return mapFromDeviceDoc(deviceRepo.save(deviceDoc).block());
	}

	@Override
	public Device updateDevice(Device device) {
		// TODO: Implement updating device inclusive sending messages to device(iot) via
		// gcp pub/sub
		return new Device();
	}

	public void sendMessage(String message) {
		String projectId = "bbqb-prd";
		String cloudRegion = "europe-west1";
		String registryName = "bbqb-iot-registry";
		String deviceId = "butler-2";

		try {
			final String devicePath = String.format(
			"projects/%s/locations/%s/registries/%s/devices/%s",
			projectId, cloudRegion, registryName, deviceId);

			GoogleCredentials credential = GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
			JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
			HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
			final CloudIot service =
				new CloudIot.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, init)
					.setApplicationName(projectId)
					.build();

			SendCommandToDeviceRequest req = new SendCommandToDeviceRequest();
			Base64.Encoder encoder = Base64.getEncoder();
			String encPayload = encoder.encodeToString(message.getBytes(StandardCharsets.UTF_8.name()));
			req.setBinaryData(encPayload);
			System.out.printf("Sending command to %s%n", devicePath);

			service
				.projects()
				.locations()
				.registries()
				.devices()
				.sendCommandToDevice(devicePath, req)
				.execute();
			System.out.println("Command response: sent");
		} catch (Exception e) {
			System.out.println(e.getMessage() + e.getStackTrace());
		}
		// messagingGateway.sendToPubsub(message); // TODO: Update message payload
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
