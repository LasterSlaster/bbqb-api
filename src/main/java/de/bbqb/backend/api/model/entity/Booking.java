package de.bbqb.backend.api.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class Booking {
    private String id;
    private String paymentIntentId;
    private String deviceId;
    private String userId;
    private String status;
    private Date timestamp;
    private Payment payment;
    private Integer timeslot;
}
