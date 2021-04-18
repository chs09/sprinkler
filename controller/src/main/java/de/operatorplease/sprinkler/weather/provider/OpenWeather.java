package de.operatorplease.sprinkler.weather.provider;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.operatorplease.sprinkler.weather.Weather;

public class OpenWeather extends Weather {
	private final Logger logger = Logger.getLogger(OpenWeather.class.getSimpleName());
	
	private String apiUrl = "api.openweathermap.org/data/2.5";
	private String apiKey;
	private String lat;
	private String lon;
	
	public OpenWeather(String apiKey, String lat, String lon) {
		this.apiKey = apiKey;
		this.lat = lat;
		this.lon = lon;
	}
	
	@Override
	public void check() {
		String api = String.format("https://%s/onecall?appid=%s&lat=%s&lon=%s&units=metric&exclude=alerts,minutely,hourly", 
				apiUrl, apiKey, lat, lon);
		logger.info("Calling weather api " + api);
		
		URL url;
		try {
			url = new URL(api);
		} catch (MalformedURLException e) {
			logger.log(Level.WARNING, "Malformed weather api url", e);
			return;
		}
		
		String response;
		try {
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json;charset=utf-8");
			conn.connect();
			
			int responsecode = conn.getResponseCode();
			if(responsecode != 200) {
				logger.warning("Weather api call unsuccessful. Response code: " + responsecode);
				return;
			}
			
			response = "";
			try(InputStream is = url.openStream(); Scanner scanner = new Scanner(is)) {
				while (scanner.hasNext()) {
					response += scanner.nextLine();
				}
			}
			
			final JsonNode weather = new ObjectMapper().readTree(response);
			final JsonNode current = weather.findPath("current");
			if(!current.isMissingNode()) {
				JsonNode currentTemp = current.findValue("temp");
				double t = currentTemp.asDouble();
				
				JsonNode humidity = current.findValue("humidity");
				double h = humidity.asDouble();
			}
			
		} catch (IOException e) {
			logger.log(Level.WARNING, "Weather api error", e);
			return;
		}
	}	
}
