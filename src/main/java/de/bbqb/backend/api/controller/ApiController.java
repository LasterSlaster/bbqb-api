package de.bbqb.backend.api.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import de.bbqb.backend.api.model.entity.Device;
import de.bbqb.backend.api.model.service.DeviceService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST Controller with endpoints to manage device resources like
 * accessing/updating/creating device information and sending messages to
 * devices.
 * 
 * @author Marius Degen
 *
 */
@CrossOrigin(origins = "*") // CORS configuration to allow all for the endpoints in this controller
@RestController
public class ApiController {

	private DeviceService deviceService;

	public ApiController(DeviceService deviceService) {
		super();
		this.deviceService = deviceService;
	}

	// Test endpoint only for development purposes
	@GetMapping("/")
	public String hello() {
		return "Hello World";
	}

	/**
	 * publish a message to a device like open bbqb
	 * 
	 * @param deviceDoc
	 * @return
	 */
	@PostMapping("/message")
	public ResponseEntity<Device> postMessage(@RequestBody Device device) {
		deviceService.openDevice(device);

		return ResponseEntity.accepted().build();
	}

	@GetMapping("/devices")
	public Flux<Device> getDevices() {
		return deviceService.readAllDevices();
	}

	@GetMapping("/devices/{id}")
	public Mono<ResponseEntity<Device>> getDevice(@PathVariable("id") String deviceId) {
		ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();

		return deviceService.readDevice(deviceId).map((Device device) -> {
			// create Response
			if (device == null) {// TODO: Check if this is ever the case
				return ResponseEntity.notFound().build();
			} else {
				URI uri = builder.build().toUri();
				return ResponseEntity.created(uri).body(device);
			}
		});
	}

	@PostMapping("/devices")
	public Mono<ResponseEntity<Device>> postDevices(@RequestBody Device device) {
		ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();

		return deviceService.createDevice(device).map((Device savedDevice) -> {
			// create Response
			if (savedDevice == null) { // TODO: Check if this is ever the case
				return ResponseEntity.unprocessableEntity().build();
			} else {
				URI uri = builder.path("/{id}").buildAndExpand(savedDevice.getId()).toUri();

				return ResponseEntity.created(uri).body(savedDevice);
			}
		});
	}

	@PutMapping("/devices/{id}")
	public Mono<ResponseEntity<Device>> putDevices(@PathVariable("id") String deviceId, @RequestBody Device device) {
		ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();

		if (device.getId() != null && device.getId().equals(deviceId)) {
			return deviceService.updateDevice(device).map((Device updatedDevice) -> {
				// create Response
				if (updatedDevice == null) {// TODO: Check if this is ever the case
					return ResponseEntity.unprocessableEntity().build();
				} else {
					URI uri = builder.build().toUri();

					return ResponseEntity.created(uri).body(updatedDevice);
				}
			});
		} else {
			return Mono.just(ResponseEntity.unprocessableEntity().build()); // TODO: Create message id is missing or not
																			// equal to deviceId
		}
	}
}