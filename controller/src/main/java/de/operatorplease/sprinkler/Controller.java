package de.operatorplease.sprinkler;

import static de.operatorplease.sprinkler.Notifier.LogdataType.LOGDATA_RAINDELAY;
import static de.operatorplease.sprinkler.Notifier.MessageType.NOTIFY_PROGRAM_SCHED;
import static de.operatorplease.sprinkler.Notifier.MessageType.NOTIFY_RAINDELAY;
import static de.operatorplease.sprinkler.Sensor.TYPE.SENSOR_TYPE_FLOW;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.operatorplease.sprinkler.Notifier.LogdataType;
import de.operatorplease.sprinkler.Notifier.MessageType;
import de.operatorplease.sprinkler.Sensor.TYPE;
import de.operatorplease.sprinkler.settings.Plan;

public class Controller implements Runnable {
	private final Logger logger = Logger.getLogger(Controller.class.getTypeName());
	
	private LocalDateTime next_weather_check;
	private LocalDateTime rd_stop_time;
	private LocalDateTime resetModeAfter;

	private final Notifier notifier = new Notifier();
	private final Scheduler scheduler = new Scheduler();
	private final Status status = new Status();
	
	private Display display = new Display();
	private Weather weather = new Weather();
	private Clock clock = new Clock();
	
	private final List<Sensor> sensors = new CopyOnWriteArrayList<>();
	private final List<Program> programs = new CopyOnWriteArrayList<>();
	private final Map<String, Station> stations = new ConcurrentHashMap<>();

	public void setDisplay(Display display) {
		this.display = display;
	}
	
	public void setWeather(Weather weather) {
		this.weather = weather;
	}
	
	public void setClock(Clock clock) {
		this.clock = Objects.requireNonNull(clock);
	}
	
	public void addStation(Station station) {
		if(station != null) {
			this.stations.put(station.getZoneId(), station);
		}
	}
	
	public void addStations(List<Station> stations) {
		if(stations != null) {
			stations.forEach(this::addStation);
		}
	}
	
	public void addSensor(Sensor sensor) {
		if(sensor != null) {
			this.sensors.removeIf(s -> Objects.equals(s.getSid(), sensor.getSid()));
			this.sensors.add(sensor);
		}
	}
	
	public void addSensors(List<Sensor> sensors) {
		if(sensors != null) {
			sensors.forEach(this::addSensor);
		}
	}
	
	public void setPlans(List<Plan> programs) {
		this.programs.clear();
		programs.forEach(plan -> this.programs.add(new Program(plan)));
	}
	
	private void resetAllZonesImmediate() {
		logger.info("reset all zone immediate");
		for (Station zone : stations.values()) {
			zone.stop();
		}
	}

	private void checkRainDelay(LocalDateTime now) {		
		// ====== Check rain delay status ======
		if (status.rainDelayed) {
			if (rd_stop_time.isBefore(now)) { // rain delay is over
				status.rainDelayed = false;
				notifier.push_message(NOTIFY_RAINDELAY, 0, LOGDATA_RAINDELAY, 0);
			}
		}
		
		Optional<Sensor> rainSensor = sensors.stream()
				.filter(s -> s.getType() == TYPE.SENSOR_TYPE_RAIN)
				.findFirst();

		if(rainSensor.isPresent()) {
			if(rainSensor.get().getValue() != 0) {
				rd_stop_time = clock.now().plusHours(1);
				status.rainDelayed = true;
				notifier.push_message(NOTIFY_RAINDELAY, 0, LOGDATA_RAINDELAY, 1);		
			}
		}
	}
	
	private void flowPoll(LocalDateTime now) {
		// check flow sensor
		Optional<Sensor> flowSensor = sensors.stream()
				.filter(s -> s.getType() == SENSOR_TYPE_FLOW)
				.findFirst();

		if (flowSensor.isPresent()) {
			// TODO
		}
	}
	
	class Scheduler {
		private class RuntimeQueueStruct {
			long start;      // start time in ms
			long duration;   // water time in ms
			Station zone;
		};
		
