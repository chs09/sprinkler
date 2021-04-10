package de.operatorplease.sprinkler;

public abstract class Sensor {
	private int sid;
	
	public enum TYPE {
		SENSOR_TYPE_RAIN,
		SENSOR_TYPE_TEMPERATURE,
		SENSOR_TYPE_FLOW,
		SENSOR_TYPE_SOIL,
		SENSOR_TYPE_PSWITCH
	}
	
	private Sensor() {
		// use Sensor.get(num);
	}
	
	public abstract TYPE getType();

	public int getSid() {
		return sid;
	}
	
	public boolean isDigital() {
		return true;
	}
	
	public boolean isAnalog() {
		return !isDigital();
	}
	
	public boolean isActive() {
		if(isDigital()) {
			return getValue() > 0.5f;
		}
		return true;
	}
	
	public float getValue() {
		return 1f;
	}
}
