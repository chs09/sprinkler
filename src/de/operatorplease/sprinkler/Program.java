package de.operatorplease.sprinkler;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Program {
	private int pid;
	private boolean enabled = true;
	private boolean use_weather = true;
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

	private String name;

	public boolean isSequential() {
		return this.sequential;
	}
	
	public void setSeqential(boolean sequential) {
		this.sequential = sequential;
	}
	
	public void setWeatherDependent(boolean use_weather) {
		this.use_weather = use_weather;
	}
	
	public boolean isWeatherDependent() {
		return use_weather;
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
	
	public Set<Entry<Integer, Short>> getDurations() {
		return durations.entrySet();
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
	
	/** Check if a given time matches the program's start day */
	private boolean checkDayMatch(LocalDateTime t) {
		DayOfWeek weekday_t = t.getDayOfWeek();
		int day_t = t.getDayOfMonth();
		Month month_t = t.getMonth();

		// check day match
		switch (type) {
		case BIWEEKLY:
			int week = t.get(WeekFields.ISO.weekOfWeekBasedYear());
			if (week % 2 == 0)
				return false;

			// fall trough, weekday match
		case WEEKLY:
			// weekday match
			if (0 == (days & (1 << weekday_t.ordinal())))
				return false;
			break;

		case MONTHLY:
			if (day_t != (days & 0b11111))
				return false;
			break;
		}

		// check odd/even day restriction
		if (oddeven == EvenOddRestriction.EVEN) {
			// even day restriction
			if ((day_t % 2) != 0)
				return false;
		} else if (oddeven == EvenOddRestriction.ODD) {
			// odd day restriction
			// skip 31st and Feb 29
			if (day_t == 31)
				return false;
			else if (day_t == 29 && month_t == Month.FEBRUARY)
				return false;
			else if ((day_t % 2) != 1)
				return false;
		}
		return true;
	}

	private int offset(LocalTime t) {
		return (t.getHour() * 60) + t.getMinute();
	}

	public boolean matches(LocalDateTime t) {
		// check program enable status
		if (!enabled) {
			return false;
		}

		LocalTime time = t.toLocalTime();

		// first assume program starts today
		if (checkDayMatch(t)) {
			// t matches the program's start day
			if (repeat == 0) {
				for (StartTime s : starttimes) {
					if (s.matches(time)) {
						// if current minute matches any of the given start time return true
						return true;
					}
				}
				return false; // otherwise return false

				// repeating type
			} else {
				if (starttimes.size() != 1) {
					throw new IllegalStateException(
							"Repeating program should have exactly 1 start time, but had " + starttimes.size());
				}

				StartTime start = starttimes.get(0);

				// if current_minute matches start time, return true
				if (start.matches(time))
					return true;

				// otherwise, current_minute must be larger than start time, and interval must
				// be non-zero
				if (offset(t.toLocalTime()) > offset(start.decode()) && delay > 0) {
					for (int loop = 1; loop <= repeat; loop++) {
						start = start.plus(delay);
						if (start.matches(time)) {
							return true;
						}
					}
				}
			}
		}
//				// to proceed, program has to be repeating type, and interval and repeat must be non-zero
//				if (starttime_type == StartTimeType.FIXED || interval == 0)
//					return false;
//
//				// next, assume program started the previous day and ran over night
//			 	if (check_day_match(t-86400L)) {
//					// t-86400L matches the program's start day
//					int c = (current_minute - start + 1440) / interval;
//					if ((c * interval == (current_minute - start + 1440)) && c <= repeat) {
//						return true;
//					}
//				}
		return false;
	}
}
