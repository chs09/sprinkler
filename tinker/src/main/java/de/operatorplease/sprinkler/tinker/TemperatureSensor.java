package de.operatorplease.sprinkler.tinker;

import com.tinkerforge.BrickletHumidityV2;
import com.tinkerforge.TinkerforgeException;

import de.operatorplease.sprinkler.Sensor;

public class TemperatureSensor extends Sensor implements BrickletHumidityV2.TemperatureListener {
	private BrickletHumidityV2 brickletHumidityV2;

	public TemperatureSensor(BrickletHumidityV2 brickletHumidityV2) throws TinkerforgeException {
		super(brickletHumidityV2.getIdentity().deviceIdentifier);
		this.brickletHumidityV2 = brickletHumidityV2;
		this.brickletHumidityV2.setTemperatureCallbackConfiguration(60000, false, 'x', -4000, 16500);
		this.brickletHumidityV2.addTemperatureListener(this);
	}

	private int value;
	
	@Override
	public TYPE getType() {
		return TYPE.SENSOR_TYPE_TEMPERATURE;
	}

	@Override
	public int getValue() {
		return value;
	}
	
	@Override
	public void temperature(int temperature) {
		value = temperature;
	}
}
