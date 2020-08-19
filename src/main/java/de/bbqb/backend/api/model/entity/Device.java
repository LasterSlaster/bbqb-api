package de.bbqb.backend.api.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * BBQ-Butler business object to hold device information
 *
 * @author Marius Degen
 */
@Getter
@AllArgsConstructor
public class Device {

    private String id;
    private String deviceId;
    private String name;
    private String number;
    // Milliseconds since January 1, 1970, 00:00:00 UTC
    private Long publishTime; // TODO: Change to seconds since UNIX epoche
    private String status; // TODO: Change to Enum?
    private Location location;
    private Address address;

    public Device(String id, Device device) {
        this(id, device.getDeviceId(), device.getName(), device.getNumber(), device.getPublishTime(), device.getStatus(), device.getLocation(), device.getAddress());
    }
}