package de.bbqb.backend.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

  @GetMapping
  public String test() {
    return "App up and running";
  }

  @GetMapping("/hello")
  public String hello() {
    return "Hello World";
  }
}
