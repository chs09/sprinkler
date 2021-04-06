package de.operatorplease.sprinkler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

class ZoneGroup {

	public static final Zone DUMMY_NO_MAIN_VALVE = new Zone(-1) {
		@Override public void stop() { /* dummy, noop */ }
		@Override public void start() { /* dummy, noop */ }
	};
	
	private Map<Zone, List<Zone>> groups = new HashMap<Zone, List<Zone>>();

	public int size() {
		return groups.values().stream().map(l -> Integer.valueOf(l.size())).reduce(0, (e1,e2) -> e1+e2);
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	public boolean containsKey(Zone key) {
		return groups.containsKey(key);
	}

	public boolean containsValue(Zone value) {
		return groups.values().stream().anyMatch(l -> l.contains(value));
	}

	public Zone get(int sid) {
		Optional<Zone> station = values().stream().filter(s -> s.getZid() == sid).findAny();
		return station.isEmpty() ? null : station.get();
	}
	
	public List<Zone> get(Zone mainValve) {
		List<Zone> stations = groups.get(mainValve);
		return stations == null ? Collections.emptyList() : stations;
	}

	public void add(Zone mainValve, Zone value) {
		groups.computeIfAbsent(mainValve, l -> new ArrayList<>()).add(value);
	}
	
	public void addAll(Zone mainValve, List<Zone> values) {
		groups.computeIfAbsent(mainValve, l -> new ArrayList<>()).addAll(values);
	}

	public List<Zone> put(Zone mainValve, List<Zone> values) {
		return groups.put(mainValve, values);
	}
	
	public List<Zone> remove(Zone mainValve) {
		return groups.remove(mainValve);
	}

	public void putAll(Map<? extends Zone, ? extends List<Zone>> m) {
		groups.putAll(m);
	}

	public void clear() {
		groups.clear();
	}

	public Set<Zone> keySet() {
		return groups.keySet();
	}

	public List<Zone> values() {
		ArrayList<Zone> stations = new ArrayList<>();
		for(List<Zone> l: groups.values()) {
			stations.addAll(l);
		}
		return stations;
	}

	public Set<Map.Entry<Zone, List<Zone>>> entrySet() {
		return groups.entrySet();
	}
}
