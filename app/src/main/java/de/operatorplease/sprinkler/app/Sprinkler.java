package de.operatorplease.sprinkler.app;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import de.operatorplease.sprinkler.Clock;
import de.operatorplease.sprinkler.Controller;
import de.operatorplease.sprinkler.tinker.TinkerControlThread;
import de.operatorplease.sprinkler.web.WebserverThread;

public class Sprinkler {
	private static final String HOST = "192.168.178.50";
	private static final int PORT = 4223;

	private static WebserverThread webserverThread;
	private static TinkerControlThread tinkerThread;
	private static Controller controller;

	public static void main(String[] args) {
		tinkerThread = new TinkerControlThread(HOST, PORT);
		controller = tinkerThread.getController();
		tinkerThread.start();
		
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
		
		webserverThread = new WebserverThread(controller, 80);
		webserverThread.start();
//		webserverThread.setUncaughtExceptionHandler(null)
	}
}
