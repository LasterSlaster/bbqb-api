package de.bbqb.backend.gcp.firestore;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudiot.v1.CloudIot;
import com.google.api.services.cloudiot.v1.CloudIotScopes;
import com.google.api.services.cloudiot.v1.model.SendCommandToDeviceRequest;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.GeoPoint;

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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// TODO: Implement business layer and separate business logic from external systems like REST and DB(firestore,pubsub,iot)
/**
 * Device service to retrieve device information from a gcp firestore nosql db
 * 
 * @author Marius Degen
 *
 */
@Service
public class FirestoreDeviceService implements DeviceService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FirestoreDeviceService.class);

	private final DeviceRepo deviceRepo;
	
	private final Firestore firestore;
	
	@Value("${spring.cloud.gcp.project-id}")
	private String gcpProjectId;

	@Value("${bbq.backend.gcp.cloud-region}")
	private String cloudRegion;

	@Value("${bbq.backend.gcp.registry-name}")
	private String registryName;

	@Value("${bbq.backend.gcp.iot.message.open-device}")
	private String openDeviceMessage;

	public FirestoreDeviceService(DeviceRepo deviceRepo, Firestore firestore) {
		this.deviceRepo = deviceRepo;
		this.firestore = firestore;
	}

	@Override
	public void openDevice(Device device) {
		try {
			final String devicePath = String.format("projects/%s/locations/%s/registries/%s/devices/%s", gcpProjectId,
					cloudRegion, registryName, device.getDeviceId());

			GoogleCredentials credential = GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
			JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
			HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
			final CloudIot service = new CloudIot.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory,
					init).setApplicationName(gcpProjectId).build();

			SendCommandToDeviceRequest req = new SendCommandToDeviceRequest();
			Base64.Encoder encoder = Base64.getEncoder();
			String encPayload = encoder.encodeToString(this.openDeviceMessage.getBytes(StandardCharsets.UTF_8.name()));
			req.setBinaryData(encPayload);
			LOGGER.info("Sending command to %s%n", devicePath);

			service.projects().locations().registries().devices().sendCommandToDevice(devicePath, req).execute();
			LOGGER.info("Command response: sent");
		} catch (Exception e) {
			LOGGER.info(e.getMessage() + e.getStackTrace());
		}
	}

	@Override
	public void lockDevice(Device device) {
		// TODO Auto-generated method stub
	}

	@Override
	public Mono<Device> createDevice(Device device) {
		// Auto-generate an unique id for the new firestore device document
		// This makes sure that a new document is created and no existing one is overridden 
		String id = firestore.collection("devices").document().getId();
		Device deviceWithId = new Device(id, device);

		return deviceRepo.save(mapToDeviceDoc(deviceWithId)).map((DeviceDoc deviceDoc) -> {
			return mapFromDeviceDoc(deviceDoc);
		});
	}

	@Override
	public Mono<Device> updateDevice(Device device) {
		// updates an existing device otherwise creates a new one
		return deviceRepo.save(mapToDeviceDoc(device)).map((DeviceDoc deviceDoc) -> {
			return mapFromDeviceDoc(deviceDoc);
		});
	}

	@Override
	public Mono<Device> readDevice(String deviceId) {
		return deviceRepo.findById(deviceId).map((DeviceDoc deviceDoc) -> {
			return mapFromDeviceDoc(deviceDoc);
		});
	}

	@Override
	public Flux<Device> readAllDevices() {
		return deviceRepo.findAll().map((DeviceDoc deviceDoc) -> {
			return mapFromDeviceDoc(deviceDoc);
		});
	}

	private DeviceDoc mapToDeviceDoc(Device device) {
		Address address = device.getAddress();
		Location location = device.getLocation();

		DeviceDoc deviceDoc = new DeviceDoc(device.getId(), device.getDeviceId(), device.getNumber().toString(),
				Timestamp.of(new Date(device.getPublishTime())), device.getStatus(),
				new GeoPoint(location.getLatitude(), location.getLongitude()), address.getName(),
				address.getStreet(), address.getHouseNumber(), address.getCity(), address.getPostalcode(),
				address.getCountry());

		return deviceDoc;
	}

	private Device mapFromDeviceDoc(DeviceDoc deviceDoc) {
		Location location = new Location(deviceDoc.getLocation().getLatitude(),
				deviceDoc.getLocation().getLongitude());
		Address address = new Address(deviceDoc.getCountry(), deviceDoc.getPostalCode(), deviceDoc.getCity(),
				deviceDoc.getStreet(), deviceDoc.getHouseNumber(), deviceDoc.getAddressName());
		Device device = new Device(deviceDoc.getId(), deviceDoc.getDeviceId(), deviceDoc.getNumber(),
				deviceDoc.getPublishTime().getSeconds() * 1000,
				deviceDoc.getStatus(), location, address);

		return device;
	}

}