package de.operatorplease.sprinkler.weather;

import java.time.LocalDate;
import java.util.Objects;

public class Data extends DataPoint {
	private LocalDate date;
	private Float minTemp;
	private Float maxTemp;
	private Float nightTemp;
	
	public Data() {
		this.date = LocalDate.now();
	}
	
	public Data(LocalDate date) {
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
	
	public Float getNightTemp() {
		return nightTemp;
	}
	
	public boolean isSameDate(LocalDate date) {
		return this.date.isEqual(date);
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
	
	public void setNightTemp(Float nightTemp) {
		this.nightTemp = round(nightTemp);
	}
}