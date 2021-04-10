package de.operatorplease.sprinkler.tinker;

import com.tinkerforge.BrickletHumidityV2;
import com.tinkerforge.TinkerforgeException;

import de.operatorplease.sprinkler.Sensor;

public class HumiditySensor extends Sensor implements BrickletHumidityV2.HumidityListener {
	private BrickletHumidityV2 brickletHumidityV2;

	public HumiditySensor(BrickletHumidityV2 brickletHumidityV2) throws TinkerforgeException {
		super(brickletHumidityV2.getIdentity().deviceIdentifier);
		this.brickletHumidityV2 = brickletHumidityV2;
		this.brickletHumidityV2.setHumidityCallbackConfiguration(60_000, false, 'x', 0, 10_000);
		this.brickletHumidityV2.addHumidityListener(this);
	}

	private int value;
	
	@Override
	public TYPE getType() {
		return TYPE.SENSOR_TYPE_HUMIDITY;
	}

	@Override
	public int getValue() {
		return value;
	}

	@Override
	public void humidity(int humidity) {
		value = humidity;
	}
}
