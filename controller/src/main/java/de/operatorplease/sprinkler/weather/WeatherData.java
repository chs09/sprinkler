package de.operatorplease.sprinkler.weather;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Weather data of a whole day.
 * Some values are average values, partly supplemented by min and max values.
 */
public class WeatherData extends WeatherDataPoint {
	/**
	 * Date to which this record belongs
	 */
	private LocalDate date;
	
	/**
	 * Min daily temperature
	 */
	private Float minTemp;
	
	/**
	 * Max daily temperature
	 */
	private Float maxTemp;
	
	public WeatherData() {
		this.date = LocalDate.now();
	}
	
	public WeatherData(LocalDate date) {
		this.date = Objects.requireNonNull(date);
	}
	
	public LocalDate getDate() {
		return date;
	}
	
	public Float getMaxTemp() {
		return maxTemp;
	}

	public Float getMinTemp() {
		return minTemp;
	}
	
	public void setDate(LocalDate date) {
		this.date = Objects.requireNonNull(date);
	}
	
	public void setMaxTemp(Float maxTemp) {
		this.maxTemp = round(maxTemp);
	}
	
	public void setMinTemp(Float minTemp) {
		this.minTemp = round(minTemp);
	}
}