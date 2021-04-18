package de.operatorplease.sprinkler;

public abstract class Sensor {
	private String sid;
	
	public static final int BUTTON_NONE = 0;
	public static final int BUTTON_SELECT = 1;
	public static final int BUTTON_ENTER = 2;
	public static final int BUTTON_ESC = 3;
	public static final int BUTTON_MODE = 4;
	
	public enum TYPE {
		
		// unit: binary, 1 wet, 0 dry
		SENSOR_TYPE_RAIN_CLICK,
		
		// unit: 1/10 mm, range 0 to 16500
		SENSOR_TYPE_RAIN,
		
		// unit: 1/10 °C, range -4000 to 16500
		SENSOR_TYPE_TEMPERATURE,
		
		// unit: 1/10 %, range 0 to 1000
		SENSOR_TYPE_HUMIDITY,
		
		SENSOR_TYPE_FLOW,
		
		SENSOR_TYPE_SOIL,
		
		// unit: bits set by button, range 0-4
		SENSOR_TYPE_PSWITCH
	}
	
	protected Sensor(String sid) {
		this.sid = sid;
	}
	
	public abstract TYPE getType();

	public String getSid() {
		return sid;
	}
	
	public abstract int getValue();
}
