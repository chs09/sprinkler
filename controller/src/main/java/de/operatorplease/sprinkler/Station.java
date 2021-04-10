package de.operatorplease.sprinkler;

import java.util.Objects;

import de.operatorplease.sprinkler.settings.Zone;

public abstract class Station {
	private final Zone zone;
	
	public Station(Zone zone) {
		this.zone = Objects.requireNonNull(zone, "Zone cannot be null!");
	}
	
	public boolean isActive() {
		return false;
	}
	
	public boolean isDisabled() {
		return zone.isDisabled();
	}
	
	public int getZoneId() {
		return zone.getZid();
	}
	
	public abstract void stop();
	public abstract void start();
	
	@Override
	public String toString() {
		return "Station[" + zone.getZid() + "]";
	}
}
