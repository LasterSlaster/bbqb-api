package de.bbqb.backend.api.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BookingRequest {
    private final String deviceId;
    private final Integer timeslot;
    private final String paymentMethodId;
}
