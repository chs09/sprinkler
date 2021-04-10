package de.operatorplease.sprinkler.demo;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import de.operatorplease.sprinkler.Clock;
import de.operatorplease.sprinkler.Controller;

public class Demo {

	public static void main(String[] args) {
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
		
		controller.run();
	}
}
