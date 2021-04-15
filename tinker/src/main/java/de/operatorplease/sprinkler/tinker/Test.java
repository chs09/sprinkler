package de.operatorplease.sprinkler.tinker;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.tinkerforge.BrickletDualButtonV2;
import com.tinkerforge.BrickletHumidityV2;
import com.tinkerforge.BrickletIndustrialQuadRelayV2;
import com.tinkerforge.BrickletOLED128x64V2;
import com.tinkerforge.IPConnection;

import de.operatorplease.sprinkler.Clock;
import de.operatorplease.sprinkler.Controller;

public class Test {
	private static final String HOST = "192.168.178.50";
	private static final int PORT = 4223;
	private static IPConnection ipcon = null;
	private static Enumerator listener = null;

	public static void main(String[] args) {
		new Test().run();
	}
	
	private Controller controller;
	
	private void run() {
		controller = new Controller();
		
		ipcon = new IPConnection();
		while(true) {
			try {
				ipcon.connect(HOST, PORT);
				break;
			} catch(com.tinkerforge.TinkerforgeException e) {
			}

			try {
				Thread.sleep(1000);
			} catch(InterruptedException ei) {
			}
		}

		listener = new Enumerator(ipcon);
		ipcon.addEnumerateListener(listener);
		ipcon.addConnectedListener(listener);

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

		// TODO remove, for demo purposes use a clock, with 6 times speed
		controller.setClock(new Clock() {
			private final LocalDateTime start = LocalDateTime.now();
			@Override
			public LocalDateTime now() {
				LocalDateTime now = LocalDateTime.now();
				long seconds = ChronoUnit.SECONDS.between(start, now);
				// increment minutes every 10 seconds
				return start.plusMinutes(seconds / 10).withSecond(now.getSecond());
			}
		});

		//controller.setZones(null, null);
		//controller.setPrograms(null);

		try {
			controller.run();
			System.out.println("Press key to exit"); System.in.read();
		} catch(Exception e) {
			
		} finally {
			try {
				ipcon.disconnect();
			} catch(com.tinkerforge.NotConnectedException e) {
			}
		}
	}
	
	class Enumerator implements IPConnection.EnumerateListener, IPConnection.ConnectedListener {
		private final IPConnection ipcon;

		public Enumerator(IPConnection ipcon) {
			this.ipcon = ipcon;
		}

		@Override
		public void enumerate(String uid, String connectedUid, char position,
				short[] hardwareVersion, short[] firmwareVersion,
				int deviceIdentifier, short enumerationType) {
			if(enumerationType == IPConnection.ENUMERATION_TYPE_CONNECTED ||
					enumerationType == IPConnection.ENUMERATION_TYPE_AVAILABLE) {
				if(deviceIdentifier == BrickletOLED128x64V2.DEVICE_IDENTIFIER) {
					try {
						controller.setDisplay(new OLedDisplay(new BrickletOLED128x64V2(uid, ipcon)));
						System.out.println("OLED 128x64 initialized");
					} catch(com.tinkerforge.TinkerforgeException e) {
						System.out.println("OLED init failed: " + e);
					}
				} else if(deviceIdentifier == BrickletHumidityV2.DEVICE_IDENTIFIER) {
					try {
						BrickletHumidityV2 brickletHumidityV2 = new BrickletHumidityV2(uid, ipcon);
						controller.addSensor(new HumiditySensor(brickletHumidityV2));
						controller.addSensor(new TemperatureSensor(brickletHumidityV2));
						System.out.println("Humidity 2.0 initialized");
					} catch(com.tinkerforge.TinkerforgeException e) {
						System.out.println("Humidity 2.0 init failed: " + e);
					}
//				} else if(deviceIdentifier == BrickletBarometerV2.DEVICE_IDENTIFIER) {
//					try {
//						brickletBarometerV2 = new BrickletBarometerV2(uid, ipcon);
//						brickletBarometerV2.setAirPressureCallbackConfiguration(1000, false, 'x', 0, 0);
//						brickletBarometerV2.addAirPressureListener(this);
//						System.out.println("Barometer 2.0 initialized");
//					} catch(com.tinkerforge.TinkerforgeException e) {
//						brickletBarometerV2 = null;
//						System.out.println("Barometer 2.0 init failed: " + e);
//					}
				} else if(deviceIdentifier == BrickletDualButtonV2.DEVICE_IDENTIFIER) {
					try {
						BrickletDualButtonV2 bricklet = new BrickletDualButtonV2(uid, ipcon);
						ButtonSensor buttonSensor = new ButtonSensor(bricklet);
						System.out.println("Dual Button 2.0 initialized " + deviceIdentifier);
						
						controller.addSensor(buttonSensor);
					} catch(com.tinkerforge.TinkerforgeException e) {
						System.out.println("Dual Button 2.0 init failed: " + e);
					}
				} else if(deviceIdentifier == BrickletIndustrialQuadRelayV2.DEVICE_IDENTIFIER) {
					try {
						BrickletIndustrialQuadRelayV2 bricklet = new BrickletIndustrialQuadRelayV2(uid, ipcon);
						bricklet.setStatusLEDConfig(BrickletIndustrialQuadRelayV2.STATUS_LED_CONFIG_OFF);
						QuadStationRelay stationRelay = new QuadStationRelay(bricklet);
						System.out.println("Quad Relay 2.0 initialized " + deviceIdentifier);

						controller.addStations(stationRelay.getStations());
					} catch(com.tinkerforge.TinkerforgeException e) {
						System.out.println("Dual Button 2.0 init failed: " + e);
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
}
