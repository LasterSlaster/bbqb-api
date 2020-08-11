package de.bbq.backend.gcp.firestore;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.api.client.util.DateTime;

import de.bbqb.backend.api.model.entity.Address;
import de.bbqb.backend.api.model.entity.Device;
import de.bbqb.backend.api.model.entity.Location;
import de.bbqb.backend.gcp.firestore.DeviceRepo;
import de.bbqb.backend.gcp.firestore.FirestoreDeviceService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class FirestoreDeviceServiceTest {

	private FirestoreDeviceService sut;
	
	private DeviceRepo deviceRepoMock;

	private final String id = "deviceId";

	private final String deviceId = "deviceId";

	private final String number = "deviceId";

	private final Long publishTime = new Date().getTime();

	private final String status = "deviceId";

	private final Address address = new Address("Deutschland", "78467", "Konstanz", "Straße", "2", "Adressname");

	private final Location location = new Location(1.1, 1.2);
	
	@BeforeEach
	public void setUp() {
		this.deviceRepoMock = new DeviceRepoMock(); 

		// TODO: Create Firestore mock
		this.sut = new FirestoreDeviceService(deviceRepoMock, null);
	}

	@Test
	public void testCreateDevice() {
		// given
		Device device = new Device(id, deviceId, number, publishTime, status, location, address);
		
		// when
		//Mono<Device> savedDevice = this.sut.createDevice(device);

		// then
		//Device actualSavedDevice = savedDevice.block();
		//actualSavedDevice.equals(device);
	}

	@Test
	public void testUpdateDevice() {
		// given
		Device device = new Device(id, deviceId, number, publishTime, status, location, address);
		
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
		Device device = new Device(id, deviceId, number, publishTime, status, location, address);
		
		// when
		this.sut.openDevice(device);

		// then
	}

	@Test
	public void testLockDevice() {
		// given
		Device device = new Device(id, deviceId, number, publishTime, status, location, address);
		
		// when
		this.sut.lockDevice(device);

		// then
	}
}
