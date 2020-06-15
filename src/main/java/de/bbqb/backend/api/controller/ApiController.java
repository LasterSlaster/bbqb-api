package de.bbqb.backend.api.controller;

import java.net.URI;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
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

@CrossOrigin(origins = "*")
@RestController
public class ApiController {
	
	private DeviceService deviceService;

	public ApiController(DeviceService deviceService) {
		super();
		this.deviceService = deviceService;
	}

	// Test endpoint only for development purposes
	@GetMapping("/hello")
	public String hello() {
		return "Hello World";
	}
	

	// TODO: Implement message publishing to communicate with iot devices
	@PutMapping("/devices")
	public ResponseEntity<Device> putDevices(@RequestBody Device device) { //TODO: Change return value

		Device savedDevice = deviceService.updateDevice(device);
		
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
	public ResponseEntity<Device> postDevices(@RequestBody Device device) { 

		Device savedDevice = deviceService.createDevice(device);
		
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
	}


	@GetMapping("/devices")
	public Stream<Device> getDevices() {

		return deviceService.readAllDevices();
	}
	

	@GetMapping("/devices/{deviceId}")
	public ResponseEntity<Device> getDevice(@PathVariable("deviceId") String deviceId) {

		Device device = deviceService.readDevice(deviceId);

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
	}
	
}