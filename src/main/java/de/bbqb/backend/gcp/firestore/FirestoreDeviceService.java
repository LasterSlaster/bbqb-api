package de.bbqb.backend.gcp.firestore;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.bbqb.backend.api.model.entity.Address;
import de.bbqb.backend.api.model.entity.Device;
import de.bbqb.backend.api.model.entity.Location;
import de.bbqb.backend.api.model.service.DeviceService;
import de.bbqb.backend.gcp.firestore.DeviceRepo;
import de.bbqb.backend.gcp.firestore.document.DeviceDoc;

// TODO: Implement business layer and separate business logic from external systems like REST and DB(firestore,pubsub,iot)
/**
 * Device service to retrieve device information from a gcp firestore nosql db
 * @author laster
 *
 */
@Service
public class FirestoreDeviceService implements DeviceService {

	private static final Logger logger = LoggerFactory.getLogger(FirestoreDeviceService.class);
	 
	private DeviceRepo deviceRepo; // TODO: Think about making this static

	@Value("${spring.cloud.gcp.project-id}")
	private String gcpProjectId;

	@Value("${bbq.backend.gcp.cloud-region}")
	private String cloudRegion;

	@Value("${bbq.backend.gcp.registry-name}")
	private String registryName;

	@Value("${bbq.backend.gcp.device-id}")
	private String deviceId;

	@Value("${bbq.backend.gcp.iot.message.open-device}")
	private String openDeviceMessage;

	@Override
	public Device createDevice(Device device) {
		DeviceDoc deviceDoc = mapToDeviceDoc(device);

		return mapFromDeviceDoc(deviceRepo.save(deviceDoc).block());
	}

	@Override
	public Device updateDevice(Device device) {
		return mapFromDeviceDoc(deviceRepo.save(mapToDeviceDoc(device)).block());
	}

	@Override
	public void openDevice(Device device) {
		try {
			final String devicePath = String.format(
					"projects/%s/locations/%s/registries/%s/devices/%s", gcpProjectId, cloudRegion, registryName, deviceId);

			GoogleCredentials credential = GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
			JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
			HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
			final CloudIot service =
				new CloudIot.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, init)
					.setApplicationName(gcpProjectId)
					.build();

			SendCommandToDeviceRequest req = new SendCommandToDeviceRequest();
			Base64.Encoder encoder = Base64.getEncoder();
			String encPayload = encoder.encodeToString(this.openDeviceMessage.getBytes(StandardCharsets.UTF_8.name()));
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
	}

	@Override
	public void lockDevice(Device device) {
		// TODO Auto-generated method stub
	}
	
	
	@Override
	public Device readDevice(String deviceId) {
		return mapFromDeviceDoc(deviceRepo.findById(deviceId).block());
	}
	

	// TODO: Add to interface and override
	public Stream<Device> readAllDevices() {
		return deviceRepo.findAll().toStream().map((DeviceDoc deviceDoc) -> {
				return mapFromDeviceDoc(deviceDoc);
			}
		);
	}
	
	
	private DeviceDoc mapToDeviceDoc(Device device) {
		Address address = device.getAddress();
		Location location = device.getLocation();

		DeviceDoc deviceDoc = new DeviceDoc(
				device.getId(), 
				device.getNumber().toString(), 
				device.getPublishTime().toString(), // TODO: Inject and use DateFormatter
				device.getStatus(), 
				location.getLongitude().toString(),
				location.getLatitude().toString(),
				address.getName(), 
				address.getStreet(),
				address.getHouseNumber(),
				address.getCity(),
				address.getPostalcode(),
				address.getCountry());

		return deviceDoc;
	}
	
	
	private Device mapFromDeviceDoc(DeviceDoc deviceDoc) {
		Location location = new Location(
				Double.valueOf(deviceDoc.getLatitude()), 
				Double.valueOf(deviceDoc.getLongitude()));
		Address address = new Address(deviceDoc.getCountry(), 
				deviceDoc.getPostalCode(), 
				deviceDoc.getCity(), 
				deviceDoc.getStreet(), 
				deviceDoc.getHouseNumber(), 
				deviceDoc.getAddressName()); 
		Device device = new Device(
				deviceDoc.getId(), 
				Integer.valueOf(deviceDoc.getNumber()), 
				new Date(deviceDoc.getPublishTime()), // TODO: Test this part to make sure that parsing time works correctly
				deviceDoc.getStatus(),
				location,
				address);

		return device;
	}

}