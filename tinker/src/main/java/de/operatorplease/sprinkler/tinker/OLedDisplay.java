package de.operatorplease.sprinkler.tinker;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.tinkerforge.BrickletOLED128x64V2;
import com.tinkerforge.TinkerforgeException;

import de.operatorplease.sprinkler.Display;
import de.operatorplease.sprinkler.Status;

public class OLedDisplay extends Display {
	private static long TIMEOUT = TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS);

	private final BrickletOLED128x64V2 oled;
	private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EE dd.MM.yy HH:mm   ");
	private final AtomicBoolean off = new AtomicBoolean();
	
	public OLedDisplay(BrickletOLED128x64V2 oled) throws TinkerforgeException {
		this.oled = oled;
		oled.clearDisplay();
		oled.writeLine(0, 0, "Welcome");
	}
	
	private int lastStatusHashCode;
	
	// TODO configuration
	private boolean alwaysOn = false;
	private long lastActivation;
	private String lastTime = "";
	
	@Override
	public void printTime(LocalDateTime time) {
		try {
			if(!off.get()) {
				String formatted = time.format(dateTimeFormatter);
				if(!formatted.equals(lastTime))
					oled.writeLine(7, 0, formatted);
				lastTime = formatted;
			} else {
				lastTime = "";
			}
		} catch (Exception e) {
			// TODO logger
			e.printStackTrace();
		}
	}
		
	public void updateStatus(Status status) {
		try {
			// check status modified
			if(alwaysOn) {
				lastActivation = System.currentTimeMillis();
			} 
			else if(status.lastUserInput > lastActivation) {
				lastActivation = status.lastUserInput;
			}
			else if(status.hashCode() != lastStatusHashCode) {
				lastActivation = System.currentTimeMillis();
			}
			
			if(lastActivation + TIMEOUT < System.currentTimeMillis()) {
				if(off.getAndSet(true) == false) {
					// clear display to prevent burn-in effect
					oled.clearDisplay();
				}
				return;
			} else {
				off.set(false);
			}
			
			if(lastStatusHashCode == status.hashCode()) {
				// nothing changed
				return;
			}
			lastStatusHashCode = status.hashCode();
			String statusLine = String.format("%s %-8s %10s", 
					String.valueOf(status.mode),
					status.program,
					toString(status.duration)
					);
			
			oled.writeLine(2, 0, statusLine);
		} catch (Exception e) {
			// TODO logger
			e.printStackTrace();
		}
	}
}
