package de.operatorplease.sprinkler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import de.operatorplease.sprinkler.settings.Zone;

class StationMap {

	public static final Station DUMMY_NO_MAIN_VALVE = new Station(new Zone(-1)) {
		@Override public void start() {}
		@Override public void stop()  {}
	};
	
	private Map<Station, List<Station>> groups = new HashMap<Station, List<Station>>();

	public int size() {
		return groups.values().stream().map(l -> Integer.valueOf(l.size())).reduce(0, (e1,e2) -> e1+e2);
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public boolean containsKey(Station key) {
		return groups.containsKey(key);
	}

	public boolean containsValue(Station value) {
		return groups.values().stream().anyMatch(l -> l.contains(value));
	}

	public Station get(int sid) {
		Optional<Station> station = values().stream().filter(s -> s.getZoneId() == sid).findAny();
		return station.isEmpty() ? null : station.get();
	}
	
	public List<Station> get(Station mainValve) {
		List<Station> stations = groups.get(mainValve);
		return stations == null ? Collections.emptyList() : stations;
	}

	public void add(Station mainValve, Station value) {
		groups.computeIfAbsent(mainValve, l -> new ArrayList<>()).add(value);
	}
	
	public void addAll(Station mainValve, List<Station> values) {
		groups.computeIfAbsent(mainValve, l -> new ArrayList<>()).addAll(values);
	}

	public List<Station> put(Station mainValve, List<Station> values) {
		return groups.put(mainValve, values);
	}
	
	public List<Station> remove(Station mainValve) {
		return groups.remove(mainValve);
	}

	public void putAll(Map<? extends Station, ? extends List<Station>> m) {
		groups.putAll(m);
	}

	public void clear() {
		groups.clear();
	}

	public Set<Station> keySet() {
		return groups.keySet();
	}

	public List<Station> values() {
		ArrayList<Station> stations = new ArrayList<>();
		for(List<Station> l: groups.values()) {
			stations.addAll(l);
		}
		return stations;
	}

	public Set<Map.Entry<Station, List<Station>>> entrySet() {
		return groups.entrySet();
	}
}
