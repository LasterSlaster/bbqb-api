package de.bbqb.backend.api.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Payment {
    private final String id;
    private final String clientSecret;
    private final Long amount;
    private final String country;
    private final String currency;
    private final String label;
}
