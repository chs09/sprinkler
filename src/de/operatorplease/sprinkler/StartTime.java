package de.operatorplease.sprinkler;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

// in Minutes per Day
public class StartTime {
	enum Type {
		FIXED,
		SUNRISE,
		SUNSET
	}
	
	private Type type = Type.FIXED;

	// in Minutes -1440 < X < 1440 
	// may be negative (e.g. sunrise - 60 minutes)
	private int offset;

	private StartTime() {
		// empty constructor for de-/serialization
	}
	
	public static StartTime of(Type type, LocalTime offset) {
		return StartTime.of(type, (offset.getHour() * 60) + offset.getMinute());
	}
	
	public static StartTime of(Type type, int offset) {
		StartTime instance = new StartTime();
		instance.type = type;
		
		if (offset >=  1440) offset =  1439; // clamp it to 1440 if larger than 1440
		if (offset <=- 1440) offset = -1439; // clamp it to 1440 if larger than 1440
		
		instance.offset = offset;
		return instance;
	}
	
	public StartTime plus(int offset) {
		return of(type, this.offset + offset);
	}
	
	private LocalTime getSunriseTime() {
		// TODO https://shredzone.org/maven/commons-suncalc/index.html
		return LocalTime.of(7, 0);
	}
	
	private LocalTime getSunsetTime() {
		return LocalTime.of(21, 0);
	}
	
	public LocalTime decode() {
		switch(type) {
			case FIXED:
				return LocalTime.of(offset / 60, offset % 60);
				
			case SUNRISE:
				return getSunriseTime().plusMinutes(offset);
				
			case SUNSET:
				return getSunsetTime().plusMinutes(offset);
		}
		throw new IllegalStateException();
	}
	
	public boolean matches(LocalTime now) {
		LocalTime time = decode();
		return (ChronoUnit.MINUTES.between(time, now) == 0);
	}
}
