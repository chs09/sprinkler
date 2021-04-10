package de.operatorplease.sprinkler.tinker;

import java.util.concurrent.atomic.AtomicInteger;

import com.tinkerforge.BrickletDualButtonV2;
import com.tinkerforge.TinkerforgeException;

import de.operatorplease.sprinkler.Sensor;

public class ButtonSensor extends Sensor implements BrickletDualButtonV2.StateChangedListener {
	private BrickletDualButtonV2 brickletDualButtonV2;
	private AtomicInteger value = new AtomicInteger();
	private int state;

	public ButtonSensor(BrickletDualButtonV2 brickletDualButtonV2) throws TinkerforgeException {
		super(brickletDualButtonV2.getIdentity().deviceIdentifier);
		this.brickletDualButtonV2 = brickletDualButtonV2;
		//		this.brickletDualButtonV2.setStateChangedCallbackConfiguration(true);
		this.brickletDualButtonV2.addStateChangedListener(this);
	}

	@Override
	public TYPE getType() {
		return TYPE.SENSOR_TYPE_PSWITCH;
	}

	@Override
	public int getValue() {
		return value.getAndSet(state);
	}

	@Override
	public void stateChanged(int buttonL, int buttonR, int ledL, int ledR) {
		String text = String.format("Button L=%d R=%d", buttonL, buttonR);

		if(buttonL == 0 && buttonR == 0) {
			value.set(3);
			state = 3;
		} else if(buttonL == 0) {
			value.set(1);
			state = 1;
		} else if(buttonR == 0) {
			value.set(2);
			state = 2;
		} else {
			state = 0;
		}

		try {
			brickletDualButtonV2.setLEDState(1, 1);
		} catch (com.tinkerforge.TinkerforgeException e) {
		}
		System.out.println("Write to line 1: " + text);
	}
}
