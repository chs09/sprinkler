package de.operatorplease.sprinkler.weather;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Snapshot for weather data at a point in time.
 */
public class WeatherDataPoint {
	/**
	 * Temperature in °C
	 */
	private Float temp;
	
	/**
	 * Humidity in %
	 */
	private Integer humidity;
	
	/**
	 * Atmospheric pressure on the sea level, hPa
	 */
	private Integer pressure;
	
	/**
	 * Atmospheric temperature (varying according to pressure and humidity) below 
	 * which water droplets begin to condense and dew can form
	 */
	private Float dew;
	
	/**
	 * rain in mm
	 */
	private Float precipitation;
	
	/**
	 * probability of precipitation, used in forecast
	 */
	private Integer precipitationProbability;

	public static Float round(Float value) {
		if(value != null) {
			return new BigDecimal(value).setScale(2, RoundingMode.FLOOR).floatValue();
		} else {
			return null;
		}
	}

	public Float getDew() {
		return dew;
	}
	
	public Integer getHumidity() {
		return humidity;
	}
	
	public Float getPrecipitation() {
		return precipitation;
	}
	
	public Integer getPrecipitationProbability() {
		return precipitationProbability;
	}
	
	public Integer getPressure() {
		return pressure;
	}

	public Float getTemp() {
		return temp;
	}
	
	public void setDew(Float dew) {
		this.dew = round(dew);
	}

	public void setHumidity(Integer humidity) {
		this.humidity = humidity;
	}

	public void setPrecipitation(Float precipitation) {
		this.precipitation = round(precipitation);
	}

	public void setPrecipitationProbability(Integer pop) {
		this.precipitationProbability = pop;
	}

	public void setPressure(Integer pressure) {
		this.pressure = pressure;
	}

	public void setTemp(Float temp) {
		this.temp = round(temp);
	}
}