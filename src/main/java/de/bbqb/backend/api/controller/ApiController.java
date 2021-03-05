package de.bbqb.backend.api.controller;

import org.springframework.web.bind.annotation.*;

/**
 * REST Controller with application wide endpoints
 *
 * @author Marius Degen
 */
@CrossOrigin(origins = "*") // CORS configuration to allow all for the endpoints in this controller
@RestController
public class ApiController {

    /**
     * Endpoint for gcp appengine to test application availability
     *
     * @return hello world string
     */
    @GetMapping("/")
    public String hello() {
        return "Hello World";
    }
}
