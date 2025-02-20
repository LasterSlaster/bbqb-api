package de.bbqb.backend.gcp.firestore.document;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.cloud.gcp.data.firestore.Document;

// TODO: Think about hinding TImestamp and GeoPoint dependencies
// TODO: Rework data types:  use geopoint for lat/lngt,status -> int or string?, are number and id correct?

/**
 * Device bean to represent a device document from a gcp firestore database.
 * This class is used for Jackson un-/marshaling
 *
 * @author Marius Degen
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collectionName = "devices")
public class DeviceDoc {

    @DocumentId
    private String id;
    private String deviceId;
    private String number;
    private Timestamp publishTime;
    private Boolean blocked;
    private Boolean locked;
    private Boolean closed;
    private Double wifiSignal;
    private Double isTemperaturePlate1;
    private Double isTemperaturePlate2;
    private Double setTemperaturePlate1;
    private Double setTemperaturePlate2;
    private GeoPoint location;
    private String addressName;
    private String street;
    private String houseNumber;
    private String city;
    private String postalCode;
    private String country;
}
