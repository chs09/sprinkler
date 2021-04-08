package de.operatorplease.sprinkler;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;

import org.shredzone.commons.suncalc.SunTimes;

import de.operatorplease.sprinkler.settings.Plan;
import de.operatorplease.sprinkler.settings.StartTime;
import de.operatorplease.sprinkler.settings.Plan.EvenOddRestriction;

class Program {
	private LocalTime sunrise = LocalTime.of(7, 0);
	private LocalTime sunset = LocalTime.of(21, 0);
	private LocalDateTime sunupdate = LocalDateTime.MIN;
	
	private Plan plan;
	
	public Program(Plan plan) {
		this.plan = Objects.requireNonNull(plan, "Plan should not be null");
	}
	
	private void computeSunTimes() {
		try {
			sunupdate = LocalDateTime.now();
			ZonedDateTime dateTime = sunupdate.atZone(ZoneId.systemDefault());
			double lat = 0, lng = 0;// geolocation
			SunTimes times = SunTimes.compute()
					.on(dateTime)   // set a date
					.at(lat, lng)   // set a location
					.execute();     // get the results
			
			sunrise = times.getRise().toLocalTime();
			sunset = times.getSet().toLocalTime();
			
			System.out.println("Sun times: " + times);
		} catch (Exception e) {
			// TODO logger
			e.printStackTrace();
		}
	}
	
	private LocalTime getSunriseTime() {
		if(sunupdate.isBefore(LocalDateTime.now())) {
			computeSunTimes();
		}
		return sunrise;
	}
	
	private LocalTime getSunsetTime() {
		if(sunupdate.isBefore(LocalDateTime.now())) {
			computeSunTimes();
		}
		return sunset;
	}
	
	public LocalTime decode(StartTime start) {
		int offset = start.getOffset();
		switch(start.getType()) {
			case FIXED:
				return LocalTime.of(offset / 60, offset % 60);
				
			case SUNRISE:
				return getSunriseTime().plusMinutes(offset);
				
			case SUNSET:
				return getSunsetTime().plusMinutes(offset);
		}
		throw new IllegalStateException();
	}

	public boolean matches(StartTime start, LocalTime now) {
		LocalTime time = decode(start);
		return (ChronoUnit.MINUTES.between(time, now) == 0);
	}
	
	/** Check if a given time matches the program's start day */
	private boolean checkDayMatch(LocalDateTime t) {
		DayOfWeek weekday_t = t.getDayOfWeek();
		int day_t = t.getDayOfMonth();
		Month month_t = t.getMonth();

		// check day match
		int days = plan.getDays();
		switch (plan.getType()) {
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
		EvenOddRestriction oddeven = plan.getOddeven();
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
		if (!plan.isEnabled()) {
			return false;
		}

		LocalTime time = t.toLocalTime();

		// first assume program starts today
		if (checkDayMatch(t)) {
			List<StartTime> starttimes = plan.getStarttimes();
			
			int repeat = plan.getRepeat();
			int delay = plan.getDelay();
			
			// t matches the program's start day
			if (repeat == 0) {
				for (StartTime s : starttimes) {
					if (matches(s, time)) {
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
				if (matches(start, time))
					return true;

				// otherwise, current_minute must be larger than start time, and interval must
				// be non-zero
				if (offset(t.toLocalTime()) > offset(decode(start)) && delay > 0) {
					LocalTime st = decode(start);
					for (int loop = 1; loop <= repeat; loop++) {
						st = st.plusMinutes(delay);
						if (ChronoUnit.MINUTES.between(st, time) == 0) {
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

	public boolean isWeatherDependent() {
		return plan.isWeatherDependent();
	}

	public boolean isSequential() {
		return plan.isSequential();
	}
	
	public boolean isEnabled() {
		return plan.isEnabled();
	}
	
	public int getPid() {
		return plan.getPid();
	}
	
	public Integer getMainValveId() {
		return plan.getMainValve();
	}
	
	public Plan getPlan() {
		return plan;
	}

	public Set<Entry<Integer, Short>> getDurations() {
		return plan.getDurations();
	}
}
