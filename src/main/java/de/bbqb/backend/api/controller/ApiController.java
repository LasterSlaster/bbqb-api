package de.bbqb.backend.api.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import de.bbqb.backend.api.model.entity.Device;
import de.bbqb.backend.gcp.firestore.DeviceRepo;
import de.bbqb.backend.gcp.firestore.document.DeviceDoc;
import reactor.core.publisher.Flux;

@RestController
public class ApiController {
	
	DeviceRepo deviceRepo;

	@GetMapping
	public String test() {
		return "App up and running";
	}
	
	@GetMapping("/hello")
	public String hello() {
		return "Hello World";
	}
	 
	@GetMapping("/devices")
	public List<DeviceDoc> getDevices() {
		Flux<DeviceDoc> devices = deviceRepo.findAll();
		return devices;
	}
}
