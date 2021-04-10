package de.operatorplease.sprinkler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class Notifier {
	private final Logger logger = Logger.getLogger(Notifier.class.getName());
	
	enum MessageType {
		/** Notification macro defines */
		NOTIFY_PROGRAM_SCHED(0x0001),
		NOTIFY_SENSOR(0x0002),
		NOTIFY_FLOWSENSOR(0x0004),
		NOTIFY_WEATHER_UPDATE(0x0008),
		NOTIFY_REBOOT(0x0010),
		NOTIFY_RAINDELAY(0x0080),
		NOTIFY_ZONE(0x0100);
		
		private MessageType(int value) {
			this.value = value;
		}
		
		public final int value; 
	}
	
	enum LogdataType {
		LOGDATA_NONE(0x000),
		LOGDATA_ONOFF(0x001),
		LOGDATA_RAINDELAY(0x002),
		LOGDATA_WATERLEVEL(0x003),
		LOGDATA_FLOWSENSE(0x004),
		LOGDATA_CURRENT(0x080);
		
		private LogdataType(int value) {
			this.value = value;
		}
		
		public final int value;
	}
	
	private Map<UUID, Long> state = new HashMap<>();
	
	// ====== Check controller status changes and write log ======
	public void push_message(MessageType type, int which, LogdataType datatype, long value) {
		UUID id = new UUID(type.value, datatype.value);
		Long previous = state.put(id, value);
		
		boolean modified = previous != null && previous != value;
		if(modified) {
			logger.info("Notification " + type + "(" + which + ") " + datatype + ": " + value);
			
			// TODO send MQTT message
		}
	}
}
