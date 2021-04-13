package de.operatorplease.sprinkler.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Plan {
	private int pid;
	private boolean enabled = true;
	private boolean weatherDependent = true;
	private boolean sequential = true;
	
	// odd/even restriction:
	// 0->none, 1->odd day (except 31st and Feb 29th)
	// 2->even day
	public enum EvenOddRestriction {
		NONE, ODD, EVEN
	}

	private EvenOddRestriction oddeven = EvenOddRestriction.NONE;

	public enum ScheduleType {
		WEEKLY, BIWEEKLY, MONTHLY
	}

	private ScheduleType type = ScheduleType.MONTHLY;

	// weekly: days correspond to Monday till Sunday (binary OR)
	// bi-weekly: days correspond to Monday till Sunday (binary OR)
	// monthly: days stores the day of the month (32 means last day of month)
	private int days;

	// program start time
	private List<StartTime> starttimes = new ArrayList<>();

	// program is a fixed start time or a repeating type
	private int repeat = 0;

	// interval in minutes if repeat == true
	private int delay = 0;

	private final Map<Integer, Short> durations = new HashMap<>(); // duration / water time of each zone

	private Integer mainValve = null;
	
	private String name;

	public Integer getMainValve() {
		return mainValve;
	}
	
	public void setMainValve(Integer mainValve) {
		this.mainValve = mainValve;
	}
	
	public boolean isSequential() {
		return this.sequential;
	}
	
	public void setSeqential(boolean sequential) {
		this.sequential = sequential;
	}
	
	public void setWeatherDependent(boolean use_weather) {
		this.weatherDependent = use_weather;
	}
	
	public boolean isWeatherDependent() {
		return weatherDependent;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public short getDuration(Zone zone) {
		return durations.getOrDefault(zone.getZid(), Short.valueOf((short) 0));
	}
	
	public void setDuration(Zone zone, short minutes) {
		if(minutes < 0)
			throw new IllegalArgumentException("watering duration cannot be less than 0");
		durations.put(zone.getZid(), minutes);
	}
	
	public Map<Integer, Short> getDurations() {
		return Collections.unmodifiableMap(durations);
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public int getPid() {
		return pid;
	}

	public EvenOddRestriction getOddeven() {
		return oddeven;
	}

	public void setOddeven(EvenOddRestriction oddeven) {
		this.oddeven = oddeven;
	}

	public ScheduleType getType() {
		return type;
	}

	public void setType(ScheduleType type) {
		this.type = type;
	}

	public int getDays() {
		return days;
	}

	public void setDays(int days) {
		this.days = days;
	}

	public List<StartTime> getStarttimes() {
		return starttimes;
	}

	public void setStarttimes(List<StartTime> starttimes) {
		this.starttimes = starttimes;
	}

	public int getRepeat() {
		return repeat;
	}

	public void setRepeat(int repeat) {
		this.repeat = repeat;
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public void setSequential(boolean sequential) {
		this.sequential = sequential;
	}	
}
