package de.operatorplease.sprinkler;

import java.util.logging.Logger;

import de.operatorplease.sprinkler.weather.Weather;
import de.operatorplease.sprinkler.weather.WeatherStore;

/**
 * <p>Weather based sprinkler adjustment decision maker
 * 
 * <p>The formula used to set the scale is as follows:
 * <ul>
 * 
 * <li><b>Humidity</b>: Average humidity for the previous day above 30% is subtracted 
 * from the weather adjustment, below 30% is added to it. 
 * <p>Example: 
 * <br>77% max humidity + 26% min humidity / 2 = 51% avg humidity, 
 * <br>30% neutral humidity - 51% avg humidity = -21% humidity adjustment
 * 
 * <li><b>Temperature</b>: +4% for each degree above 21°C, and -4% for each degree below 21°C.
 * 
 * <li><b>Precipitation</b>: -0.8% for one hundredths of a mm precipitation from today and yesterday. 
 * <p>Example:
 * <br>0.3mm rain today + 0.12mm rain yesterday * 100 = 42 hundredths mm of rain,
 * <br>42 * -0.8 = -34% precipitation adjustment
 * 
 * </ul>
 * 
 * @implNote The total weather adjustment will not exceed the range of 0% to 200%.
 * 
 * <p>Both the weather adjustment (if enabled via a plan) and the seasonal 
 * adjustment are used to adjust the run time for each zone. For example, 
 * if the seasonal adjustment is 50% and the weather adjustment is 200%, 
 * they will add to make 100% so the run times will not be changed.
 */
public class Adjuster {
	private final Logger logger = Logger.getLogger(Adjuster.class.getSimpleName());
	
	// TODO use weather data if available,
	// use today forecast before sunset and sunrise
	// use today's avg between sunrise and sunset
	// alt: use forecast before 13:00
	
	private final WeatherStore weatherStore;
	
	public Adjuster() {
		weatherStore = Weather.getStore();
	}

	public float getAdjustment() {
		return 1.0f;
	}
	
	class Vals {
		public int maxhumidity = NEUTRAL_HUMIDITY;
		public int minhumidity = NEUTRAL_HUMIDITY;
		public int meantempi = NEUTRAL_TEMP_C;
		public int precipi = 0;
		public int precip_today = 0;
	}
	
	private static final int NEUTRAL_HUMIDITY = 30;
	private static final int NEUTRAL_TEMP_C = 21;

	int getScale(Vals vals)
	{
		final int humid_factor = NEUTRAL_HUMIDITY - (vals.maxhumidity + vals.minhumidity) / 2;
		final int temp_factor = (vals.meantempi - NEUTRAL_TEMP_C) * 4;
		final int rain_factor = (int)((vals.precipi + vals.precip_today) * -0.8);
		
		int adj = Math.min(Math.max(0, 100+humid_factor+temp_factor+rain_factor), 200);
		logger.info(String.format("Adjusting H(%d) T(%d) R(%d): %d", humid_factor, temp_factor, rain_factor, adj));
		return adj;
	}
}
