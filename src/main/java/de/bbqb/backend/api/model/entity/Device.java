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
    private String number;
    // Milliseconds since January 1, 1970, 00:00:00 UTC
    private Long publishTime; // TODO: Change to seconds since UNIX epoche
    private Boolean blocked;
    private Boolean locked; // TODO: Change to Enum?
    private Boolean closed;
    private Double wifiSignal;


    private Double isTemperaturePlate1;
    private Double isTemperaturePlate2;
    private Double setTemperaturePlate1;
    private Double setTemperaturePlate2;
    private Location location;
    private Address address;

    public Device(String id, Device device) {
        this(id, device.getDeviceId(), device.getNumber(), device.getPublishTime(), device.getLocked(), device.getClosed(), device.getWifiSignal(), device.getTemperaturePlate1(), device.getTemperaturePlate2(), device.getLocation(), device.getAddress());
    }
}