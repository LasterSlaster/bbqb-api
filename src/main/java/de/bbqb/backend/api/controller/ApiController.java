package de.bbqb.backend.api.controller;

import java.net.URI;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import de.bbqb.backend.api.ApiApplication.PubsubOutboundGateway;
import de.bbqb.backend.gcp.firestore.DeviceRepo;
import de.bbqb.backend.gcp.firestore.document.DeviceDoc;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class ApiController {
	
	
	private final DeviceRepo deviceRepo;

	@Autowired
	private PubsubOutboundGateway messagingGateway;

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
	
	/**
	 * publish a message to a device like open bbqb
	 * @param deviceDoc
	 * @return
	 */
	@PutMapping("/devices") 
	public ResponseEntity<DeviceDoc> putDevices(@RequestBody DeviceDoc deviceDoc) {
		messagingGateway.sendToPubsub("message"); // TODO: Update message payload

		DeviceDoc savedDevice = null; // TODO: updat this part
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
