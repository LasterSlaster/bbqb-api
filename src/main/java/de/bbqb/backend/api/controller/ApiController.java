package de.bbqb.backend.api.controller;

import de.bbqb.backend.api.model.entity.Device;
import de.bbqb.backend.api.model.service.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.retry.Repeat;

import java.net.URI;
import java.time.Duration;

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
    public String hello(@AuthenticationPrincipal OAuth2User oauth2User) {
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
        if (device.getDeviceId() != null && deviceService.openDevice(device.getDeviceId())) { //TODO: research processing sideeffects(IO) in if-statement evaluation
            return ResponseEntity.accepted().build();
        } else {
            return ResponseEntity.unprocessableEntity().build();
        }
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
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();
        return deviceService.readDevice(deviceId).map((Device device) -> {
            URI uri = builder.build().toUri();
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
     * @param id:     The ID of the device to be updated. Must be identical to the id field in the device object in the request body.
     * @param device: The device object which will be used to update the device.
     * @return The updated device.
     */
    @PutMapping("/devices/{id}")
    public Mono<ResponseEntity<Device>> putDevices(@PathVariable("id") String id, @RequestBody Device device) {
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();
        if (device.getId() != null && device.getId().equals(id)) {
            return deviceService
                    .updateDevice(device)
                    .flatMap(updatedDevice -> this.checkToOpenDevice(device, updatedDevice))
                    .map(updatedDevice -> ResponseEntity.created(builder.build().toUri()).body(updatedDevice)) // TODO: Think about returning 200/204 instead
                    .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        } else {
            return Mono.just(ResponseEntity.unprocessableEntity().build()); // TODO: Create message "id is missing" or "not equal to deviceId"
        }
    }

    private Mono<Device> checkToOpenDevice(Device device, Device updatedDevice) {
        // TODO: Think about moving this into deviceService
        if (device.getLocked() != null && device.getLocked() == false) {
            if (openDevice(device)) {
                return deviceService.readDevice(device.getId())
                        .switchIfEmpty(Mono.error(new Exception())) // TODO: Implement better exception
                        // Repeatedly fetch the device the signal was send to until it is unlocked. 5 times with 1 second delays
                        .filter(pendingDevice -> pendingDevice.getLocked() == false)
                        .repeatWhenEmpty(Repeat.times(10).fixedBackoff(Duration.ofSeconds(1)))
                        //.repeatWhenEmpty(comp -> comp.zipWith(Flux.range(1,5), (a,b) -> {
                        //    if (b < 4) {
                        //        return b;
                        //    } else {
                        //        throw Exceptions.propagate(new Exception());
                        //    }
                        //}).delayElements(Duration.ofSeconds(1L)))
                        .doOnSuccess(result -> {
                            // If the device is still locked after repeats throw exception
                            if (result == null) {
                                throw Exceptions.propagate(new Exception());
                            }
                        })
                        ;
            } else {
                return Mono.error(new Exception()); // TODO: Implement better exception
            }
        } else {
            // Continue with updatedDevice if it doesn't have to be unlocked
            return Mono.just(updatedDevice);
        }
    }

    private Boolean openDevice(Device device) {
        return device.getDeviceId() != null && deviceService.openDevice(device.getDeviceId());
    }
}