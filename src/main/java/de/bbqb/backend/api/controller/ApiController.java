package de.bbqb.backend.api.controller;

import de.bbqb.backend.api.model.entity.Device;
import de.bbqb.backend.api.model.service.DeviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * REST Controller with endpoints to manage device resources like
 * accessing/updating/creating device information and sending messages to
 * devices.
 *
 * @author Marius Degen
 */
@CrossOrigin(origins = "*") // CORS configuration to allow all for the endpoints in this controller
@RestController
public class ApiController {

    private DeviceService deviceService;

    public ApiController(DeviceService deviceService) {
        super();
        this.deviceService = deviceService;
    }

    /**
     * Test endpoint. Only for development purposes.
     *
     * @return hello world string
     */
    @GetMapping("/")
    public String hello() {
        return "Hello World";
    }

    /**
     * Send an open device signal to a device.
     *
     * @param device: The device to send the signal to.
     * @return The device to which the signal was send to with its up to date information.
     */
    @PostMapping("/message")
    public ResponseEntity<Device> postMessage(@RequestBody Device device) {
        deviceService.openDevice(device);

        return ResponseEntity.accepted().build();
    }

    /**
     * Retrieve all devices.
     *
     * @return An Array of device objects.
     */
    @GetMapping("/devices")
    public Flux<Device> getDevices() {
        return deviceService.readAllDevices();
    }

    /**
     * Retrieve a device by its ID.
     *
     * @param deviceId: The ID of the device to retrieve.
     * @return The device identified by the deviceId parameter.
     */
    @GetMapping("/devices/{id}")
    public Mono<ResponseEntity<Device>> getDevice(@PathVariable("id") String deviceId) {
        return deviceService.readDevice(deviceId).map((Device device) -> {
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
            return ResponseEntity.created(uri).body(device);
        }).defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Create a new device.
     *
     * @param device: The device to be created.
     * @return The created device with additional information.
     */
    @PostMapping("/devices")
    public Mono<ResponseEntity<Device>> postDevices(@RequestBody Device device) {
        // TODO: Validate device object
        return deviceService.createDevice(device).map((Device savedDevice) -> {
            URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(savedDevice.getId()).toUri();
            return ResponseEntity.created(uri).body(savedDevice);
        });
    }

    /**
     * Update the information of a device.
     *
     * @param id:     The ID of the device to be updated. Must be identical to the id field in the device object in the request body
     * @param device: The device object which will be used to update the device.
     * @return The updated device.
     */
    @PutMapping("/devices/{id}")
    public Mono<ResponseEntity<Device>> putDevices(@PathVariable("id") String id, @RequestBody Device device) {
        // TODO: Validate device object more
        if (device.getId() != null && device.getId().equals(id)) {
            return deviceService.updateDevice(device).map((Device updatedDevice) -> {
                URI uri = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
                return ResponseEntity.created(uri).body(updatedDevice);
            });
        } else {
            // TODO: Create message "id is missing" or "not equal to deviceId"
            return Mono.just(ResponseEntity.unprocessableEntity().build());
        }
    }
}