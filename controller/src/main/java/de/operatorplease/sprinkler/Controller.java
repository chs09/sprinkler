package de.operatorplease.sprinkler;

import static de.operatorplease.sprinkler.Notifier.LogdataType.LOGDATA_RAINDELAY;
import static de.operatorplease.sprinkler.Notifier.MessageType.NOTIFY_PROGRAM_SCHED;
import static de.operatorplease.sprinkler.Notifier.MessageType.NOTIFY_RAINDELAY;
import static de.operatorplease.sprinkler.Sensor.TYPE.SENSOR_TYPE_FLOW;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.operatorplease.sprinkler.Notifier.LogdataType;
import de.operatorplease.sprinkler.Notifier.MessageType;
import de.operatorplease.sprinkler.Sensor.TYPE;
import de.operatorplease.sprinkler.settings.Plan;

public class Controller implements Runnable {
	private final Logger logger = Logger.getLogger(Controller.class.getName());
	
	private long flowpoll_timeout;
	
	private LocalDateTime next_weather_check;
	private LocalDateTime rd_stop_time;

	private final Notifier notifier = new Notifier();
	private final Scheduler scheduler = new Scheduler();
	private final Status status = new Status();
	
	private Display display = new Display();
	private Weather weather = new Weather();
	private Clock clock = new Clock();
	private List<Sensor> sensors = Collections.emptyList();
	private List<Program> programs = Collections.emptyList();
	private List<Station> stations = Collections.emptyList();

	public void setDisplay(Display display) {
		this.display = display;
	}
	
	public void setWeather(Weather weather) {
		this.weather = weather;
	}
	
	public void setClock(Clock clock) {
		this.clock = Objects.requireNonNull(clock);
	}
	
	public void setStations(List<Station> stations) {
		if(stations != null) {
			this.stations = stations;
		}
	}
	
	public void setSensors(List<Sensor> sensors) {
		this.sensors = (sensors == null) ? Collections.emptyList() : sensors;
	}
	
	public void setPrograms(List<Plan> programs) {
		this.programs = programs == null ? Collections.emptyList() : programs.stream().map(p -> new Program(p)).collect(Collectors.toList());
	}
	
	private void resetAllZonesImmediate() {
		for (Station zone : stations) {
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
	
	private void flowPoll() {

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
				handleMainValve(runningProgram);
				
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
				
				handleMainValve(runningProgram);
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
			
			for(Entry<Integer, Short> entry: runningProgram.getDurations()) {
				int zid = entry.getKey();
				int duration = entry.getValue();
				
				Optional<Station> zone = stations.stream().filter(s -> s.getZoneId() == zid).findFirst();
				if (zone.isEmpty()) {
					logger.warning("Zone " + zid + " not available.");
					continue;
				}
				
				Station station = zone.get();
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
				
				logger.info(now + " checking " + programs.size() + " programs");
				
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
			runningProgram = null;
			queue.clear();
		}
	}
	


	@Override
	public void run() {
		// handle flow sensor using polling every 1ms (maximum freq 1/(2*1ms)=500Hz)
		flowpoll_timeout = 0;
		
		Controller controller = new Controller();
		while(true) {
			try {
				controller.do_loop();

				// The main control loop runs once every second
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, "interrupted");
				return;
			} catch (Exception e) {
				logger.log(Level.WARNING, "controller error", e);
			}
		}
	}
		
	private void do_loop() {
		// check flow sensor
		Optional<Sensor> flowSensor = sensors.stream()
				.filter(s -> s.getType() == SENSOR_TYPE_FLOW)
				.findFirst();
		
		if (flowSensor.isPresent()) {
			long curr = System.currentTimeMillis();
			if (curr != flowpoll_timeout) {
				flowpoll_timeout = curr;
				flowPoll();
			}
		}

		heartbeat();

		final LocalDateTime now = clock.now();

		if (display != null) {
			display.printTime(now);
			display.updateStatus(status);
		}

		// ====== Check rain delay status ======
		checkRainDelay(now);

		// ====== Check binary (i.e. rain or soil) sensor status ======
		//detect_binarysensor_status();

		for (Sensor sensor : sensors) {
			if (sensor.isActive()) {
				notifier.push_message(MessageType.NOTIFY_SENSOR, sensor.getSid(), LogdataType.LOGDATA_ONOFF, 1);
			} else {
				notifier.push_message(MessageType.NOTIFY_SENSOR, sensor.getSid(), LogdataType.LOGDATA_ONOFF, 0);
			}
		}

		// ====== schedule program data ======
		scheduler.schedule(now);
		
		// ===== Check program switch status =====
		Optional<Sensor> pswitch = sensors.stream().filter(s -> s.getType() == TYPE.SENSOR_TYPE_PSWITCH).findFirst();
		if (pswitch.isPresent()) {
			Sensor button = pswitch.get();
			int value = (int) button.getValue();
			if ((value & 0b11) == 0b11) {
				resetAllZonesImmediate(); // immediately stop all zones
				scheduler.reset();
			}
			else if ((value & 0b01) != 0) {
//					if(pd.nprograms > 0)	manual_start_program(1, 0);
			}
			else if ((value & 0b10) != 0) {
//					if(pd.nprograms > 1)	manual_start_program(2, 0);
			}
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
		logger.info("heartbeat");
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

	private void handleMainValve(Program runningProgram) {
		Integer mainValveId = runningProgram.getMainValveId();
		if(mainValveId == null) {
			return;
		}
		
		Optional<Station> zone = stations.stream().filter(s -> s.getZoneId() == mainValveId).findFirst();
		if(zone.isEmpty()) {
			return;
		}

		Station main = zone.get();
		boolean running = stations.stream()
				.anyMatch( s -> s.isActive() && s.getZoneId() != mainValveId );

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
