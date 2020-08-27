package de.bbq.backend.gcp.firestore;

import com.google.cloud.firestore.Firestore;
import de.bbqb.backend.api.model.entity.Address;
import de.bbqb.backend.api.model.entity.Device;
import de.bbqb.backend.api.model.entity.Location;
import de.bbqb.backend.gcp.firestore.DeviceRepo;
import de.bbqb.backend.gcp.firestore.FirestoreDeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

@Disabled
public class FirestoreDeviceServiceTest {

    private final String id = "deviceId";
    private final String deviceId = "deviceId";
    private final String name = "name";
    private final String number = "deviceId";
    private final Long publishTime = new Date().getTime();
    private final String status = "deviceId";
    private final Address address = new Address("Deutschland", "78467", "Konstanz", "Straße", "2", "Adressname");
    private final Location location = new Location(1.1, 1.2);
    private FirestoreDeviceService sut;
    @Mock
    private DeviceRepo deviceRepoMock;
    @Mock
    private Firestore firestoreMock;

    @BeforeEach
    public void setUp() {
        this.sut = new FirestoreDeviceService(deviceRepoMock, firestoreMock);
    }

    @Test
    public void testCreateDevice() {
        // given
        Device device = new Device(id, deviceId, name, number, publishTime, status, location, address);

        // when
        //Mono<Device> savedDevice = this.sut.createDevice(device);

        // then
        //Device actualSavedDevice = savedDevice.block();
        //actualSavedDevice.equals(device);
    }

    @Test
    public void testUpdateDevice() {
        // given
        Device device = new Device(id, deviceId, name, number, publishTime, status, location, address);

        // when
        Mono<Device> updatedDevice = this.sut.updateDevice(device);

        // then
        Device actualUpdatedDevice = updatedDevice.block();
        actualUpdatedDevice.equals(device);
    }

    //@Test
    public void testReadDevice() {
        // given
        String deviceId = "Butler-2";

        // when
        Mono<Device> readDevice = this.sut.readDevice(deviceId);

        // then
        Device actualReadDevice = readDevice.block();
    }

    @Test
    public void testReadAllDevice() {
        // given

        // when
        Flux<Device> allDevices = this.sut.readAllDevices();

        // then
    }

    @Test
    public void testOpenDevice() {
        // given

        // when
        this.sut.openDevice(deviceId);

        // then
    }

    @Test
    public void testLockDevice() {
        // given
        Device device = new Device(id, deviceId, name, number, publishTime, status, location, address);

        // when
        this.sut.lockDevice(device);

        // then
    }
}
