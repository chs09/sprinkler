package de.operatorplease.sprinkler.weather;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
		public DataPoint current;
		public final List<DataPoint> today = new ArrayList<>();
		public final Map<LocalDate, Data> history = new LinkedHashMap<>() {
			private static final long serialVersionUID = 8456961946275535312L;

			protected boolean removeEldestEntry(Map.Entry<LocalDate,Data> eldest) {
				// do not store longer than 1 year
				// can be changed later
				return history.size() >= 366;
			};
		};

		public void setToday(List<DataPoint> today) {
			if(today != null) {
				this.today.clear();
				this.today.addAll(today);
			}
		}
		
		public void setHistory(Map<LocalDate, Data> history) {
			if(history != null) {
				this.history.clear();
				this.history.putAll(history);;
			}
		}
		
		public void setCurrent(DataPoint current) {
			this.current = current;
		}
		
		public void setCurrentDate(LocalDate currentDate) {
			this.currentDate = currentDate;
		}
		
		public DataPoint getCurrent() {
			return current;
		}
		
		public LocalDate getCurrentDate() {
			return currentDate;
		}
		
		public Map<LocalDate, Data> getHistory() {
			return history;
		}
		
		public List<DataPoint> getToday() {
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
	
	private DoubleStream mapToDouble(Stream<DataPoint> stream, Function<DataPoint, ? extends Number> map) {
		return stream
				.map(map)
				.filter(Objects::nonNull)
				.mapToDouble(Number::doubleValue);		
	}
	
	private void max(Stream<DataPoint> stream, Function<DataPoint, ? extends Number> map, Consumer<Float> consumer) {
		OptionalDouble optValue = mapToDouble(stream, map).min();
		if(optValue.isPresent()) {
			consumer.accept(Float.valueOf((float) optValue.getAsDouble()));
		}
	}

	private void min(Stream<DataPoint> stream, Function<DataPoint, ? extends Number> map, Consumer<Float> consumer) {
		OptionalDouble optValue = mapToDouble(stream, map).min();
		if(optValue.isPresent()) {
			consumer.accept(Float.valueOf((float) optValue.getAsDouble()));
		}
	}
	
	private void average(Stream<DataPoint> stream, Function<DataPoint, ? extends Number> map, Consumer<Float> consumer) {
		OptionalDouble optValue = mapToDouble(stream, map).average();
		if(optValue.isPresent()) {
			consumer.accept(Float.valueOf((float) optValue.getAsDouble()));
		}
	}
	
	public void update(DataPoint dp) {
		LocalDate now = LocalDate.now();
		if(store.current != null && store.currentDate.isBefore(now)) {
			// calc local avg values, add to history, reset today
			if(!store.today.isEmpty()) {
				Data data = store.history.computeIfAbsent(store.currentDate, today -> new Data(today));
				
				average(store.today.stream(), DataPoint::getTemp, f -> data.setTemp(f));
				average(store.today.stream(), DataPoint::getHumidity, f -> data.setHumidity(f.intValue()));
				average(store.today.stream(), DataPoint::getPressure, f -> data.setPressure(f.intValue()));
				
				min(store.today.stream(), DataPoint::getTemp, f -> data.setMinTemp(f));
				max(store.today.stream(), DataPoint::getTemp, f -> data.setMaxTemp(f));
			}
			store.today.clear();
		}
		store.currentDate = now;
		store.current = dp;
		store.today.add(dp);
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
	
	public void addForecast(Data data) {
		store.history.put(data.getDate(), data); 
	}
	
	public Data getTomorrow() {
		LocalDate tomorrow = LocalDate.now().plusDays(1);
		return store.history.get(tomorrow);
	}
	
	public Data getYesterday() {
		LocalDate yesterday = LocalDate.now().minusDays(1);
		return store.history.get(yesterday);
	}
}