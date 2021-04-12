package de.operatorplease.sprinkler;

import java.time.Duration;
import java.time.LocalDateTime;

public class Display {
	public void printTime(LocalDateTime time) {
		
	}
	
	public void updateStatus(Status status) {
		
	}
	
	protected String toString(Duration duration) {
		if(duration == null) {
			return "";
		}
		// skip 'PT'
		return duration.toString().substring(2);
	}
}
