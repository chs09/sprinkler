package de.operatorplease.sprinkler;

public abstract class Sensor {
	private int sid;
	
	public enum TYPE {
		SENSOR_TYPE_RAIN,
		
		// unit: 1/100 °C, range -40000 to 165000
		SENSOR_TYPE_TEMPERATURE,
		
		// unit: 1/100 %, range 0 to 10000
		SENSOR_TYPE_HUMIDITY,
		
		SENSOR_TYPE_FLOW,
		
		SENSOR_TYPE_SOIL,
		
		// unit: bits set by button, range 0-3
		SENSOR_TYPE_PSWITCH
	}
	
	protected Sensor(int sid) {
		this.sid = sid;
	}
	
	public abstract TYPE getType();

	public int getSid() {
		return sid;
	}
	
	public abstract int getValue();
}
