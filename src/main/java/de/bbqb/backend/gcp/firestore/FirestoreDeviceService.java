package de.bbqb.backend.gcp.firestore;

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
import de.bbqb.backend.api.model.entity.Address;
import de.bbqb.backend.api.model.entity.Device;
import de.bbqb.backend.api.model.entity.Location;
import de.bbqb.backend.api.model.service.DeviceService;
import de.bbqb.backend.gcp.firestore.document.DeviceDoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

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
     *
     * @param device The device to send the signal to.
     *               The IoT-Device is evaluated by its id.
     */
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

    /**
     * Send the lock signal to the IoT-Device specified by {@code device}
     *
     * @param device The device to send the signal to.
     *               The IoT-Device is evaluated by its id.
     */
    @Override
    public void lockDevice(Device device) {
        // TODO Auto-generated method stub
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
        Device deviceWithId = new Device(id, device);

        return deviceRepo.save(mapToDeviceDoc(deviceWithId)).map((DeviceDoc deviceDoc) -> {
            return mapFromDeviceDoc(deviceDoc);
        });
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
        // TODO: First try to read the device from the db and then only update valid parts if present(Transaction)
        return deviceRepo.save(mapToDeviceDoc(device)).map((DeviceDoc deviceDoc) -> {
            return mapFromDeviceDoc(deviceDoc);
        });
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
        return deviceRepo.findById(deviceId).map((DeviceDoc deviceDoc) -> {
            return mapFromDeviceDoc(deviceDoc);
        });
    }

    /**
     * Returns all devices.
     *
     * @return Flux emitting all devices
     */
    @Override
    public Flux<Device> readAllDevices() {
        return deviceRepo.findAll().map((DeviceDoc deviceDoc) -> {
            return mapFromDeviceDoc(deviceDoc);
        });
    }

    /**
     * Map a Device object to a DeviceDoc object.
     *
     * @param device The device to map to a DeviceDoc
     * @return A DeviceDoc with the information from device
     */
    private DeviceDoc mapToDeviceDoc(Device device) {
        Address address = device.getAddress();
        Location location = device.getLocation();

        DeviceDoc deviceDoc = new DeviceDoc(device.getId(), device.getDeviceId(), device.getName(), device.getNumber(),
                Timestamp.of(new Date(device.getPublishTime())), device.getStatus(),
                new GeoPoint(location.getLatitude(), location.getLongitude()), address.getName(),
                address.getStreet(), address.getHouseNumber(), address.getCity(), address.getPostalcode(),
                address.getCountry());

        return deviceDoc;
    }

    /**
     * Map a DeviceDoc object to a Device object.
     *
     * @param deviceDoc The DeviceDoc to map to a Device
     * @return ADevice with the information from deviceDoc
     */
    private Device mapFromDeviceDoc(DeviceDoc deviceDoc) {
        Location location = new Location(deviceDoc.getLocation().getLatitude(),
                deviceDoc.getLocation().getLongitude());
        Address address = new Address(deviceDoc.getCountry(), deviceDoc.getPostalCode(), deviceDoc.getCity(),
                deviceDoc.getStreet(), deviceDoc.getHouseNumber(), deviceDoc.getAddressName());
        return new Device(deviceDoc.getId(), deviceDoc.getDeviceId(), deviceDoc.getName(), deviceDoc.getNumber(),
                convertToMilliseconds(deviceDoc.getPublishTime().getSeconds(), Long.valueOf(deviceDoc.getPublishTime().getNanos())),
                deviceDoc.getStatus(), location, address);
    }

    /**
     * Convert seconds and nanoseconds part since January 1, 1970, 00:00:00 UTC
     *
     * @param seconds
     * @param nanos
     * @return milliseconds since January 1, 1970, 00:00:00 UTC
     */
    private Long convertToMilliseconds(Long seconds, Long nanos) {
        return seconds * 1000 + nanos / 1000000;
    }
}