package de.operatorplease.sprinkler.weather;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WeatherStore {
	private final Logger logger = Logger.getLogger(WeatherStore.class.getSimpleName());
	
	// data object to serialize / deserialize the state
	static class Store {
		public LocalDate currentDate;
		public WeatherDataPoint current;
		
		public final Map<LocalDateTime, WeatherDataPoint> today = new LinkedHashMap<>() {
			private static final long serialVersionUID = 8456961946275535312L;

			protected boolean removeEldestEntry(Map.Entry<LocalDateTime,WeatherDataPoint> eldest) {
				LocalDateTime time = eldest.getKey();
				return time.isBefore(time.minusHours(24));
			};
		};
		
		public final Map<LocalDate, WeatherData> history = new LinkedHashMap<>() {
			private static final long serialVersionUID = 8456961946275535312L;

			protected boolean removeEldestEntry(Map.Entry<LocalDate,WeatherData> eldest) {
				// do not store longer than 1 year
				// can be changed later
				return history.size() >= 366;
			};
		};

		public void setToday(Map<LocalDateTime, WeatherDataPoint> today) {
			if(today != null) {
				 this.today.clear();
				 this.today.putAll(today);
			}
		}
		
		public void setHistory(Map<LocalDate, WeatherData> history) {
			if(history != null) {
				this.history.clear();
				this.history.putAll(history);;
			}
		}
		
		public void setCurrent(WeatherDataPoint current) {
			this.current = current;
		}
		
		public void setCurrentDate(LocalDate currentDate) {
			this.currentDate = currentDate;
		}
		
		public WeatherDataPoint getCurrent() {
			return current;
		}
		
		public LocalDate getCurrentDate() {
			return currentDate;
		}
		
		public Map<LocalDate, WeatherData> getHistory() {
			return history;
		}
		
		public Map<LocalDateTime, WeatherDataPoint> getToday() {
			return today;
		}
	}
	
	private Store store;

	public WeatherStore() {
		try(FileInputStream is = new FileInputStream("weather.json")) {
			ObjectMapper mapper= new ObjectMapper();
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			store = mapper.readerFor(Store.class).readValue(is);
		} catch (Exception e) {
			store = new Store();
			if(e instanceof FileNotFoundException) {
				logger.info("waether data file does not exists: " + e.getMessage());
			} else {
				logger.log(Level.SEVERE, "could not read weather data store", e);
			}
		}
	}
	
	private DoubleStream mapToDouble(Stream<WeatherDataPoint> stream, Function<WeatherDataPoint, ? extends Number> map) {
		return stream
				.map(map)
				.filter(Objects::nonNull)
				.mapToDouble(Number::doubleValue);		
	}
	
	private void max(Stream<WeatherDataPoint> stream, Function<WeatherDataPoint, ? extends Number> map, Consumer<Float> consumer) {
		OptionalDouble optValue = mapToDouble(stream, map).min();
		if(optValue.isPresent()) {
			consumer.accept(Float.valueOf((float) optValue.getAsDouble()));
		}
	}

	private void min(Stream<WeatherDataPoint> stream, Function<WeatherDataPoint, ? extends Number> map, Consumer<Float> consumer) {
		OptionalDouble optValue = mapToDouble(stream, map).min();
		if(optValue.isPresent()) {
			consumer.accept(Float.valueOf((float) optValue.getAsDouble()));
		}
	}
	
	private void average(Stream<WeatherDataPoint> stream, Function<WeatherDataPoint, ? extends Number> map, Consumer<Float> consumer) {
		OptionalDouble optValue = mapToDouble(stream, map).average();
		if(optValue.isPresent()) {
			consumer.accept(Float.valueOf((float) optValue.getAsDouble()));
		}
	}
	
	public void update(WeatherDataPoint dp) {
		LocalDateTime now = LocalDateTime.now();
		if(store.current != null && store.currentDate.isBefore(now.toLocalDate())) {
			// calc local avg values, add to history, reset today
			if(!store.today.isEmpty()) {
				WeatherData data = getToday();
				if(data != null) {
					store.history.put(store.currentDate, data);
				}
			}
			store.today.clear();
		}
		store.currentDate = now.toLocalDate();
		store.current = dp;
		store.today.put(now, dp);
		save();
	}
	
	private void save() {
		ObjectMapper mapper = new ObjectMapper();
		try(OutputStream os = new BufferedOutputStream(new FileOutputStream("weather.json"))) {
			mapper
				.writerFor(Store.class)
				.withDefaultPrettyPrinter()
				.writeValue(os, store);
			logger.fine("stored weather data");
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Could not store weather data", e);
		}
	}
	
	public void addForecast(WeatherData data) {
		store.history.put(data.getDate(), data); 
	}
	
	public WeatherData getTomorrow() {
		LocalDate tomorrow = LocalDate.now().plusDays(1);
		return store.history.get(tomorrow);
	}
	
	public WeatherData getYesterday() {
		LocalDate yesterday = LocalDate.now().minusDays(1);
		return store.history.get(yesterday);
	}
	
	/**
	 * provides the average values of the last 24 hours
	 */
	public WeatherData getToday() {
		WeatherData data = store.history.computeIfAbsent(store.currentDate, today -> new WeatherData(today));
		
		average(store.today.values().stream(), WeatherDataPoint::getTemp, f -> data.setTemp(f));
		average(store.today.values().stream(), WeatherDataPoint::getHumidity, f -> data.setHumidity(f.intValue()));
		average(store.today.values().stream(), WeatherDataPoint::getPressure, f -> data.setPressure(f.intValue()));
		
		min(store.today.values().stream(), WeatherDataPoint::getTemp, f -> data.setMinTemp(f));
		max(store.today.values().stream(), WeatherDataPoint::getTemp, f -> data.setMaxTemp(f));

		return data;
	}
}