		private Program runningProgram = null;
		private final LinkedList<Program> pendingPrograms = new LinkedList<>();
		private final LinkedList<RuntimeQueueStruct> queue = new LinkedList<>();
		
		private void scheduleZones() {
			// ====== Run program data ======
			// Check if a program is running currently
			// If so, do run-time keeping
			if (!queue.isEmpty()) {
				long curr_time = System.currentTimeMillis();
				
				// next, go through the zones and perform time keeping
				for (RuntimeQueueStruct q: queue) {
					// check if this zone is scheduled, either running or waiting to run
					if (q.start > 0) {
						// if so, check if we should turn it off
						if (curr_time >= q.start + q.duration) {
							turnOff(q.zone);
							// clear up finished element
							queue.remove(q);
						}
					}
					
					// if current zone is not running, check if we should turn it on
					if (!q.zone.isActive() && curr_time >= q.start && curr_time < q.start + q.duration) {
						turnOn(q.zone);
					} // if curr_time > scheduled_start_time
				}
				handleMainValve();
				
				long numberOfZonesRunning = queue.stream().filter(q -> q.start > 0).count();
				if(numberOfZonesRunning == 0 && !queue.isEmpty()) {
					RuntimeQueueStruct q = queue.getFirst();
					q.start = System.currentTimeMillis();
					turnOn(q.zone);
				}
				
//			// log flow sensor reading if flow sensor is used
//			if (os.iopts[IOPT_SENSOR1_TYPE] == SENSOR_TYPE_FLOW) {
//				write_log(LOGDATA_FLOWSENSE, curr_time);
//				push_message(NOTIFY_FLOWSENSOR,
//						(flow_count > os.flowcount_log_start) ? (flow_count - os.flowcount_log_start) : 0);
//			}
				
				handleMainValve();
			} // if_some_program_is_running
			
			if(!queue.isEmpty()) {
				// wait until previous program has finished
				return;
			} else {
				status.programRunning = null;
			}
			
			runningProgram = pendingPrograms.poll();
			if(runningProgram == null) {
				return;
			}
			status.programRunning = runningProgram.getPlan();
			status.program = "PRG" + runningProgram.getPid();
			
			for(Entry<String, Short> entry: runningProgram.getDurations().entrySet()) {
				String zid = entry.getKey();
				int duration = entry.getValue();
				
				Station station = stations.get(zid);
				if (station == null) {
					logger.warning("Zone " + zid + " not available.");
					continue;
				}
				
				if(station.isDisabled()) {
					continue;
				}
								
				if (duration > 0) {
					// water time is scaled by watering percentage
					long wateringTime = TimeUnit.MILLISECONDS.convert(duration, TimeUnit.MINUTES);
					
					// check weather, seasonal adjustment
					if(runningProgram.isWeatherDependent()) {
						float weatherAdjusment = weather.getAdjustment();
						if(weatherAdjusment < 0) {
							// reset to 100%
							weatherAdjusment = 1;
						}
						// max. 150%
						weatherAdjusment = Math.min(1.5f, weatherAdjusment);
						wateringTime = (long) (wateringTime * weatherAdjusment);
					}
					
					// check if water time is still valid
					// because it may end up being zero after scaling
					if (wateringTime > TimeUnit.SECONDS.toMillis(10)) {
						RuntimeQueueStruct q = new RuntimeQueueStruct();
						
						if(runningProgram.isSequential()) {
							q.start = 0;
						} else {
							// start all parallel
							q.start = System.currentTimeMillis();
						}
						q.duration = wateringTime;
						q.zone = station;
						
						queue.add(q);
					} // if water_time
				}
			}
		}

		private void add(Program prog) {
			// skip program if already assigned
			if(runningProgram != prog && !pendingPrograms.contains(prog)) {
				logger.info("selected program " + prog.getPid());
				pendingPrograms.add(prog);
				notifier.push_message(NOTIFY_PROGRAM_SCHED, prog.getPid(), LogdataType.LOGDATA_NONE, 0);
			}
		}

		private long last_minute;
		
