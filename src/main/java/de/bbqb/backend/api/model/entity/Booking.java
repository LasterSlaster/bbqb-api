package de.bbqb.backend.api.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class Booking {
    private String id;
    private String paymentIntentId; // TODO: Remove this field. Redundant, also in payment as id
    private String deviceId;
    private String userId;
    private String status;
    private Date requestTime;
    private Date sessionStart;
    private Payment payment;
    private Integer timeslot;
}
