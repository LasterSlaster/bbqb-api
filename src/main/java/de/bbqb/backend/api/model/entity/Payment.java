package de.bbqb.backend.api.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Payment {
    private final String id;
    private final String clientSecret;
    // Identifies a Stripe paymentMethod for a specific user like a credit card
    private final String paymentMethodId;
    private final Long amount;
    private final String country;
    private final String currency;
    // The label describes what the payment is for
    private final String label;
}
