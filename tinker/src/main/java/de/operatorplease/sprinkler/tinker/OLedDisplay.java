package de.operatorplease.sprinkler.tinker;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.tinkerforge.BrickletOLED128x64V2;
import com.tinkerforge.TinkerforgeException;

import de.operatorplease.sprinkler.Display;
import de.operatorplease.sprinkler.Status;

public class OLedDisplay extends Display {
	private final BrickletOLED128x64V2 oled;
	
	public OLedDisplay(BrickletOLED128x64V2 oled) throws TinkerforgeException {
		this.oled = oled;
		oled.clearDisplay();
		oled.writeLine(0, 0, "Welcome");
	}
	
	private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EE dd.MM.yy HH:mm   ");
	
	@Override
	public void printTime(LocalDateTime time) {
		try {
			oled.writeLine(7, 0, time.format(dateTimeFormatter));
		} catch (Exception e) {
			// TODO logger
			e.printStackTrace();
		}
	}
	
	public void updateStatus(Status status) {
		
	}
}
