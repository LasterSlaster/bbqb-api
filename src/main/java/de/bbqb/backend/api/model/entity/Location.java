package de.bbqb.backend.api.model.entity;

/**
 * BBQ-Butler business object to hold location information based on lat/long
 * 
 * @author laster
 */
public class Location {

	private Double latitude;
	private Double longitude;

	public Location(Double latitude, Double longitude) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}
}