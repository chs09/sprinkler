package de.operatorplease.sprinkler;

import java.time.Duration;

import de.operatorplease.sprinkler.settings.Plan;

public class Status {
	public boolean rainDelayed;
	public Plan programRunning;
	public boolean networkAvailable;
	
	public char mode;
	public Duration duration;
	public String program;
	public long lastUserInput = System.currentTimeMillis();
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((duration == null) ? 0 : duration.hashCode());
		result = prime * result + (int) (lastUserInput ^ (lastUserInput >>> 32));
		result = prime * result + mode;
		result = prime * result + (networkAvailable ? 1231 : 1237);
		result = prime * result + ((program == null) ? 0 : program.hashCode());
		result = prime * result + ((programRunning == null) ? 0 : programRunning.hashCode());
		result = prime * result + (rainDelayed ? 1231 : 1237);
		return result;
	}
}