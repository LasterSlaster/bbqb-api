package de.bbqb.backend.api.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
public class Booking {
    public Booking() {}
    private String id;
    private String paymentIntentId; // TODO: Remove this field. Redundant, also in payment as id
    private String deviceId;
    private String userId;
    private String status;
    private Date requestTime;
    private Date sessionStart;
    private Payment payment;
    private Integer timeslot;


    public Booking(String id, String paymentIntentId, String deviceId, String userId, String status, Date requestTime, Date sessionStart, Payment payment, Integer timeslot) {
        this.id = id;
        this.paymentIntentId = paymentIntentId;
        this.deviceId = deviceId;
        this.userId = userId;
        this.status = status;
        this.requestTime = requestTime;
        this.sessionStart = sessionStart;
        this.payment = payment;
        this.timeslot = timeslot;
    }

    public String getId() {
        return id;
    }

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getUserId() {
        return userId;
    }

    public String getStatus() {
        return status;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public Date getSessionStart() {
        return sessionStart;
    }

    public Payment getPayment() {
        return payment;
    }

    public Integer getTimeslot() {
        return timeslot;
    }
}
