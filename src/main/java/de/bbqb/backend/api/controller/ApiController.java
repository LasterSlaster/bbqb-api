package de.bbqb.backend.api.controller;

import java.net.URI;
import java.util.stream.Stream;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import de.bbqb.backend.gcp.firestore.DeviceRepo;
import de.bbqb.backend.gcp.firestore.document.DeviceDoc;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class ApiController {
	
	
	private final DeviceRepo deviceRepo;

	public ApiController(DeviceRepo deviceRepo) {
		this.deviceRepo = deviceRepo;
	}

	@GetMapping
	public String test() {
		return "App up and running";
	}
	
	@GetMapping("/hello")
	public String hello() {
		return "Hello World";
	}
	 
	@GetMapping("/devices")
	public Stream<DeviceDoc> getDevices() {
		Flux<DeviceDoc> devices = deviceRepo.findAll();
		return devices.toStream();
	}
	
	@PostMapping("/devices")
	public ResponseEntity<DeviceDoc> postDevices(@RequestBody DeviceDoc deviceDoc) {
		DeviceDoc savedDevice = deviceRepo.save(deviceDoc).block();
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
}