		public void schedule(LocalDateTime now) {
			long curr_time = now.atZone(ZoneOffset.systemDefault()).toEpochSecond();
			long curr_minute = curr_time / 60;

			// since the granularity of start time is minute
			// we only need to check once every minute
			if (curr_minute != last_minute) {
				last_minute = curr_minute;
				
				logger.fine(now + " checking " + programs.size() + " programs");
				
				// check through all programs
				for (Program prog : programs) {
					if (!prog.matches(now)) {
						continue;
					}
					add(prog);				
				}

				// calculate start and end time
				scheduleZones();
			} // if_check_current_minute
		}

		public void reset() {
			logger.info("reset scheduler");
			runningProgram = null;
			queue.clear();
		}
	}

	@Override
	public void run() {
		long[] durations = new long[10];
		int cycle = 0;
		while(true) {
			try {
				Thread.sleep(300);

				// The main control loop
				long start = System.currentTimeMillis();
				do_loop();

				// calc avg loop time
				durations[cycle++] = System.currentTimeMillis()-start;
				if(cycle == durations.length) {
					OptionalDouble avg = Arrays.stream(durations).average();
					double avgTime = avg.getAsDouble();
					logger.log((avgTime > 30) ? Level.WARNING : Level.FINE, "avg main cycle time "+ avgTime + "ms.");
					cycle = 0;
				}
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, "interrupted");
				return;
			} catch (Exception e) {
				logger.log(Level.WARNING, "controller error", e);
			}
		}
	}
	
	public enum Mode {
		AUTOMATIC,
		PAUSED,
		ITERATE_PROGRAMS,
		ITERATE_STATIONS
	}
	
	private final State state = new State();
	private class State {
		int index = 0;
		private Mode __mode = Mode.AUTOMATIC;
		
		public State() {
			setMode(Mode.AUTOMATIC);
		}
		
		public Mode getMode() {
			if(resetModeAfter != null && resetModeAfter.isBefore(clock.now())) {
				setMode(Mode.AUTOMATIC);
			}
			return __mode;
		}
		
		public void setMode(Mode mode) {
			if(mode != Mode.PAUSED || (mode != Mode.AUTOMATIC && __mode != Mode.PAUSED)) {
				scheduler.reset();
			}
			
			__mode = mode;
			if(mode == Mode.PAUSED) {
				resetModeAfter = clock.now().plusMinutes(5);
			} else if(mode == Mode.AUTOMATIC) {
				resetModeAfter = null;
			} else {
				resetModeAfter = clock.now().plusHours(2);
			}
			
			resetAllZonesImmediate();
			switch(mode) {
			case AUTOMATIC: status.mode = 'A'; break;
			case ITERATE_PROGRAMS: status.mode = 'M'; break;
			case ITERATE_STATIONS: status.mode = 'S'; break;
			case PAUSED: status.mode = 'P'; break;
			}
			index = -1;
			next();
		}
		
		public void toggle() {
			switch(__mode) {
			case AUTOMATIC: state.setMode(Mode.ITERATE_PROGRAMS); break;
			case ITERATE_PROGRAMS: state.setMode(Mode.ITERATE_STATIONS); break;
			case ITERATE_STATIONS: state.setMode(Mode.ITERATE_PROGRAMS); break;
			default: state.setMode(Mode.PAUSED); break;
			case PAUSED: state.setMode(Mode.ITERATE_PROGRAMS);
 			}
		}
		
		public void next() {
			index++;
			if(__mode == Mode.ITERATE_PROGRAMS) {
				index = !programs.isEmpty() ? index % programs.size() : -1;
				status.program = "PRG" + index; // TODO use program pid
			} else if(__mode == Mode.ITERATE_STATIONS) {
				index = !stations.isEmpty() ? index % stations.size() : -1;
				status.program = "VLV" + index;
			} else {
				index = -1;
				status.program = "";
			}
		}
		
		public void exec() {
			if(__mode == Mode.ITERATE_PROGRAMS) {
				if(programs.size() > state.index) {
					scheduler.reset();
					scheduler.add(programs.get(state.index));
					setMode(Mode.AUTOMATIC);
				}
			}
			else if(__mode == Mode.ITERATE_STATIONS) {
				ArrayList<Station> list = new ArrayList<>(stations.values());
				if(list.size() > state.index) {
					list.sort(Comparator.comparing(Station::getZoneId));
					Station station = list.get(state.index);
					if(!station.isDisabled()) {
						station.toggle();
					}
				}
			} else {
				setMode(Mode.PAUSED);
			}
		}
	}
	
	private void handleButton(int which) {
		if (which == Sensor.BUTTON_NONE) {
			return;
		}
		
		status.lastUserInput = System.currentTimeMillis();
		
		if (which == Sensor.BUTTON_ESC) {
			logger.info("esc button");
			if(state.getMode() == Mode.AUTOMATIC) {
				scheduler.reset();
				state.setMode(Mode.PAUSED);
			} else {
				state.setMode(Mode.AUTOMATIC);
			}
		}
		else if (which == Sensor.BUTTON_MODE) {
			logger.info("mode button");
			state.toggle();
		}
		else if (which == Sensor.BUTTON_SELECT) {
			logger.info("select button");
			state.next();
		}
		else if (which == Sensor.BUTTON_ENTER) {
			logger.info("enter button");
			state.exec();
		}
	}
	
	private void do_loop() {
		heartbeat();

		final LocalDateTime now = clock.now();

		// ===== Check program switch status =====
		checkButtons();
		
		if (display != null) {
			display.printTime(now);
			display.updateStatus(status);
		}

		// ====== Check flow sensor, if preset ======
		flowPoll(now);
		
		// ====== Check rain delay status ======
		checkRainDelay(now);

		// ====== Check binary (i.e. rain or soil) sensor status ======
		for (Sensor sensor : sensors) {
			notifier.push_message(MessageType.NOTIFY_SENSOR, sensor.getType().ordinal(), LogdataType.LOGDATA_ONOFF, 1);
		}

		if(resetModeAfter != null && resetModeAfter.isAfter(now)) {
			status.duration = Duration.between(now, resetModeAfter);
		} else {
			status.duration = null;
		}
		
		// ====== schedule program data ======
		if(state.getMode() == Mode.AUTOMATIC) {
			scheduler.schedule(now);
		}
		
		// ====== check network connection ====== 
		checkNetwork();

		// ====== check weather ====== 
		if( next_weather_check == null || next_weather_check.isBefore(now) ) {
			next_weather_check = now.plusMinutes(30); 
			weather.check();
		}
	}

	private void heartbeat() {
		logger.finest("heartbeat");
	}

	private void checkButtons() {
		// ===== Check program switch status =====
		Optional<Sensor> pswitch = sensors.stream().filter(s -> s.getType() == TYPE.SENSOR_TYPE_PSWITCH).findFirst();
		if (pswitch.isPresent()) {
			Sensor button = pswitch.get();
			handleButton(button.getValue());
		}
	}
	
	private void checkNetwork() {
		// TODO Auto-generated method stub
		status.networkAvailable = true;
	}

	private void turnOn(Station zone) {
		logger.info("turning on " + zone);
		zone.start();
	}

	private void turnOff(Station zone) {
		logger.info("turning off " + zone);
		zone.stop();
	}

	private void handleMainValve() {
		Map<String, Set<String>> map = programs.stream()
				.filter(p -> Objects.nonNull(p.getMainValveId()))
				.collect(Collectors.toMap(Program::getMainValveId, p -> p.getDurations().keySet()));
		
		if(map.isEmpty()) {
			return;
		}
		
		for(Entry<String, Set<String>> entry: map.entrySet()) {
			String mainValveId = entry.getKey();

			if(mainValveId == null) {
				// no main station assigned
				continue;
			}

			Station main = stations.get(mainValveId);
			if(main == null) {
				// should not happen
				logger.warning("Main station " + mainValveId + " not found.");
				continue;
			}
			
			boolean running = false;
			for(String id: entry.getValue()) {
				Station station = stations.get(id);
				if(station != null && station.isActive()) {
					running = true;
					break;
				}
			}

			if(running) {
				if(!main.isActive()) {
					turnOn(main);
				}
			} else {
				if(main.isActive()) {
					turnOff(main);
				}
			}
		}
	}
}
