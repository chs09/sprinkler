package de.operatorplease.sprinkler.tinker;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.tinkerforge.IPConnection;

import de.operatorplease.sprinkler.Clock;
import de.operatorplease.sprinkler.Controller;

public class Test {
	private static final String HOST = "192.168.178.50";
	private static final int PORT = 4223;
	private static IPConnection ipcon = null;
	private static Listener listener = null;

	public static void main(String[] args) {
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

		listener = new Listener(ipcon);
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

		try {
			System.out.println("Press key to exit"); System.in.read();
		} catch(java.io.IOException e) {
		}

		try {
			ipcon.disconnect();
		} catch(com.tinkerforge.NotConnectedException e) {
		}


		Controller controller = new Controller();

		// for demo purposes use a clock, with 6 times speed
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
		//controller.setSensors(null);

//		controller.run();
	}
}
