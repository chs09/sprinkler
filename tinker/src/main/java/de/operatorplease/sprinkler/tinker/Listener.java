package de.operatorplease.sprinkler.tinker;

import com.tinkerforge.IPConnection;
import com.tinkerforge.BrickletDualButtonV2;
import com.tinkerforge.BrickletOLED128x64V2;
import com.tinkerforge.BrickletHumidityV2;
import com.tinkerforge.BrickletBarometerV2;

class Listener implements IPConnection.EnumerateListener,
	                             IPConnection.ConnectedListener,
	                             BrickletDualButtonV2.StateChangedListener,
	                             BrickletHumidityV2.HumidityListener,
	                             BrickletBarometerV2.AirPressureListener {
	
	private final IPConnection ipcon;
	
	private BrickletOLED128x64V2 brickletLCD ;
	private BrickletHumidityV2 brickletHumidityV2;
	private BrickletBarometerV2 brickletBarometerV2;
	private BrickletDualButtonV2 brickletDualButtonV2;

	public Listener(IPConnection ipcon) {
		this.ipcon = ipcon;
	}

	@Override
	public void stateChanged(int buttonL, int buttonR, int ledL, int ledR) {
		if(brickletLCD != null) {
			String text = String.format("Button L=%d R=%d", buttonL, buttonR);

			try {
				brickletLCD.writeLine((short)1, (short)0, text);
				brickletDualButtonV2.setLEDState(1, 1);
			} catch(com.tinkerforge.TinkerforgeException e) {
			}
			
			System.out.println("Write to line 1: " + text);
		}
	}
	
	@Override
	public void humidity(int humidity) {
		if(brickletLCD != null) {
			float factor = 10.0f;

			if (brickletHumidityV2 != null) {
				factor = 100.0f; // FIXME: assuming that only one Humiditiy Bricklet (2.0) is connected
			}

			String text = String.format("Humidity   %6.2f %%", humidity/factor);

			try {
				brickletLCD.writeLine((short)1, (short)0, text);
			} catch(com.tinkerforge.TinkerforgeException e) {
			}

			System.out.println("Write to line 1: " + text);
		}
	}

	@Override
	public void airPressure(int airPressure) {
		if(brickletLCD != null) {
			String text = String.format("Air Press %7.2f mb", airPressure/1000.0);
			try {
				brickletLCD.writeLine((short)2, (short)0, text);
			} catch(com.tinkerforge.TinkerforgeException e) {
			}

			System.out.println("Write to line 2: " + text);

			int temperature;
			try {
				if (brickletBarometerV2 != null) {
					temperature = brickletBarometerV2.getTemperature();
				} else  {
					temperature = 0;
				}
			} catch(com.tinkerforge.TinkerforgeException e) {
				System.out.println("Could not get temperature: " + e);
				return;
			}

			// 0xDF == ° on LCD 20x4 charset
			text = String.format("Temperature %5.2f %cC", temperature/100.0, 0xDF);
			try {
				brickletLCD.writeLine((short)3, (short)0, text);
			} catch(com.tinkerforge.TinkerforgeException e) {
			}

			System.out.println("Write to line 3: " + text.replace((char)0xDF, '°'));
		}
	}

	@Override
	public void enumerate(String uid, String connectedUid, char position,
	                      short[] hardwareVersion, short[] firmwareVersion,
	                      int deviceIdentifier, short enumerationType) {
		if(enumerationType == IPConnection.ENUMERATION_TYPE_CONNECTED ||
		   enumerationType == IPConnection.ENUMERATION_TYPE_AVAILABLE) {
			if(deviceIdentifier == BrickletOLED128x64V2.DEVICE_IDENTIFIER) {
				try {
					brickletLCD = new BrickletOLED128x64V2(uid, ipcon);
					brickletLCD.clearDisplay();
					brickletLCD.writeLine((short) 0, (short) 0, "Welcome Sprinkler V1");
					System.out.println("Display initialized");
				} catch(com.tinkerforge.TinkerforgeException e) {
					brickletLCD = null;
					System.out.println("Display init failed: " + e);
				}
			} else if(deviceIdentifier == BrickletHumidityV2.DEVICE_IDENTIFIER) {
				try {
					brickletHumidityV2 = new BrickletHumidityV2(uid, ipcon);
					brickletHumidityV2.setHumidityCallbackConfiguration(1000, true, 'x', 0, 0);
					brickletHumidityV2.addHumidityListener(this);
					System.out.println("Humidity 2.0 initialized");
				} catch(com.tinkerforge.TinkerforgeException e) {
					brickletHumidityV2 = null;
					System.out.println("Humidity 2.0 init failed: " + e);
				}
			} else if(deviceIdentifier == BrickletBarometerV2.DEVICE_IDENTIFIER) {
				try {
					brickletBarometerV2 = new BrickletBarometerV2(uid, ipcon);
					brickletBarometerV2.setAirPressureCallbackConfiguration(1000, false, 'x', 0, 0);
					brickletBarometerV2.addAirPressureListener(this);
					System.out.println("Barometer 2.0 initialized");
				} catch(com.tinkerforge.TinkerforgeException e) {
					brickletBarometerV2 = null;
					System.out.println("Barometer 2.0 init failed: " + e);
				}
			} else if(deviceIdentifier == BrickletDualButtonV2.DEVICE_IDENTIFIER) {
				try {
					brickletDualButtonV2 = new BrickletDualButtonV2(uid, ipcon);
					brickletDualButtonV2.setStateChangedCallbackConfiguration(true);
					brickletDualButtonV2.addStateChangedListener(this);
					System.out.println("Barometer 2.0 initialized");
				} catch(com.tinkerforge.TinkerforgeException e) {
					brickletBarometerV2 = null;
					System.out.println("Barometer 2.0 init failed: " + e);
				}
			}
		}
	}

	@Override
	public void connected(short connectedReason) {
		if(connectedReason == IPConnection.CONNECT_REASON_AUTO_RECONNECT) {
			System.out.println("Auto Reconnect");

			while(true) {
				try {
					ipcon.enumerate();
					break;
				} catch(com.tinkerforge.NotConnectedException e) {
				}

				try {
					Thread.sleep(1000);
				} catch(InterruptedException ei) {
				}
			}
		}
	}
}