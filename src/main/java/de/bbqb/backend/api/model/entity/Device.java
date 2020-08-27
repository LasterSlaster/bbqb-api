package de.bbqb.backend.api.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * BBQ-Butler business object to hold device information
 *
 * @author Marius Degen
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Device {

    private String id;
    private String deviceId;
    private String name;
    private String number;
    // Milliseconds since January 1, 1970, 00:00:00 UTC
    private Long publishTime; // TODO: Change to seconds since UNIX epoche
    private String lockStatus; // TODO: Change to Enum?
    private Location location;
    private Address address;
    private String drawerStatus;
    private Double wifiSignal;
    private Double temperature;

    public Device(String id, Device device) {
        this(id, device.getDeviceId(), device.getName(), device.getNumber(), device.getPublishTime(), device.getLockStatus(), device.getLocation(), device.getAddress(), device.getDrawerStatus(), device.getWifiSignal(), device.getTemperature());
    }
}