package de.bbqb.backend.api.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Card {
    private final String id;
    private final String clientSecret;
    private final String brand;
    private final Long expMonth;
    private final Long expYear;
    private final String last4;

    public Card(String clientSecret) {
        this.clientSecret = clientSecret;
        this.id = null;
        this.brand = null;
        this.expMonth = null;
        this.expYear = null;
        this.last4 = null;
    }
}
