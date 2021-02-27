package de.bbqb.backend.api.controller;

import de.bbqb.backend.api.model.entity.*;
import de.bbqb.backend.api.model.service.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * REST Controller with endpoints to manage device resources like
 * accessing/updating/creating device information.
 *
 * @author Marius Degen
 */
@CrossOrigin(origins = "*") // CORS configuration to allow all for the endpoints in this controller
@RestController
public class DeviceController {

    private DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        super();
        this.deviceService = deviceService;
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
     * @param deviceId: The ID for the device to retrieve.
     * @return The device identified by the deviceId parameter.
     */
    @GetMapping("/devices/{id}")
    public Mono<ResponseEntity<Device>> getDevice(@PathVariable("id") String deviceId) {
        return deviceService.readDevice(deviceId)
                .map(ResponseEntity.ok()::body)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Create a new device.
     *
     * @param device: The device to be created.
     * @return The created device with additional information.
     */
    @PostMapping("/devices")
    public Mono<ResponseEntity<Device>> postDevices(@RequestBody Device device) {
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();
        // TODO: Validate device object
        return deviceService.createDevice(device).map((Device savedDevice) -> {
            URI uri = builder.path("/{id}").buildAndExpand(savedDevice.getId()).toUri();
            return ResponseEntity.created(uri).body(savedDevice);
        });
    }

    /**
     * Update the information of a device.
     * TODO: Currently not idempotent! Because it does not use the id from the request but creates a new one
     *
     * @param id:     The ID for the device to be updated. Must be identical to the id field in the device object in the request body.
     * @param device: The device object which will be used to update the device.
     * @return The updated device.
     */
    @PutMapping("/devices/{id}")
    public Mono<ResponseEntity<Device>> putDevices(@AuthenticationPrincipal Authentication sub, @PathVariable("id") String id, @RequestBody Device device) {
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();
        if (device.getId() != null && device.getId().equals(id)) {
            return deviceService
                    .updateDevice(device)
                    // TODO: Check if there is a active booking for this device and user
                    .map(ResponseEntity.created(builder.build().toUri())::body) // TODO: Think about returning 200/204 instead
                    .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        } else {
            return Mono.just(ResponseEntity.unprocessableEntity().build()); // TODO: Create message "id is missing" or "not equal to deviceId"
        }
    }
}
