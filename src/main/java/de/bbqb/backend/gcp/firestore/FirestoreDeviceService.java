package de.bbqb.backend.gcp.firestore;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudiot.v1.CloudIot;
import com.google.api.services.cloudiot.v1.CloudIotScopes;
import com.google.api.services.cloudiot.v1.model.SendCommandToDeviceRequest;
import com.google.api.services.cloudiot.v1.model.SendCommandToDeviceResponse;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.GeoPoint;
import de.bbqb.backend.api.model.entity.Address;
import de.bbqb.backend.api.model.entity.Device;
import de.bbqb.backend.api.model.entity.Location;
import de.bbqb.backend.api.model.service.DeviceService;
import de.bbqb.backend.gcp.firestore.document.DeviceDoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

// TODO: Implement business layer and separate business logic from external systems like REST and DB(firestore,pubsub,iot)
// TODO: Move Documentation to Interface

/**
 * Device service to retrieve device information from a gcp firestore nosql db
 *
 * @author Marius Degen
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

    /**
     * Send the open signal to the IoT-Device {@code device}
     * TODO: THink rethrowing the error and changing return type
     * TODO: THink about movin this method to a separate Class
     *
     * @param deviceId The device to send the signal to.
     *                 The IoT-Device is evaluated by its id.
     * @return true if signal was send successfully to device otherwise false
     */
    @Override
    public Mono<Void> openDevice(String deviceId) {
        return Mono.fromCallable(() -> {
            final String devicePath = String.format("projects/%s/locations/%s/registries/%s/devices/%s", gcpProjectId,
                    cloudRegion, registryName, deviceId);

            GoogleCredentials credential;
            try {
                credential = GoogleCredentials.getApplicationDefault().createScoped(CloudIotScopes.all());
            } catch (IOException e) {
                // For development deploys
                // Try to load GCP credentials file from classpath (resources folder)
                credential = GoogleCredentials.fromStream(new ClassPathResource("bbqb-prd-a6d055683b57.json").getInputStream()).createScoped(CloudIotScopes.all());
            }
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            HttpRequestInitializer init = new HttpCredentialsAdapter(credential);
            final CloudIot service = new CloudIot.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory,
                    init).setApplicationName(gcpProjectId).build();

            SendCommandToDeviceRequest req = new SendCommandToDeviceRequest();
            Base64.Encoder encoder = Base64.getEncoder();
            String encPayload = encoder.encodeToString(this.openDeviceMessage.getBytes(StandardCharsets.UTF_8.name()));
            req.setBinaryData(encPayload);
            LOGGER.info("Sending command to " + devicePath);

            SendCommandToDeviceResponse response = service.projects().locations().registries().devices().sendCommandToDevice(devicePath, req).execute();
            LOGGER.info("Command response: sent");
            LOGGER.info("Response is :" + response.toString());
            return null;
        });
    }

    /**
     * Send the lock signal to the IoT-Device specified by {@code device}
     *
     * @param device The device to send the signal to.
     *               The IoT-Device is evaluated by its id.
     */
    @Override
    public Boolean lockDevice(Device device) {
        return false;
    }

    /**
     * Create a new Device with the information from device.
     *
     * @param device The device to be saved. Must not be null.
     * @return Mono emitting the saved device
     * @throws {@link IllegalArgumentException} in case the given entity is null.
     */
    @Override
    public Mono<Device> createDevice(Device device) {
        // Auto-generate an unique id for the new firestore device document
        // This makes sure that a new document is created and no existing one is overridden
        String id = firestore.collection("devices").document().getId();
        DeviceDoc deviceDoc = new DeviceDoc();
        deviceDoc.setId(id);
        deviceDoc.setLocked(true);
        deviceDoc.setPublishTime(Timestamp.now());
        // TODO: Set lockSTatus, drawerStatus, timestamp, temp, wifi defaults???
        return deviceRepo.save(mapToDeviceDoc(device, deviceDoc)).map(this::mapFromDeviceDoc);
    }

    /**
     * Updates an existing device document with the information
     * from device otherwise creates a new one.
     *
     * @param device update the device with its information. Must not be null.
     * @return Mono emitting the updated device
     * @throws {@link IllegalArgumentException} in case the given entity is null.
     */
    @Override
    public Mono<Device> updateDevice(Device device) {
        // TODO: Wrap read, update, save into a transaction
        return deviceRepo.findById(device.getId())
                .map(deviceDoc -> this.mapToDeviceDoc(device, deviceDoc))
                .flatMap(deviceRepo::save) // if existing(not Empty) update deviceDocument
                .map(this::mapFromDeviceDoc)
                .switchIfEmpty(this.createDevice(device)); // otherwise create a new DeviceDoc
    }

    /**
     * Read a single device specified by the device id
     *
     * @param deviceId The id of the device to read. Must not be null.
     * @return Mono emitting the device specified by device id or Mono.empty if none found.
     * @throws {@link IllegalArgumentException} in case the given id is null.
     */
    @Override
    public Mono<Device> readDevice(String deviceId) {
        return deviceRepo.findById(deviceId).map(this::mapFromDeviceDoc);
    }

    /**
     * Returns all devices.
     *
     * @return Flux emitting all devices
     */
    @Override
    public Flux<Device> readAllDevices() {
        return deviceRepo.findAll().map(this::mapFromDeviceDoc);
    }

    /**
     * Map a Device object to a DeviceDoc object.
     * Mutates the parameter deviceDoc!
     *
     * @param device    The device to map to a DeviceDoc
     * @param deviceDoc device document which will be updated with the information from device
     * @return A DeviceDoc with the information from device
     */
    private DeviceDoc mapToDeviceDoc(Device device, DeviceDoc deviceDoc) {
        if (device.getDeviceId() != null) {
            deviceDoc.setDeviceId(device.getDeviceId());
        }
        if (device.getNumber() != null) {
            deviceDoc.setNumber(device.getNumber());
        }
        if (device.getAddress() != null) {
            if (device.getAddress().getName() != null) {
                deviceDoc.setAddressName(device.getAddress().getName());
            }
            if (device.getAddress().getCountry() != null) {
                deviceDoc.setCountry(device.getAddress().getCountry());
            }
            if (device.getAddress().getCity() != null) {
                deviceDoc.setCity(device.getAddress().getCity());
            }
            if (device.getAddress().getPostalcode() != null) {
                deviceDoc.setPostalCode(device.getAddress().getPostalcode());
            }
            if (device.getAddress().getStreet() != null) {
                deviceDoc.setStreet(device.getAddress().getStreet());
            }
            if (device.getAddress().getHouseNumber() != null) {
                deviceDoc.setHouseNumber(device.getAddress().getHouseNumber());
            }
        }
        if (device.getLocation() != null) {
            deviceDoc.setLocation(new GeoPoint(device.getLocation().getLatitude(), device.getLocation().getLongitude()));
        }
        return deviceDoc;
    }

    /**
     * Map a DeviceDoc object to a Device object.
     *
     * @param deviceDoc The DeviceDoc to map to a Device
     * @return A device with the information from deviceDoc
     */
    private Device mapFromDeviceDoc(DeviceDoc deviceDoc) {
        Location location = new Location(deviceDoc.getLocation().getLatitude(),
                deviceDoc.getLocation().getLongitude());
        Address address = new Address(deviceDoc.getCountry(), deviceDoc.getPostalCode(), deviceDoc.getCity(),
                deviceDoc.getStreet(), deviceDoc.getHouseNumber(), deviceDoc.getAddressName());
        return new Device(deviceDoc.getId(), deviceDoc.getDeviceId(), deviceDoc.getNumber(),
                convertToMilliseconds(deviceDoc.getPublishTime().getSeconds(), (long) deviceDoc.getPublishTime().getNanos()),
                deviceDoc.getLocked(), deviceDoc.getClosed(), deviceDoc.getWifiSignal(), deviceDoc.getIsTemperaturePlate1(), deviceDoc.getIsTemperaturePlate2(), location, address);
    }

    /**
     * Convert seconds and nanoseconds part to milliseconds since January 1, 1970, 00:00:00 UTC
     *
     * @param seconds seconds since Unix epoche
     * @param nanos   nanoseconds part of seconds
     * @return milliseconds since January 1, 1970, 00:00:00 UTC
     */
    private Long convertToMilliseconds(Long seconds, Long nanos) {
        return seconds * 1000 + nanos / 1000000;
    }
}