package de.operatorplease.sprinkler.web.dto;

import de.operatorplease.sprinkler.Station;

public class ValveDTO {
	private Station station;
	
	public ValveDTO(Station station) {
		this.station = station;
	}
	
	public String getId() {
		return station.getZoneId();
	}
	
	public boolean isActive() {
		return station.isActive();
	}
	
	public boolean isDisabled() {
		return station.isDisabled();
	}
}
