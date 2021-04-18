package de.operatorplease.sprinkler.weather;

import java.time.Duration;

// dummy weather provider, always returning 100%
public class Weather {
	
	// store is static, so every provider will use the same store
	private static final WeatherStore store = new WeatherStore();
	
	public void check() {
		
	}
	
	public Duration getUpdateInterval() {
		// by default, update every 30 minutes
		return Duration.ofMinutes(30);
	}
	
	public static WeatherStore getStore() {
		return store;
	}
}