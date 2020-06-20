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

@CrossOrigin(origins = "*")
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
	

	@PutMapping("/devices")
	public Mono<ResponseEntity<Device>> putDevices(@RequestBody Device device) {
		return deviceService.updateDevice(device).map((Device updatedDevice) -> {
			// create Response
			if (updatedDevice == null) {
	    	    return ResponseEntity.notFound().build();
	    	} else {
	    	    URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
	    	      .path("/{id}")
	    	      .buildAndExpand(device.getId())
	    	      .toUri();
	 
	    	    return ResponseEntity.created(uri)
	    	     .body(updatedDevice);
	    	}
		});
	}
	
	/**
	 * publish a message to a device like open bbqb
	 * @param deviceDoc
	 * @return
	 */
	@PostMapping("/message") 
	public ResponseEntity<Device> postMessage(@RequestBody Device device) {
		deviceService.openDevice(device);
	 
	    return ResponseEntity.accepted().build();
	}
	 

	@PostMapping("/devices")
	public Mono<ResponseEntity<Device>> postDevices(@RequestBody Device device) { 
		return deviceService.createDevice(device).map((Device savedDevice) -> {
			// create Response
			if (savedDevice == null) {
	    	    return ResponseEntity.notFound().build();
	    	} else {
	    	    URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
	    	      .path("/{id}")
	    	      .buildAndExpand(savedDevice.getId())
	    	      .toUri();
	 
	    	    return ResponseEntity.created(uri)
	    	      .body(savedDevice);
	    	}
		});
	}


	@GetMapping("/devices")
	public Flux<Device> getDevices() {
		return deviceService.readAllDevices();
	}
	

	@GetMapping("/devices/{deviceId}")
	public Mono<ResponseEntity<Device>> getDevice(@PathVariable("deviceId") String deviceId) {
		return deviceService.readDevice(deviceId).map((Device device) -> {
			// create Response
			if (device == null) {
	    	    return ResponseEntity.notFound().build();
	    	} else {
	    	    URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
	    	      .path("/{id}")
	    	      .buildAndExpand(device.getId())
	    	      .toUri();
	 
	    	    return ResponseEntity.created(uri)
	    	      .body(device);
	    	}
		});
	}
	
}