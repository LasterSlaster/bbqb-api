package de.bbqb.backend.api.model.service;

import de.bbqb.backend.api.model.entity.Device;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Interface to handle business logic to interact with bbqb devices
 *
 * @author Marius Degen
 */
public interface DeviceService {

    public Mono<Device> createDevice(Device device);

    public Mono<Device> updateDevice(Device device);

    public Mono<Device> readDevice(String deviceId);

    public Flux<Device> readAllDevices();

    public void openDevice(Device device); // TODO: Add return type

    public void lockDevice(Device device);

}
