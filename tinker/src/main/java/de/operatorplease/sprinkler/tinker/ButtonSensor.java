package de.operatorplease.sprinkler.tinker;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tinkerforge.BrickletDualButtonV2;
import com.tinkerforge.TinkerforgeException;

import de.operatorplease.sprinkler.Sensor;

public class ButtonSensor extends Sensor implements BrickletDualButtonV2.StateChangedListener {
	private final Logger logger = Logger.getLogger(ButtonSensor.class.getSimpleName());
	
	private BrickletDualButtonV2 brickletDualButtonV2;
	private AtomicInteger value = new AtomicInteger();
	private AtomicInteger state = new AtomicInteger();

	private final Thread btnDebounceThread = new Thread("Button-Debounce-Thread") {
		private final int DELAY_MILLIS = 10;
		private final int HOLD_THRESHOLD = 30;
		
		public void run() {
			while(true) try {
				int current;
				synchronized(state) {
					current = state.get();
					if(current == 0) {
						logger.finest("sleeping");
						state.wait();
					}
				}
				logger.finest("awake");
				int count = 0;
				if(current == 0) {
					current = state.get();
				}
				while(current != 0) {
					Thread.sleep(DELAY_MILLIS);
					int now = state.get();
					
					if(now == BUTTON_SELECT && count >= HOLD_THRESHOLD) {
						// If the button is held down, the button code changes to 
						// BUTTON_MODE after 300ms
						now = current = BUTTON_MODE;
					}
					
					logger.finest("key check " + current + " " + now);
					
					// after 100ms the value must still be stable to be 
					// considered sure that the key code does not change anymore
					if(now == current) {
						if( count == HOLD_THRESHOLD ) {
							// Trigger anyway (even if the key is held) after 3 passes (300ms)
							logger.info("debounced key " + now + " (hold)");
							value.set(current);
						}
						count++;
					} else {
						// set value when releasing the key if at least two debounce 
						// cycle has been run through
						if(count > 1) {
							// if count >= THRESHOLD, then the event has already been triggered
							if(count < HOLD_THRESHOLD) {
								logger.info("debounced key " + current);
								value.set(current);
							}
						}
						// reset current key, start debouncing again
						current = now;
						count = 0;
					}
				}
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, "key debounce thread stopped due to interruption", e);
				return;
			}
		};
	};
	
	public ButtonSensor(BrickletDualButtonV2 brickletDualButtonV2) throws TinkerforgeException {
		super(Uid.of(brickletDualButtonV2.getIdentity()));
		this.brickletDualButtonV2 = brickletDualButtonV2;
		this.brickletDualButtonV2.setStateChangedCallbackConfiguration(true);
		this.brickletDualButtonV2.addStateChangedListener(this);
		this.btnDebounceThread.setDaemon(true);
		this.btnDebounceThread.start();
	}

	@Override
	public TYPE getType() {
		return TYPE.SENSOR_TYPE_PSWITCH;
	}

	@Override
	public int getValue() {
		return this.value.getAndSet(0);
	}
	
	@Override
	public void stateChanged(int buttonL, int buttonR, int ledL, int ledR) {
		String text = String.format("Button L=%b R=%b", buttonL == 0, buttonR == 0);
		synchronized(state) {
			if(buttonL == 0 && buttonR == 0) {
				state.set(BUTTON_ESC);
			} else if(buttonL == 0) {
				state.set(BUTTON_SELECT);
			} else if(buttonR == 0) {
				state.set(BUTTON_ENTER);
			} else {
				state.set(BUTTON_NONE);
			}
			state.notify();
		}
		try {
			brickletDualButtonV2.setLEDState(1, 1);
		} catch (com.tinkerforge.TinkerforgeException e) {
		}
		logger.fine(text);
	}
}
