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

    private final String id;
    private final String deviceId;
    private final String name;
    private final String number;
    // Milliseconds since January 1, 1970, 00:00:00 UTC
    private final Long publishTime; // TODO: Change to seconds since UNIX epoche
    private final String status; // TODO: Change to Enum?
    private final Location location;
    private final Address address;

    public Device(String id, Device device) {
        this(id, device.getDeviceId(), device.getName(), device.getNumber(), device.getPublishTime(), device.getStatus(), device.getLocation(), device.getAddress());
    }
}