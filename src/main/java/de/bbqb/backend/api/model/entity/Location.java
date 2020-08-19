package de.bbqb.backend.api.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * BBQ-Butler business object to hold location information based on lat/long
 *
 * @author Marius Degen
 */
@Getter
@AllArgsConstructor
public class Location {

    private final Double latitude;
    private final Double longitude;
}