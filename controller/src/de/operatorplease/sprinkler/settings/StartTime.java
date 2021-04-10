package de.operatorplease.sprinkler.settings;

import java.time.LocalTime;

// in Minutes per Day
public class StartTime {
	public enum Type {
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
	
	public int getOffset() {
		return offset;
	}
	
	public Type getType() {
		return type;
	}
}
