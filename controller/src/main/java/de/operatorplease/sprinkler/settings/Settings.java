package de.operatorplease.sprinkler.settings;

import java.util.Optional;

public class Settings {
	
	private static final InheritableThreadLocal<Settings> instance = new InheritableThreadLocal<>();
	
	public Settings() {
		instance.set(this);
	}
	
	public static Optional<Settings> getInstance() {
		return Optional.ofNullable(instance.get());
	}
	
	public class Location {
		public double lat;
		public double lon;
	}
	
	public Location location;
	
	public String mainValve;
}
